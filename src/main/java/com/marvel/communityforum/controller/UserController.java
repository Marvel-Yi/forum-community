package com.marvel.communityforum.controller;

import com.marvel.communityforum.annotation.LoginRequired;
import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.service.FollowService;
import com.marvel.communityforum.service.LikeService;
import com.marvel.communityforum.service.UserService;
import com.marvel.communityforum.util.CommunityConstant;
import com.marvel.communityforum.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

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

    @GetMapping("/profile/{userId}")
    public Map<String, Object> getProfilePage(@PathVariable("userId") int userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("user not exist, unable to visit user profile");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("user", user);

        int likeCount = likeService.getUserLikeCount(userId);
        map.put("user like count", likeCount);

        long followCount = followService.getFollowCount(userId, SUBJECT_TYPE_USER);
        map.put("follow count", followCount);

        long fansCount = followService.getFansCount(SUBJECT_TYPE_USER, userId);
        map.put("fans count", fansCount);

        boolean hasFollowed = hostHolder.getUser() != null && followService.hasFollowed(hostHolder.getUser().getId(), SUBJECT_TYPE_USER, userId);
        map.put("has followed", hasFollowed);

        return map;
    }
}
