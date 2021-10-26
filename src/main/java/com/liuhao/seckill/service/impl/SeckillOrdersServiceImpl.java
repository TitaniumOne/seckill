package com.liuhao.seckill.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liuhao.seckill.mapper.OrdersMapper;
import com.liuhao.seckill.pojo.Orders;
import com.liuhao.seckill.pojo.SeckillGoods;
import com.liuhao.seckill.pojo.SeckillOrders;
import com.liuhao.seckill.mapper.SeckillOrdersMapper;
import com.liuhao.seckill.pojo.User;
import com.liuhao.seckill.service.ISeckillGoodsService;
import com.liuhao.seckill.service.ISeckillOrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuhao.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liuhao
 * @since 2021-10-22
 */
@Service
public class SeckillOrdersServiceImpl extends ServiceImpl<SeckillOrdersMapper, SeckillOrders> implements ISeckillOrdersService {

    @Autowired
    private ISeckillGoodsService seckillGoodsService;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ISeckillOrdersService seckillOrdersService;

    /**
     * 秒杀实现
     * @param user
     * @param goodsVo
     * @return
     */
    @Override
    public Orders secKill(User user, GoodsVo goodsVo) {
        //秒杀商品表
        SeckillGoods secKillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goodsVo.getId()));
        //注意不要直接用前端传输的库存数，容易作假
        secKillGoods.setStockCount(secKillGoods.getStockCount() - 1);
        seckillGoodsService.updateById(secKillGoods);

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

        return order;
    }
}
