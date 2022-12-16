package com.marvel.communityforum.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Comment {
    private int id;
    private int userId;
    private int subjectType; // 0 means comment on post, 1 means reply to comment
    private int subjectId; // post id or comment id
    private int targetId; // id of user who is replied while commenting on the comment of the post
    private String content;
    private int status; // 0 normal, 1 deleted
    private Date createTime;
}
