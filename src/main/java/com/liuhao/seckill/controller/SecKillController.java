package com.liuhao.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 5000个用户
 * 秒杀10个商品 QPS：2571/s
 * rabbitMq QPS: 3224/s
 * lua分布式锁 QPS: 3083/s
 */
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

    @RequestMapping(value="/doSecKill", method=RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(Model model, User user, Long goodsId) {
        if(user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        /*
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        //检查库存
        if(goodsVo.getStockCount() < 1) {
            model.addAttribute("errorMsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        // 判断是否重复抢购
        // SeckillOrders seckillOrders = seckillOrdersService.getOne(new QueryWrapper<SeckillOrders>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        SeckillOrders seckillOrders = (SeckillOrders) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVo.getId());
        if(seckillOrders != null) {
            return RespBean.error(RespBeanEnum.REPEAT_ERR);
        }

        //进入详情页面
        Orders order = ordersService.secKill(user, goodsVo);
        return RespBean.success(order);
        */

        /**
         * 预减库存优化
         */
        ValueOperations valueOperations = redisTemplate.opsForValue();
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
