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
import com.liuhao.seckill.vo.GoodsVo;
import com.liuhao.seckill.vo.OrderDetailVo;
import com.liuhao.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

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

    @Autowired
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
        //秒杀商品表
        SeckillGoods secKillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goodsVo.getId()));
        //注意不要直接用前端传输的库存数，容易作假

        secKillGoods.setStockCount(secKillGoods.getStockCount() - 1);
        boolean result = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().setSql("stock_count = stock_count-1").eq("goods_id", goodsVo.getId()).gt("stock_count",0));

        if(!result) {
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
}
