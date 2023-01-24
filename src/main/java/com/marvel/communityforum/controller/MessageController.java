package com.marvel.communityforum.controller;

import com.marvel.communityforum.annotation.LoginRequired;
import com.marvel.communityforum.entity.Message;
import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.service.MessageService;
import com.marvel.communityforum.service.UserService;
import com.marvel.communityforum.util.CommunityUtil;
import com.marvel.communityforum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @GetMapping("/message/list")
    public Map<String, Object> getConversationList(@RequestParam(name = "current", defaultValue = "1") int current,
                                                   @RequestParam(name = "limit", defaultValue = "10") int limit) {
        User user = hostHolder.getUser();
        List<Message> conversationList = messageService.getConversations(user.getId(),
                CommunityUtil.getOffset(current, limit), limit);
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> conversationMap = new HashMap<>();
                conversationMap.put("conversation", message);
                conversationMap.put("conversationMessageCount",
                        messageService.getConversationMessageCount(message.getConversationId()));
                conversationMap.put("conversationUnreadMessageCount",
                        messageService.getConversationUnreadMessageCount(user.getId(), message.getConversationId()));

                int targetUserId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                conversationMap.put("conversationTargetUser", userService.getUserById(targetUserId));

                conversations.add(conversationMap);
            }
        }

        int unreadMessageCount = messageService.getUnreadMessageCount(user.getId());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("conversations", conversations);
        resultMap.put("unreadCount", unreadMessageCount);
        return resultMap;
    }

    @LoginRequired
    @GetMapping("/message/detail/{conversationId}")
    public Map<String, Object> getConversationDetail(@PathVariable("conversationId") String conversationId,
                                        @RequestParam(name = "current", defaultValue = "1") int current,
                                        @RequestParam(name = "limit", defaultValue = "10") int limit) {
        List<Message> messageList = messageService.getConversationMessages(conversationId,
                CommunityUtil.getOffset(current, limit), limit);
        List<Map<String, Object>> detailList = new ArrayList<>();
        if (messageList != null) {
            for (Message message : messageList) {
                Map<String, Object> map = new HashMap<>();
                map.put("message", message);
                map.put("fromUser", userService.getUserById(message.getFromId()));
                detailList.add(map);
            }
        }

        Map<String, Object> detailMap = new HashMap<>();
        detailMap.put("messages", detailList);
        detailMap.put("conversationTargetUser", getConversationTarget(conversationId));

        return detailMap;
    }

    private User getConversationTarget(String conversationId) {
        String[] ids = conversationId.split("-");
        int id1 = Integer.parseInt(ids[0]);
        int id2 = Integer.parseInt(ids[1]);
        int userId = hostHolder.getUser().getId();
        if (userId == id1) {
            return userService.getUserById(id2);
        } else {
            return userService.getUserById(id1);
        }
    }
}
