package com.marvel.communityforum.controller.interceptor;

import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.service.MessageService;
import com.marvel.communityforum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            int unreadConversationMsgCnt = messageService.getUnreadMessageCount(user.getId());
            int unreadNotificationMsgCnt = messageService.getTotalUnreadSystemMsgCnt(user.getId());
            int totalUnread = unreadConversationMsgCnt + unreadNotificationMsgCnt;
            modelAndView.addObject("totalUnreadMessageCount", totalUnread);
        }
    }
}
