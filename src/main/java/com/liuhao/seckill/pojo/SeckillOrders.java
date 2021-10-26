package com.liuhao.seckill.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author liuhao
 * @since 2021-10-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_seckill_orders")
public class SeckillOrders implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 秒杀订单id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id - 与用户表的id进行关联
     */
    private Long userId;

    /**
     * 订单id - 与订单表的id进行关联
     */
    private Long orderId;

    /**
     * 商品id - 与商品表的id进行关联
     */
    private Long goodsId;


}
