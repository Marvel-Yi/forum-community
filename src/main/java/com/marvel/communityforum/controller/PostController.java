package com.marvel.communityforum.controller;

import com.marvel.communityforum.annotation.LoginRequired;
import com.marvel.communityforum.entity.Post;
import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.service.PostService;
import com.marvel.communityforum.service.UserService;
import com.marvel.communityforum.util.CommunityUtil;
import com.marvel.communityforum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/post")
public class PostController {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @LoginRequired
    @PostMapping("/publish")
    public String publishPost(String title, String content) {
        User user = hostHolder.getUser();
        Post post = new Post();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date(System.currentTimeMillis()));
        postService.addPost(post);

        return CommunityUtil.getJSONString(0, "publish succeeded");
    }

    @GetMapping("/detail/{postId}")
    public Map<String, Object> getPostById(@PathVariable("postId") int postId) {
        Map<String, Object> postMap = new HashMap<>();
        Post post = postService.getPostById(postId);
        User user = userService.getUserById(post.getUserId());
        postMap.put("post", post);
        postMap.put("user", user);
        return postMap;
    }
}
