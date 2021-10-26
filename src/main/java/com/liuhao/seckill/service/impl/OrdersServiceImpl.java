package com.liuhao.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuhao.seckill.mapper.OrdersMapper;
import com.liuhao.seckill.pojo.Orders;
import com.liuhao.seckill.service.IOrdersService;
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
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

}
