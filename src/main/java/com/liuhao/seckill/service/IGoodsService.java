package com.liuhao.seckill.service;

import com.liuhao.seckill.pojo.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liuhao.seckill.vo.GoodsVo;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liuhao
 * @since 2021-10-22
 */
public interface IGoodsService extends IService<Goods> {
    /**
     * 获取商品列表
     * @return
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 获取商品详情
     * @param goodsId
     * @return
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
