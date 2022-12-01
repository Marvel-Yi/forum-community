package com.marvel.communityforum.service;

import com.marvel.communityforum.dao.UserMapper;
import com.marvel.communityforum.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User getUserById(int id) {
        return userMapper.selectById(id);
    }
}
