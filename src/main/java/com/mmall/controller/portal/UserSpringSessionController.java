package com.mmall.controller.portal;

/**
 * 前台用户
 */

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/springsession/")
public class UserSpringSessionController {
    @Autowired
    private IUserService iUserService;

    /**
     * 用户登录
     * @param username
     * @param password
     * @param session
     * @param httpServletResponse
     * @return
     */
    @RequestMapping(value = "login.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session,
                                      HttpServletResponse httpServletResponse){
        //service->mybatis->dao
        ServerResponse<User> response = iUserService.login(username, password);
        if(response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER, response.getData());
            //session.setAttribute(Const.CURRENT_USER, response.getData());
            //CookieUtil.writeLoginToken(httpServletResponse, session.getId());
            //RedisShardedPoolUtil.setEx(session.getId(), JsonUtil.objToString(response.getData()), Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        return response;
    }

    /**
     * 退出登录
     * @param httpServletRequest
     * @param httpServletResponse
     * @return
     */
    @RequestMapping(value = "logout.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> logout(HttpServletRequest httpServletRequest, HttpSession session, HttpServletResponse httpServletResponse){
        //String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        //首先要将浏览器中的token删除
        //CookieUtil.delLoginToken(httpServletRequest, httpServletResponse);
        //redis里面的缓存的cookie也要删除
        //RedisShardedPoolUtil.del(loginToken);
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    /**
     * 获取用户登录信息
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpServletRequest httpServletRequest, HttpSession session){
        //String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        //if(StringUtils.isEmpty(loginToken)){
        //    return ServerResponse.createByErrorMessage("用户未登录，无法获取当前登录信息");
       // }
        //String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        //User user = JsonUtil.stringToObj(userJsonStr, User.class);
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        //因为存进去的时候已经是JSON格式的字符串了，所以取出来的也是
        if(user != null){
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录，无法获取当前登录信息");
    }
}
