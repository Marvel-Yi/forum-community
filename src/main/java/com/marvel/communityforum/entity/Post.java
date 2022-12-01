package com.marvel.communityforum.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Post {
    private int id;
    private int userId;
    private String title;
    private String content;
    private int postType;
    private int postStatus;
    private Date createTime;
    private int commentCount;
    private double score;
}
