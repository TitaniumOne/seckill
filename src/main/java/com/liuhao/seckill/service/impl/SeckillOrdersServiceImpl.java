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
import org.springframework.data.redis.core.RedisTemplate;
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
    @Autowired(required = false)
    private SeckillOrdersMapper seckillOrdersMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    private Object SeckillOrders;

    /**
     * 获取秒杀结果
     * @param user
     * @param goodsId
     * @return orderId:成功， -1：秒杀失败， 0：排队中
     */
    @Override
    public Long getResult(User user, Long goodsId) {
        SeckillOrders seckillOrders = seckillOrdersMapper.selectOne(new QueryWrapper<SeckillOrders>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        if(null != seckillOrders) {
            return seckillOrders.getOrderId();
        } else if (redisTemplate.hasKey("isEmptyStock:" + goodsId)) {
            return -1L;
        } else {
            return 0L;
        }

    }
}
