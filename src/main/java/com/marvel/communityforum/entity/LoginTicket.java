package com.marvel.communityforum.entity;

import lombok.Data;

import java.util.Date;

@Data
public class LoginTicket {
    private int id;
    private int userId;
    private String ticket;
    private int status; // 0 means login, 1 means logout
    private Date expired;
}
