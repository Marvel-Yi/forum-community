package com.marvel.communityforum.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Message {
    private int id;
    private int fromId;
    private int toId;
    private String conversationId;
    private String content;
    private int status; // 0 unread, 1 read, 2 deleted
    private Date createTime;
}
