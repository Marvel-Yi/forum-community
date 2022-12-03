package com.marvel.communityforum.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MailClientTest {
    @Autowired
    private MailClient mailClient;

    @Test
    public void sendTest() {
        mailClient.send("", "test", "test");
    }
}
