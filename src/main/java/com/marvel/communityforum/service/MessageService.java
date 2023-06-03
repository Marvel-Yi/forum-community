package com.marvel.communityforum.service;

import com.marvel.communityforum.dao.MessageMapper;
import com.marvel.communityforum.entity.Message;
import com.marvel.communityforum.util.SensitiveWordFilterTrie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveWordFilterTrie sensitiveWordFilter;

    public List<Message> getConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversationLatestMessage(userId, offset, limit);
    }

    public int getConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> getConversationMessages(String conversationId, int offset, int limit) {
        return messageMapper.selectConversationMessages(conversationId, offset, limit);
    }

    public int getConversationMessageCount(String conversationId) {
        return messageMapper.selectConversationMessageCount(conversationId);
    }

    public int getUnreadMessageCount(int userId) {
        return messageMapper.selectUnreadMessageCount(userId);
    }

    public int getConversationUnreadMessageCount(int userId, String conversationId) {
        return messageMapper.selectConversationUnreadMessageCount(userId, conversationId);
    }

    public int addMessage(Message message) {
        message.setContent(sensitiveWordFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    public int readMessage(List<Integer> messageIds) {
        return messageMapper.updateMessageStatus(messageIds, 1);
    }

    public Message getSystemNotification(int userId, String topic) {
        return messageMapper.selectSystemLatestMessage(userId, topic);
    }

    public int getSystemNotificationMessageCount(int userId, String topic) {
        return messageMapper.selectSystemMessageCount(userId, topic);
    }

    public int getUnreadSystemMessageCount(int userId, String topic) {
        return messageMapper.selectUnreadSystemMessageCount(userId, topic);
    }

    public int getTotalUnreadSystemMsgCnt(int userId) {
        return messageMapper.selectTotalUnreadSystemMessageCount(userId);
    }

    public List<Message> getSystemNotifications(int userId, String topic, int offset, int limit) {
        return messageMapper.selectSystemNotifications(userId, topic, offset, limit);
    }
}
