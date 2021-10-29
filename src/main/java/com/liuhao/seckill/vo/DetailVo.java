package com.liuhao.seckill.vo;

import com.liuhao.seckill.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 详情返回对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailVo {
    private User user;
    private int remainSeconds;
    private int secKillStatus;
    private GoodsVo goodsVo;
}
