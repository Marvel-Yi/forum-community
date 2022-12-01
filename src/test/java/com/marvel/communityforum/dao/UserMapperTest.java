package com.marvel.communityforum.dao;

import com.marvel.communityforum.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
public class UserMapperTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    public void selectUserByIdTest() {
        User user = userMapper.selectById(1);
        System.out.println(user);
    }

    @Test
    public void selectUserByNameTest() {
        User user = userMapper.selectByName("zhangsan");
        System.out.println(user);
    }

    @Test
    public void selectUserByEmailTest() {
        User user = userMapper.selectByEmail("111@qq.com");
        System.out.println(user);
    }

    @Test
    public void insertUserTest() {
        User user = new User();
        user.setUserName("zhangsan");
        user.setPassword("123");
        user.setEmail("111@qq.com");
        user.setUserType(0);
        user.setStatus(0);
        user.setCreateTime(new Date());

        int res = userMapper.insertUser(user);
        System.out.println(res);
        System.out.println(user.getId());
    }

    @Test
    public void updateStatusTest() {
        int res = userMapper.updateStatus(1, 1);
        System.out.println(res);
        System.out.println(userMapper.selectById(1).getStatus());
    }

    @Test
    public void updatePasswordTest() {
        int res = userMapper.updatePassword(1, "321");
        System.out.println(res);
        System.out.println(userMapper.selectById(1).getPassword());
    }
}
