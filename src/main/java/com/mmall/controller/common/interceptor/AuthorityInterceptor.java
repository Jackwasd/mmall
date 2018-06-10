package com.mmall.controller.common.interceptor;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * SpringMVC拦截器
 * 注意两个点，一个是富文本上传的问题，返回值是MAP所以要修改，一个是登录时避免陷入死循环的问题
 */
@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor{
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        log.info("preHandle");
        //请求中Controller中的方法名
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        //解析HandlerMethod
        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getClass().getSimpleName(); //只要类名，不要包名

        //解析参数。具体的参数key以及value是什么，我们打印日志
        StringBuffer requestParamBuffer = new StringBuffer();
        Map paramMap = httpServletRequest.getParameterMap();
        Iterator it = paramMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry)it.next();
            String mapKey = (String) entry.getKey();
            String mapValue = StringUtils.EMPTY;
            //request这个参数的map，里面的value返回的是一个String的数组
            Object object = entry.getKey();
            if(object instanceof String[]){
                String[] strs = (String[]) object;
                mapValue = Arrays.toString(strs);
            }
            requestParamBuffer.append(mapKey).append("=").append(mapValue);
        }

        if(StringUtils.equals(className, "UserManageController") && StringUtils.equals(methodName, "login")){
            log.info("权限拦截器拦截到请求,className:{}, methodName:{}", className, methodName);
            //如果是拦截到登录请求，不打印参数，因为参数里面有密码，会打印到日志中，防止日志泄露
            return true;
        }

        User user = null;
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isNotEmpty(loginToken)){
            String userJsonStr = RedisShardedPoolUtil.get(loginToken);
            user = JsonUtil.stringToObj(userJsonStr, User.class);
        }
        if(user == null || (user.getRole().intValue() != Const.Role.ROLE_ADMIN)){
            //返回false,不会调用Controller里面的方法
            httpServletResponse.reset();  //这里要添加reset，否则报异常 getWriter() has already been called for this response.
            httpServletResponse.setCharacterEncoding("UTF-8"); //这里要设置编码，否则会乱码
            httpServletResponse.setContentType("application/json;charset=UTF-8"); //这里要设置返回值的类型，因为全部是JSON接口

            PrintWriter out = httpServletResponse.getWriter();
            if(user == null){
                if(StringUtils.equals(className, "ProductManageController") && StringUtils.equals(methodName, "richtext_img_upload")){
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success", false);
                    resultMap.put("msg", "请登录管理员");
                    out.println(JsonUtil.objToString(resultMap));
                }else{
                    out.println(JsonUtil.objToString(ServerResponse.createByErrorMessage("拦截器拦截，用户未登录")));
                }
            }else{
                if(StringUtils.equals(className, "ProductManageController") && StringUtils.equals(methodName, "richtext_img_upload")){
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success", false);
                    resultMap.put("msg", "无权限操作");
                    out.println(JsonUtil.objToString(resultMap));
                }else{
                    out.println(JsonUtil.objToString(ServerResponse.createByErrorMessage("拦截器拦截，用户无权限操作")));
                }
            }
            out.flush();
            out.close();
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        log.info("postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        log.info("afterCompletion");
    }
}
