package com.marvel.communityforum.controller;

import com.marvel.communityforum.entity.Post;
import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.service.LikeService;
import com.marvel.communityforum.service.PostService;
import com.marvel.communityforum.service.UserService;
import com.marvel.communityforum.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class HomeController implements CommunityConstant {
    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;
    @Autowired
    private LikeService likeService;

    @GetMapping("/index")
    public List<Map<String, Object>> getIndexPage(@RequestParam(name = "current", defaultValue = "1") int current,
                                                  @RequestParam(name = "limit", defaultValue = "5") int limit,
                                                  @RequestParam(name = "sortingMode", defaultValue = "0") int sortingMode) {
        List<Post> postList = null;
        if (sortingMode == 0) {
            postList = postService.getAllPost(current, limit);
        } else if (sortingMode == 1) {
            postList = postService.getAllPostOrderByScore(current, limit);
        }
        List<Map<String, Object>> userPostMapList = new ArrayList<>();
        for (Post post : postList) {
            User user = userService.getUserById(post.getUserId());
            long likeCount = likeService.getLikeCount(COMMENT_SUBJECT_TYPE_POST, post.getId());
            Map<String, Object> userPostMap = new HashMap<>();
            userPostMap.put("post", post);
            userPostMap.put("post author", user);
            userPostMap.put("like count", likeCount);
            userPostMapList.add(userPostMap);
        }
        return userPostMapList;
    }

    @GetMapping("/error")
    public String getErrorPage() {
        return "500, server error";
    }
}
