package com.liuhao.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liuhao.seckill.config.AccessLimit;
import com.liuhao.seckill.exception.GlobalException;
import com.liuhao.seckill.pojo.Orders;
import com.liuhao.seckill.pojo.SecKillMessage;
import com.liuhao.seckill.pojo.SeckillOrders;
import com.liuhao.seckill.pojo.User;
import com.liuhao.seckill.rabbitmq.MQSender;
import com.liuhao.seckill.service.IGoodsService;
import com.liuhao.seckill.service.IOrdersService;
import com.liuhao.seckill.service.ISeckillOrdersService;
import com.liuhao.seckill.utils.JsonUtil;
import com.liuhao.seckill.vo.GoodsVo;
import com.liuhao.seckill.vo.RespBean;
import com.liuhao.seckill.vo.RespBeanEnum;

import com.rabbitmq.tools.json.JSONUtil;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * 5000个用户
 * 秒杀10个商品 QPS：2571/s
 * rabbitMq QPS: 3224/s
 * lua分布式锁 QPS: 3083/s
 */
@Slf4j
@Controller
@RequestMapping("/secKill")
public class SecKillController implements InitializingBean {
    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrdersService seckillOrdersService;

    @Autowired
    private IOrdersService ordersService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;

    @Autowired
    private RedisScript<Long> script;

    private Map<Long, Boolean> emptyStockMap = new HashMap<>();


    @RequestMapping(value = "captcha", method = RequestMethod.GET)
    public void verifyCode(User user, Long goodsId, HttpServletResponse response) {
        if(user == null || goodsId < 0) {
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        // 设置请求头为输出图片类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");  // 不能缓存，避免刷新后是旧的验证码
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 生成的验证码放入redis
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        // 获取验证码
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败", e.getMessage());
        }
    }

    /**
     * 获取秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    @AccessLimit(second=5, maxCount=5, needLogin=true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha) {
        if(user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        boolean isChecked = ordersService.checkCaptcha(user, goodsId, captcha);
        if(!isChecked) {
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        // 用户+商品编号 对应一个path
        String str = ordersService.createPath(user, goodsId);
        return RespBean.success(str);
    }


    @RequestMapping(value="/{path}/doSecKill", method=RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(@PathVariable String path, User user, Long goodsId) {
        if(user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        boolean isLegal = ordersService.checkPath(user, goodsId, path);
        if(!isLegal) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }

        /**
         * 预减库存优化
         */
        // 判断是否重复抢购
        SeckillOrders seckillOrders = seckillOrdersService.getOne(new QueryWrapper<SeckillOrders>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        if(seckillOrders != null) {
            return RespBean.error(RespBeanEnum.REPEAT_ERR);
        }

        // 预先检查内存标记的商品库存，减少redis的访问
        if (emptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 预减库存
        // Long stock = valueOperations.decrement("secKillGoods:" + goodsId);  // redis原子操作
        Long stock = (Long) redisTemplate.execute(script, Collections.singletonList("secKillGoods:" + goodsId), Collections.EMPTY_LIST);  // lua脚本
        if(stock < 0) {
            // valueOperations.increment("secKillGoods:" + goodsId);  // redis原子操作
            // 内存标记空库存的商品号
            emptyStockMap.put(goodsId, true);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 库存足够则生成订单
        // 封装后发给rabbitMq
        SecKillMessage secKillMessage = new SecKillMessage(user, goodsId);
        mqSender.sendMessage(JsonUtil.objectToJson(secKillMessage));
        // 0代表排队中
        return RespBean.success(0);
    }

    @RequestMapping(value = "/getResult", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId) {
        if(user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrdersService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

    /**
     * 系统初始化，把商品库存加载到Redis
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("secKillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            emptyStockMap.put(goodsVo.getId(), false);
        });
    }
}
