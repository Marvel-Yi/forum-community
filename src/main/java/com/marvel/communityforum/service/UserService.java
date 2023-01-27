package com.marvel.communityforum.service;

import com.marvel.communityforum.dao.LoginTicketMapper;
import com.marvel.communityforum.dao.UserMapper;
import com.marvel.communityforum.entity.LoginTicket;
import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.util.CommunityConstant;
import com.marvel.communityforum.util.CommunityUtil;
import com.marvel.communityforum.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${community.domain}")
    private String domain;

    private String activationSubject = "Activate Your Account";
    private String activationText = ", welcome to Evan's Project, please activate your account through URL: ";

    public User getUserById(int id) {
        return userMapper.selectById(id);
    }

    public User getUserByName(String name) {
        return userMapper.selectByName(name);
    }

    public Map<String, Object> reigster(User user) {
        Map<String, Object> map = new HashMap<>();

        // null or blank
        if (user == null) {
            throw new IllegalArgumentException("user is null"); // runtime exception
        }
        if (StringUtils.isBlank(user.getUserName())) {
            map.put("usernameMsg", "username is blank");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "password is blank");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "email is blank");
            return map;
        }

        User u = userMapper.selectByName(user.getUserName());
        if (u != null) {
            map.put("usernameMsg", "username already exists");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "email already exists");
            return map;
        }

        user.setPassword(CommunityUtil.md5(user.getPassword()));
        user.setUserType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // send an email for activation
        String greeting = "Hi " + user.getUserName();
        String URL = new StringBuilder(domain).append("/activation/").append(user.getId()).append("/").append(user.getActivationCode()).toString();
        mailClient.send(user.getEmail(), activationSubject, greeting + activationText + URL);

        map.put("reigsterSuccessfulMsg", URL);
        return map;
    }

    public int activation(int userId, String activationCode) {
        User u = userMapper.selectById(userId);

        if (u.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        }
        if (u.getActivationCode().equals(activationCode)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        }
        return ACTIVATION_FAILURE;
    }

    public Map<String, Object> login(String userName, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isBlank(userName)) {
            map.put("userNameMsg", "user name is blank");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "password is blank");
            return map;
        }

        // validation
        User user = userMapper.selectByName(userName);
        if (user == null) {
            map.put("userNameMsg", "user name does not exist");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("userNameMsg", "user has not been activated");
            return map;
        }
        if (!user.getPassword().equals(CommunityUtil.md5(password))) {
            map.put("passwordMsg", "incorrect password");
            return map;
        }

        // generate login ticket
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0); // 0 means status login
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertTicket(loginTicket);

        map.put("loginTicketMsg", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1); // 1 means status logout
    }

    public LoginTicket getLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int modifyPassword(User user, String originPassword, String newPassword, String repeatPassword) {
        if (user == null) {
            return LOGIN_STATUS_EXPIRED;
        }
        if (StringUtils.isBlank(originPassword) || StringUtils.isBlank(newPassword) || StringUtils.isBlank(repeatPassword)) {
            return PASSWORD_BLANK;
        }
        if (!user.getPassword().equals(CommunityUtil.md5(originPassword))) {
            return PASSWORD_INCORRECT;
        }
        if (!newPassword.equals(repeatPassword)) {
            return PASSWORD_REPEAT_INCORRECT;
        }

        userMapper.updatePassword(user.getId(), CommunityUtil.md5(newPassword));
        return PASSWORD_MODIFY_SUCCESS;
    }
}
