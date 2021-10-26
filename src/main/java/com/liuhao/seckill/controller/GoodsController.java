package com.liuhao.seckill.controller;

import com.liuhao.seckill.pojo.User;
import com.liuhao.seckill.service.IGoodsService;
import com.liuhao.seckill.service.IUserService;
import com.liuhao.seckill.service.impl.UserServiceImpl;
import com.liuhao.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 * 商品
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private IGoodsService goodsService;

    /**
     * 跳转到商品列表页
     * @param user
     * @param model
     * @return
     */
    @RequestMapping("/toList")
    public String toList(Model model, User user){
        // if(StringUtils.isEmpty(ticket)) {
        //     return "login";
        // }
        // // 使用session 获取 user
        // // User user = (User) session.getAttribute(ticket);
        // User user = userService.getUserByCookie(ticket, request, response);
        //
        // if(null == user) {
        //     return "login";
        // }
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        return "goodsList";
    }

    @RequestMapping("/toDetail/{goodsId}")
    public String toDetail(Model model, User user, @PathVariable Long goodsId) {
        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        System.out.println(goodsVo.getGoodsName());
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();

        // 秒杀状态
        int secKillStatus = 0;
        int remainSeconds = 0;
        if(nowDate.before(startDate)) {
            // 秒杀还未开始
            secKillStatus = 0;
            remainSeconds = (int)((startDate.getTime() - nowDate.getTime())/1000);
        } else {
            // 秒杀正在进行
            secKillStatus = 1;
            remainSeconds = 0;
        }

        if(nowDate.after(endDate)) {
            // 秒杀已经结束
            secKillStatus = 2;
            remainSeconds = -1;
        }
        model.addAttribute("remainSeconds", remainSeconds);
        model.addAttribute("secKillStatus", secKillStatus);
        model.addAttribute("goods", goodsVo);
        return "goodsDetail";
    }
}
