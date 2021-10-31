package com.liuhao.seckill.vo;

import com.liuhao.seckill.pojo.Orders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailVo {
    GoodsVo goodsVo;
    Orders orders;
}
