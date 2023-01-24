package com.marvel.communityforum.dao;

import com.marvel.communityforum.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    List<Message> selectConversationLatestMessage(int userId, int offset, int limit);

    int selectConversationCount(int userId);

    List<Message> selectConversationMessages(String conversationId, int offset, int limit);

    int selectConversationMessageCount(String conversationId);

    int selectUnreadMessageCount(int userId);

    int selectConversationUnreadMessageCount(int userId, String conversationId);
}
