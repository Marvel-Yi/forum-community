package com.marvel.communityforum.dao;

import com.marvel.communityforum.entity.LoginTicket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
public class LoginTicketMapperTest {
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void insertTicketTest() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(999);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10)); // 10 min
        loginTicketMapper.insertTicket(loginTicket);
    }

    @Test
    public void selectByTicketTest() {
        System.out.println(loginTicketMapper.selectByTicket("abc"));
    }

    @Test
    public void updateStatusTest() {
        loginTicketMapper.updateStatus("abc", 1);
        System.out.println(loginTicketMapper.selectByTicket("abc"));
    }
}
