package com.marvel.communityforum.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private int id;
    private String userName;
    private String password;
    private String email;
    private int userType; // 0 ordinary user，1 owner of section, 2 administrator
    private int status; // 0 not activated, 1 activated
    private String activationCode;
    private Date createTime;
}
