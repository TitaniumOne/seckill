package com.liuhao.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liuhao.seckill.pojo.Orders;
import com.liuhao.seckill.pojo.SeckillOrders;
import com.liuhao.seckill.pojo.User;
import com.liuhao.seckill.service.IGoodsService;
import com.liuhao.seckill.service.IOrdersService;
import com.liuhao.seckill.service.ISeckillOrdersService;
import com.liuhao.seckill.vo.GoodsVo;
import com.liuhao.seckill.vo.RespBean;
import com.liuhao.seckill.vo.RespBeanEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


/**
 * 5000个用户
 * 秒杀10个商品QPS：2571/s
 */
@Controller
@RequestMapping("/secKill")
public class SecKillController {
    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrdersService seckillOrdersService;

    @Autowired
    private IOrdersService ordersService;

    @Autowired
    private RedisTemplate redisTemplate;
    //
    // @RequestMapping("/doSecKill")
    // public String doSecKill(Model model, User user, Long goodsId) {
    //     if(user == null) {
    //         return "login";
    //     }
    //     model.addAttribute("user", user);
    //     GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
    //
    //     //检查库存
    //     if(goodsVo.getStockCount() < 1) {
    //         model.addAttribute("errorMsg", RespBeanEnum.EMPTY_STOCK.getMessage());
    //         return "secKillFail";
    //     }
    //
    //     //判断是否重复抢购
    //     SeckillOrders seckillOrders = seckillOrdersService.getOne(new QueryWrapper<SeckillOrders>().eq("user_id", user.getId()).eq("goods_id", goodsId));
    //     if(seckillOrders != null) {
    //         model.addAttribute("errorMsg", RespBeanEnum.REPEAT_ERR.getMessage());
    //         return "secKillFail";
    //     }
    //
    //     //进入详情页面
    //     Orders order = seckillOrdersService.secKill(user, goodsVo);
    //     model.addAttribute("order", order);
    //     model.addAttribute("goods", goodsVo);
    //     return "orderDetail";
    // }


    @RequestMapping(value="/doSecKill", method=RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(Model model, User user, Long goodsId) {
        if(user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);

        //检查库存
        if(goodsVo.getStockCount() < 1) {
            model.addAttribute("errorMsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        // 判断是否重复抢购
        // SeckillOrders seckillOrders = seckillOrdersService.getOne(new QueryWrapper<SeckillOrders>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        SeckillOrders seckillOrders = (SeckillOrders) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVo.getId());
        if(seckillOrders != null) {
            return RespBean.error(RespBeanEnum.REPEAT_ERR);
        }

        //进入详情页面
        Orders order = ordersService.secKill(user, goodsVo);
        return RespBean.success(order);
    }
}
