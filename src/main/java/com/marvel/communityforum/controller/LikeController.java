package com.marvel.communityforum.controller;

import com.marvel.communityforum.annotation.LoginRequired;
import com.marvel.communityforum.entity.Event;
import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.event.EventProducer;
import com.marvel.communityforum.service.LikeService;
import com.marvel.communityforum.util.CommunityConstant;
import com.marvel.communityforum.util.CommunityUtil;
import com.marvel.communityforum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LikeController implements CommunityConstant {
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @PostMapping("/like")
    public String like(int subjectType, int subjectId, int subjectAuthorId, int postId) {
        User user = hostHolder.getUser();

        likeService.like(user.getId(), subjectType, subjectId, subjectAuthorId);
        long likeCount = likeService.getLikeCount(subjectType, subjectId);
        int likeStatus = likeService.getLikeStatus(user.getId(), subjectType, subjectId);

        if (likeStatus == 1) {
            Event event = new Event();
            event.setTopic(TOPIC_LIKE)
                    .setUserId(user.getId())
                    .setSubjectType(subjectType)
                    .setSubjectId(subjectId)
                    .setSubjectUserId(subjectAuthorId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("like count", likeCount);
        map.put("like status", likeStatus);
        return CommunityUtil.getJSONString(0, null, map);
    }
}
