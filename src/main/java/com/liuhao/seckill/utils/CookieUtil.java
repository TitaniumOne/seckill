package com.liuhao.seckill.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class CookieUtil {
    /**
     * 得到cookie的值，无需解码
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        return getCookieValue(request, cookieName, false);
    }

    /**
     * 得到cookie的值, 采用UTF-8解码或无需解码
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName, boolean isDecoder) {
        Cookie[] cookieList = request.getCookies();
        if (cookieList == null || cookieName == null) {
            return null;
        }
        String retValue = null;
        try {
            for (int i = 0; i < cookieList.length; i++) {
                if (cookieList[i].getName().equals(cookieName)) {
                    if (isDecoder) {
                        retValue = URLDecoder.decode(cookieList[i].getValue(), "UTF-8");
                    } else {
                        retValue = cookieList[i].getValue();
                    }
                    break;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return retValue;
    }

    /**
     * 得到cookie的值, 采用UTF-8解码或无需解码
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName, String encodingString) {
        Cookie[] cookieList = request.getCookies();
        if (cookieList == null || cookieName == null) {
            return null;
        }
        String retValue = null;
        try {
            for (int i = 0; i < cookieList.length; i++) {
                if (cookieList[i].getName().equals(cookieName)) {
                    retValue = URLDecoder.decode(cookieList[i].getValue(), encodingString);
                    break;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return retValue;
    }

    /**
     * 设置cookie的值。不设置生效时间，默认浏览器关闭则失效，也不编码
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue) {
        setCookie(request, response, cookieName, cookieValue, -1);
    }

    /**
     * 设置cookie的值。在指定时间内生效，但不编码
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue, int cookieMaxAge) {
        setCookie(request, response, cookieName, cookieValue, cookieMaxAge, false);
    }

    /**
     * 设置cookie的值。不设置生效时间，但编码
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue, boolean isEncode) {
        setCookie(request, response, cookieName, cookieValue, -1, isEncode);
    }

    /**
     * 设置cookie的值。在指定时间内生效，编码参数
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue, int cookieMaxAge, boolean isEncode) {
        doSetCookie(request, response, cookieName, cookieValue, cookieMaxAge, isEncode);
    }

    /**
     * 设置cookie的值。在指定时间内生效，指定编码参数
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue, int cookieMaxAge, String encodeString) {
        doSetCookie(request, response, cookieName, cookieValue, cookieMaxAge, encodeString);
    }

    /**
     * 删除Cookie带cookie的域名
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        doSetCookie(request, response, cookieName, "", -1, false);
    }

    /**
     * 设置Cookie的值，并且使其在指定时间内生效
     */
    private static final void doSetCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue, int cookieMaxAge, boolean isEncode) {
        try {
            if (cookieValue == null) {
                cookieValue = "";
            } else if (isEncode) {
                cookieValue = URLEncoder.encode(cookieValue, "UTF-8");
            }
            Cookie cookie = new Cookie(cookieName, cookieValue);
            if (cookieMaxAge > 0)
                cookie.setMaxAge(cookieMaxAge);
            if (null != request) {
                // 设置域名的cookie
                String domainName = getDomainName(request);
                System.out.println(domainName);
                if (!"localhost".equals(domainName)) {
                    cookie.setDomain(domainName);
                }
            }
            cookie.setPath("/");
            response.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置Cookie的值，并且使其在指定时间内生效
     */
    private static final void doSetCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue, int cookieMaxAge, String encodeString) {
        try {
            if (cookieValue == null) {
                cookieValue = "";
            } else {
                cookieValue = URLEncoder.encode(cookieValue, encodeString);
            }
            Cookie cookie = new Cookie(cookieName, cookieValue);
            if (cookieMaxAge > 0)
                cookie.setMaxAge(cookieMaxAge);
            if (null != request) {
                // 设置域名的cookie
                String domainName = getDomainName(request);
                System.out.println(domainName);
                if (!"localhost".equals(domainName)) {
                    cookie.setDomain(domainName);
                }
            }
            cookie.setPath("/");
            response.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 得到cookie的域名
     */
    private static final String getDomainName(HttpServletRequest request) {
        String domainName = null;
        // 通过request对象获取访问的url地址
        String serverName = request.getRequestURI().toString();
        if (serverName == null || serverName.equals("")) {
            domainName = "";
        } else {
            // 将url地址转为小写
            serverName = serverName.toLowerCase();
            // 如果url地址是以http://开头，将其截取
            if (serverName.startsWith("http://")) {
                serverName = serverName.substring(7);
            }
            int end = serverName.length();
            // 判断url地址是否包含"/"
            if (serverName.contains("/")) {
                // 得到第一个 / 出现的位置
                end = serverName.indexOf("/");
            }
            // 截取
            serverName = serverName.substring(0, end);
            // 根据"."进行分割
            final String[] domains = serverName.split("\\.");
            int len = domains.length;
            if (len > 3) {
                // www.xxx.com.cn
                domainName = domains[len - 3] + "." + domains[len - 2] + "." + domains[len - 1];
            } else if (len <= 3 && len > 1) {
                // xxx.com or xxx.cn
                domainName = domains[len - 2] + "." + domains[len - 1];
            } else {
                domainName = serverName;
            }
        }
        if (domainName != null && domainName.indexOf(":") > 0) {
            String[] arg = domainName.split("\\:");
            domainName = arg[0];
        }
        return domainName;
    }

}
