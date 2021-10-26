package com.liuhao.seckill.config;

import com.liuhao.seckill.pojo.User;
import com.liuhao.seckill.service.IUserService;
import com.liuhao.seckill.service.impl.UserServiceImpl;
import com.liuhao.seckill.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    @Autowired
    private IUserService userService;

    /**
     * 首先判断传入的参数是否属于User类，如果返回true，才能执行resolveArgument方法
     * @param parameter
     * @return
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> clazz = parameter.getParameterType();
        return clazz == User.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        String userTicket = CookieUtil.getCookieValue(request, "userTicket");
        if(StringUtils.isEmpty(userTicket)) {
            return null;
        }
        User user = userService.getUserByCookie(userTicket, request, response);
        return user;
    }
}
