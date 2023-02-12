package com.marvel.communityforum.controller;

import com.marvel.communityforum.annotation.LoginRequired;
import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.service.FollowService;
import com.marvel.communityforum.util.CommunityUtil;
import com.marvel.communityforum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FollowController {
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

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
}
