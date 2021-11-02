package com.liuhao.seckill.controller;

import com.liuhao.seckill.pojo.User;
import com.liuhao.seckill.service.IGoodsService;
import com.liuhao.seckill.service.IUserService;
import com.liuhao.seckill.service.impl.UserServiceImpl;
import com.liuhao.seckill.vo.DetailVo;
import com.liuhao.seckill.vo.GoodsVo;
import com.liuhao.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 商品
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    /**
     * 跳转到商品列表页
     * 优化前QPS：2079/s
     * 缓存后QPS：6218/s
     *          2819/s
     * @param user
     * @param model
     * @return
     */
    @RequestMapping(value="/toList", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user, HttpServletRequest request, HttpServletResponse response){
        // Redis中获取页面，如果不为空直接返回页面
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        if(!StringUtils.isEmpty(html)) {
            return html;
        }
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        // 如果html为空，手动渲染，存入redis并且返回
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", context);
        if(!StringUtils.isEmpty(html)) {
            // 只缓存60秒，然后失效
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);
        }
        return html;
    }

    /*
    @RequestMapping(value = "/toDetail/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail(Model model, User user, @PathVariable Long goodsId, HttpServletRequest request, HttpServletResponse response) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsDetail:" + goodsId);
        if(!StringUtils.isEmpty(html)) {
            return html;
        }

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

        // 如果html为空，手动渲染，存入redis并且返回
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", context);
        if(!StringUtils.isEmpty(html)) {
            // 只缓存60秒，然后失效
            valueOperations.set("goodsDetail:" + goodsId, html, 60, TimeUnit.SECONDS);
        }
        return html;
    }
    */
    // 页面静态化后，前端ajax请求接口，接口只需要返回对象
    @RequestMapping("/detail/{goodsId}")
    @ResponseBody
    public RespBean toDetail(Model model, User user, @PathVariable Long goodsId) {
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        // System.out.println(goodsVo.getGoodsName());
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
        DetailVo detailVo = new DetailVo();
        detailVo.setUser(user);
        detailVo.setRemainSeconds(remainSeconds);
        detailVo.setSecKillStatus(secKillStatus);
        detailVo.setGoodsVo(goodsVo);
        return RespBean.success(detailVo);
    }
}
