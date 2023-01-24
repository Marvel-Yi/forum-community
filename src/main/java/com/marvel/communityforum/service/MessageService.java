package com.marvel.communityforum.service;

import com.marvel.communityforum.dao.MessageMapper;
import com.marvel.communityforum.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;

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
}
