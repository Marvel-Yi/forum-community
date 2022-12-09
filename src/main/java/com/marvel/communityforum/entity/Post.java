package com.marvel.communityforum.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Post {
    private int id;
    private int userId;
    private String title;
    private String content;
    private int postType; // 0 ordinary post, 1 top post
    private int postStatus; // 0 normal, 1 essential, 2 off the shelf
    private Date createTime;
    private int commentCount;
    private double score; // degree of popularity
}
