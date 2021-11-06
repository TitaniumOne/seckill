package com.liuhao.seckill.service;

import com.liuhao.seckill.pojo.Orders;
import com.liuhao.seckill.pojo.SeckillOrders;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liuhao.seckill.pojo.User;
import com.liuhao.seckill.vo.GoodsVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liuhao
 * @since 2021-10-22
 */
public interface ISeckillOrdersService extends IService<SeckillOrders> {

    Long getResult(User user, Long goodsId);
}
