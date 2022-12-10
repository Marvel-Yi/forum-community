package com.marvel.communityforum.controller;

import com.marvel.communityforum.annotation.LoginRequired;
import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.service.UserService;
import com.marvel.communityforum.util.CommunityConstant;
import com.marvel.communityforum.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @LoginRequired
    @PostMapping("/modify/password")
    public String modifyPassword(String originPassword, String newPassword, String repeatPassword) {
        User user = hostHolder.getUser();
        int result = userService.modifyPassword(user, originPassword, newPassword, repeatPassword);
        if (result == LOGIN_STATUS_EXPIRED) {
            return "login status expired, please login";
        }
        if (result == PASSWORD_BLANK) {
            return "password is blank, please enter password";
        }
        if (result == PASSWORD_INCORRECT) {
            return "password is incorrect, please check your password";
        }
        if (result == PASSWORD_REPEAT_INCORRECT) {
            return "new password confirmation failed, please repeat new password";
        }
        return "password modify succeed";
    }
}
