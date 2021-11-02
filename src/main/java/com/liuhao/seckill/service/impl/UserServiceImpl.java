package com.liuhao.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuhao.seckill.exception.GlobalException;
import com.liuhao.seckill.mapper.UserMapper;
import com.liuhao.seckill.pojo.User;
import com.liuhao.seckill.service.IUserService;
import com.liuhao.seckill.utils.CookieUtil;
import com.liuhao.seckill.utils.MD5Util;
import com.liuhao.seckill.utils.UUIDUtil;
import com.liuhao.seckill.vo.LoginVo;
import com.liuhao.seckill.vo.RespBean;
import com.liuhao.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liuhao
 * @since 2021-10-19
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired(required = false)
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 功能描述：登录
     * @param loginVo
     * @return
     */
    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();

        // //参数校验 - 已经替换为了SR303的方式
        // if(StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)) {
        //     return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        // }
        // if(!ValidatorUtil.isMobile(mobile)) {
        //     return RespBean.error(RespBeanEnum.MOBILE_ERROR);
        // }
        //上面判断没问题则去数据库查询,根据手机号获取用户

        User user = userMapper.selectById(mobile);
        if(null == user) {
            // return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        if(MD5Util.inputPassToDBPass(password, user.getSalt()).equals(user.getPassword())){
            // return RespBean.error(RespBeanEnum.LOGIN_ERROR);
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        // 生成cookie
        String ticket = UUIDUtil.uuid();
        // 将用户信息存入redis
        redisTemplate.opsForValue().set("user:" + ticket, user);

        // 得到session，存入数据
        // request.getSession().setAttribute(ticket, user);


        CookieUtil.setCookie(request, response, "userTicket", ticket);
        return RespBean.success(ticket);
    }

    /**
     * 根据ticket获取用户
     * @param userTicket
     * @param request
     * @param response
     * @return
     */
    @Override
    public User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if(StringUtils.isEmpty(userTicket)) {
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
        if(user != null) {
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
        }
        return user;
    }

    @Override
    public RespBean updatePassword(String userTicket, String password, HttpServletRequest request, HttpServletResponse response) {
        User user = getUserByCookie(userTicket, request, response);
        if(user == null) {
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
        }
        user.setPassword((MD5Util.inputPassToDBPass(password, user.getSalt())));
        int result = userMapper.updateById(user);
        if(1 == result) {
            // 删除redis中的旧数据
            redisTemplate.delete("user:" + userTicket);
            return RespBean.success();
        }
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }


}
