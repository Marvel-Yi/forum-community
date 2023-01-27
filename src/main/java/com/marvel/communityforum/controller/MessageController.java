package com.marvel.communityforum.controller;

import com.marvel.communityforum.annotation.LoginRequired;
import com.marvel.communityforum.entity.Message;
import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.service.MessageService;
import com.marvel.communityforum.service.UserService;
import com.marvel.communityforum.util.CommunityUtil;
import com.marvel.communityforum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

        List<Integer> unreadMessageIds = getUnreadMessageIds(messageList);
        if (!unreadMessageIds.isEmpty()) {
            messageService.readMessage(unreadMessageIds);
        }

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

    private List<Integer> getUnreadMessageIds(List<Message> messages) {
        List<Integer> unreadMessageIds = new ArrayList<>();
        if (messages != null) {
            for (Message message : messages) {
                if (message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0) {
                    unreadMessageIds.add(message.getId());
                }
            }
        }
        return unreadMessageIds;
    }

    @LoginRequired
    @PostMapping("/message/send")
    public String sendMessage(String toName, String content) {
        User toUser = userService.getUserByName(toName);
        if (toUser == null) {
            return CommunityUtil.getJSONString(1, "target user not exist");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(toUser.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "-" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "-" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0, "send message succeeded");
    }
}
