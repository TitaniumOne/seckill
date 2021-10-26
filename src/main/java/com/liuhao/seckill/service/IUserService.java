package com.liuhao.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liuhao.seckill.pojo.User;
import com.liuhao.seckill.vo.LoginVo;
import com.liuhao.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liuhao
 * @since 2021-10-19
 */
public interface IUserService extends IService<User> {

    /**
     * 登录
     * @param loginVo
     * @param request
     * @param response
     * @return
     */
    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据ticket获取用户
     * @param userTicket
     * @param request
     * @param response
     * @return
     */
    User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);
}
