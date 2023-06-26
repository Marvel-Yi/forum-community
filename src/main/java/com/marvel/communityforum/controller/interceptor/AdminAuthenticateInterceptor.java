package com.marvel.communityforum.controller.interceptor;

import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.util.CommunityConstant;
import com.marvel.communityforum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AdminAuthenticateInterceptor implements HandlerInterceptor, CommunityConstant {
    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User user = hostHolder.getUser();
        if (user == null || user.getUserType() != USER_TYPE_ADMIN) {
            response.sendRedirect("/deny");
            return false;
        }
        return true;
    }
}
