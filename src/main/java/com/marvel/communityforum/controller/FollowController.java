package com.marvel.communityforum.controller;

import com.marvel.communityforum.annotation.LoginRequired;
import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.service.FollowService;
import com.marvel.communityforum.service.UserService;
import com.marvel.communityforum.util.CommunityConstant;
import com.marvel.communityforum.util.CommunityUtil;
import com.marvel.communityforum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FollowController implements CommunityConstant {
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @LoginRequired
    @PostMapping("/follow")
    public String follow(int subjectType, int subjectId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), subjectType, subjectId);
        return CommunityUtil.getJSONString(0, "follow succeeded");
    }

    @LoginRequired
    @PostMapping("/unfollow")
    public String unfollow(int subjectType, int subjectId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), subjectType, subjectId);
        return CommunityUtil.getJSONString(0, "unfollow succeeded");
    }

    @GetMapping("/follow/list/{userId}")
    public Map<String, Object> getFollowPeople(@PathVariable("userId") int userId,
                                               @RequestParam(name = "current", defaultValue = "1") int current,
                                               @RequestParam(name = "limit", defaultValue = "10") int limit) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("queried user not exist");
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("user", user);

        List<Map<String, Object>> followList = followService.getFollowList(user.getId(), CommunityUtil.getOffset(current, limit), limit);
        if (followList != null) {
            for (Map<String, Object> followPersonMap : followList) {
                User followPerson = (User) followPersonMap.get("follow user");
                followPersonMap.put("has followed", hasFollowed(followPerson.getId()));
            }
        }
        resultMap.put("follow list", followList);

        return resultMap;
    }

    @GetMapping("/fans/list/{userId}")
    public Map<String, Object> getFans(@PathVariable("userId") int userId,
                                       @RequestParam(name = "current", defaultValue = "1") int current,
                                       @RequestParam(name = "limit", defaultValue = "10") int limit) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("queried user not exist");
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("user", user);

        List<Map<String, Object>> fansList = followService.getFansList(user.getId(), CommunityUtil.getOffset(current, limit), limit);
        if (fansList != null) {
            for (Map<String, Object> fanMap : fansList) {
                User fan = (User) fanMap.get("fan");
                fanMap.put("has followed", hasFollowed(fan.getId()));
            }
        }
        resultMap.put("fan list", fansList);

        return resultMap;
    }

    private boolean hasFollowed(int userId) {
        User currentUser = hostHolder.getUser();
        return currentUser != null && followService.hasFollowed(currentUser.getId(), SUBJECT_TYPE_USER, userId);
    }
}
