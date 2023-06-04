package com.marvel.communityforum.controller.interceptor;

import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.service.StatService;
import com.marvel.communityforum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class StatInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private StatService statService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // uv record
        String ip = request.getRemoteHost();
        statService.recordUV(ip);

        // dau record
        User user = hostHolder.getUser();
        if (user != null) {
            statService.recordDAU(user.getId());
        }

        return true;
    }
}
