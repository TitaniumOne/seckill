package com.liuhao.seckill.service;

import com.liuhao.seckill.pojo.Orders;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liuhao.seckill.pojo.User;
import com.liuhao.seckill.vo.GoodsVo;
import com.liuhao.seckill.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liuhao
 * @since 2021-10-22
 */
public interface IOrdersService extends IService<Orders> {

    Orders secKill(User user, GoodsVo goodsVo);

    OrderDetailVo detail(Long orderId);
}
