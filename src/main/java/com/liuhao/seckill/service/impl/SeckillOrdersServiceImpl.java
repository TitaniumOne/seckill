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

}
