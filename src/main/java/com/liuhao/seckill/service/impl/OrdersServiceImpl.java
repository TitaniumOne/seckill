package com.liuhao.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuhao.seckill.exception.GlobalException;
import com.liuhao.seckill.mapper.OrdersMapper;
import com.liuhao.seckill.pojo.Orders;
import com.liuhao.seckill.pojo.SeckillGoods;
import com.liuhao.seckill.pojo.SeckillOrders;
import com.liuhao.seckill.pojo.User;
import com.liuhao.seckill.service.IGoodsService;
import com.liuhao.seckill.service.IOrdersService;
import com.liuhao.seckill.service.ISeckillGoodsService;
import com.liuhao.seckill.service.ISeckillOrdersService;
import com.liuhao.seckill.utils.MD5Util;
import com.liuhao.seckill.utils.UUIDUtil;
import com.liuhao.seckill.vo.GoodsVo;
import com.liuhao.seckill.vo.OrderDetailVo;
import com.liuhao.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liuhao
 * @since 2021-10-22
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

    @Autowired
    private ISeckillGoodsService seckillGoodsService;

    @Autowired(required = false)
    private OrdersMapper ordersMapper;

    @Autowired
    private ISeckillOrdersService seckillOrdersService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 秒杀实现
     * @param user
     * @param goodsVo
     * @return
     */
    @Transactional
    @Override
    public Orders secKill(User user, GoodsVo goodsVo) {
        ValueOperations valueOperations = redisTemplate.opsForValue();

        //秒杀商品表
        SeckillGoods secKillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goodsVo.getId()));
        //注意不要直接用前端传输的库存数，容易作假

        secKillGoods.setStockCount(secKillGoods.getStockCount() - 1);
        // 更新秒杀商品表的库存数
        boolean result = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().setSql("stock_count = stock_count-1").eq("goods_id", goodsVo.getId()).gt("stock_count",0));

        if(secKillGoods.getStockCount() < 1) {
            // 用于判断是否还有库存
            valueOperations.set("isEmptyStock:" + goodsVo.getId(), "0");
            return null;
        }

        //生成订单
        Orders order = new Orders();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(secKillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        ordersMapper.insert(order);

        //生成秒杀订单
        SeckillOrders seckillOrders = new SeckillOrders();
        seckillOrders.setUserId(user.getId());
        seckillOrders.setOrderId(order.getId());
        seckillOrders.setGoodsId(goodsVo.getId());
        seckillOrdersService.save(seckillOrders);
        redisTemplate.opsForValue().set("order:" + user.getId() + ":" + goodsVo.getId(), seckillOrders);

        return order;
    }

    @Override
    public OrderDetailVo detail(Long orderId) {
        if(orderId == null)
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        Orders orders = ordersMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(orders.getGoodsId());
        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setGoodsVo(goodsVo);
        orderDetailVo.setOrders(orders);
        return orderDetailVo;
    }

    @Override
    public String createPath(User user, Long goodsId) {
        // 生成一个字符串，夹在secKill/doSecKill中间，并且存入redis
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisTemplate.opsForValue().set("secKillPath:" + user.getId() + ":" + goodsId, str,60, TimeUnit.SECONDS);
        return str;
    }

    /**
     * 校验秒杀地址
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if(user == null || goodsId < 0 || StringUtils.isEmpty(path)) {
            return false;
        }
        String redisPath = (String) redisTemplate.opsForValue().get("secKillPath:" + user.getId() + ":" + goodsId);
        return path.equals(redisPath);
    }

    /**
     * 校验验证码
     * @param user
     * @param goodsId
     * @param captcha
     * @return
     */
    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {
        if(user == null || StringUtils.isEmpty(captcha) || goodsId < 0) {
            return false;
        }
        String redisCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);
        return captcha.equals(redisCaptcha);
    }
}
