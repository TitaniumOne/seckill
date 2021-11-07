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

    /**
     * 获取秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    String createPath(User user, Long goodsId);

    /**
     * 校验秒杀地址
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    boolean checkPath(User user, Long goodsId, String path);

    /**
     * 校验验证码
     * @param user
     * @param goodsId
     * @param captcha
     * @return
     */
    boolean checkCaptcha(User user, Long goodsId, String captcha);
}

