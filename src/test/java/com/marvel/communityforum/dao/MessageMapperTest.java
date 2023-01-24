package com.marvel.communityforum.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MessageMapperTest {
    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void selectConversationLatestMessageTest() {
        System.out.println(messageMapper.selectConversationLatestMessage(8, 1, 3));
    }

    @Test
    public void selectConversationCountTest() {
        System.out.println(messageMapper.selectConversationCount(8));
    }

    @Test
    public void selectConversationMessagesTest() {
        System.out.println(messageMapper.selectConversationMessages("8-9", 0, 10));
    }

    @Test
    public void selectConversationMessageCountTest() {
        System.out.println(messageMapper.selectConversationMessageCount("8-9"));
    }

    @Test
    public void selectUnreadMessageCountTest() {
        System.out.println(messageMapper.selectUnreadMessageCount(8));
        System.out.println(messageMapper.selectUnreadMessageCount(9));
    }

    @Test
    public void selectConversationUnreadMessageCountTest() {
        System.out.println(messageMapper.selectConversationUnreadMessageCount(8, "8-9"));
        System.out.println(messageMapper.selectConversationUnreadMessageCount(9, "2-9"));
    }
}
