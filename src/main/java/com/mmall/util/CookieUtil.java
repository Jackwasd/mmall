package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CookieUtil {
    private static final String COOKIE_DOMAIN = "happymmall.com";
    private static final String COOKIE_NAME = "mmall_login_token";

    /**
     * 读取存入的cookie
     * @param request
     * @return
     */
    public static String readLoginToken(HttpServletRequest request){
        Cookie[] cks = request.getCookies();
        if(cks != null){
            for (Cookie ck : cks){
                log.info("read cookieName:{},cookieValue:{}", ck.getName(), ck.getValue());
                if(StringUtils.equals(ck.getName(), COOKIE_NAME)){
                    log.info("return cookieName:{}, cookieValue:{}", ck.getName(), ck.getValue());
                    return ck.getValue();
                }
            }
        }
        return null;
    }

    //X:domain=".happymmall.com"
    //a拿不到b的cookie,b也拿不到a的cookie,c和d可以共享a和e的cookie
    //a:A.happymmall.com                 cookie:domain=A.happymmall.com;path="/"
    //b:B.happymmall.com                 cookie:domain=B.happymmall.com;path="/"
    //c:A.happymmall.com/test/cc         cookie:domain=A.happymmall.com;path="/test/cc"
    //d:A.happymmall.com/test/dd         cookie:domain=A.happymmall.com;path="/test/dd"
    //e:A.happymmall.com/test            cookie:domain=A.happymmall.com;path="/test"

    /**
     * 写入cookie
     * @param response
     * @param token
     */
    public static void writeLoginToken(HttpServletResponse response, String token){
        Cookie ck = new Cookie(COOKIE_NAME, token);
        ck.setDomain(COOKIE_DOMAIN);
        ck.setPath("/");    //代表设置在根目录下
        String s = ck.getPath();
        ck.setHttpOnly(true);  //无法通过脚本获取cookie信息，确保安全性
        //如果这个maxage不设置的话,cookie就不会写入硬盘，而是写在内存，只在当前页面有效
        ck.setMaxAge(60 * 60 * 24 * 365);    //设置有效期，如果值是-1的话，就是永久的，单位是秒
        log.info("write cookieName:{}, cookieValue:{}", ck.getName(),ck.getValue());
        response.addCookie(ck);
    }

    /**
     * 删除保存的cookie
     * @param request
     * @param response
     */
    public static void delLoginToken(HttpServletRequest request, HttpServletResponse response){
        Cookie[] cks = request.getCookies();
        if(cks != null){
            for (Cookie ck : cks){
                if(StringUtils.equals(ck.getName(), COOKIE_NAME)){
                    ck.setDomain(COOKIE_DOMAIN);
                    ck.setPath("/");
                    ck.setMaxAge(0); //设置为0，代表删除此cookie
                    log.info("del cookieName:{}, cookieValue:{}", ck.getName(), ck.getValue());
                    response.addCookie(ck); //加入一个有效期为0的cookie，浏览器会删除他
                    return;
                }
            }
        }
    }

}
