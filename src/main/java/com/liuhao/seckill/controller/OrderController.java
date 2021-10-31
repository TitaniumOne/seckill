package com.liuhao.seckill.controller;

import com.liuhao.seckill.pojo.User;
import com.liuhao.seckill.service.IOrdersService;
import com.liuhao.seckill.vo.OrderDetailVo;
import com.liuhao.seckill.vo.RespBean;
import com.liuhao.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private IOrdersService ordersService;

    /**
     * 订单详情
     * @param user
     * @param orderId
     * @return
     */
    @RequestMapping("/detail")
    @ResponseBody
    public RespBean detail(User user, Long orderId) {
        if(user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        OrderDetailVo detail = ordersService.detail(orderId);
        return RespBean.success(detail);
    }
}
