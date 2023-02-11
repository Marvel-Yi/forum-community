package com.marvel.communityforum.controller;

import com.marvel.communityforum.annotation.LoginRequired;
import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.service.LikeService;
import com.marvel.communityforum.util.CommunityUtil;
import com.marvel.communityforum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LikeController {
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @PostMapping("/like")
    public String like(int subjectType, int subjectId, int subjectAuthorId) {
        User user = hostHolder.getUser();

        likeService.like(user.getId(), subjectType, subjectId, subjectAuthorId);
        long likeCount = likeService.getLikeCount(subjectType, subjectId);
        int likeStatus = likeService.getLikeStatus(user.getId(), subjectType, subjectId);

        Map<String, Object> map = new HashMap<>();
        map.put("like count", likeCount);
        map.put("like status", likeStatus);
        return CommunityUtil.getJSONString(0, null, map);
    }
}
