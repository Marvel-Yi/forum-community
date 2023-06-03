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

    int insertMessage(Message message);

    int updateMessageStatus(List<Integer> messageIds, int status);

    Message selectSystemLatestMessage(int userId, String topic);

    int selectSystemMessageCount(int userId, String topic);

    int selectUnreadSystemMessageCount(int userId, String topic);

    int selectTotalUnreadSystemMessageCount(int userId);

    List<Message> selectSystemNotifications(int userId, String topic, int offset, int limit);
}
