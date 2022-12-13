package com.marvel.communityforum.controller;

import com.marvel.communityforum.annotation.LoginRequired;
import com.marvel.communityforum.entity.Post;
import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.service.PostService;
import com.marvel.communityforum.util.CommunityUtil;
import com.marvel.communityforum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/post")
public class PostController {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private PostService postService;

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
}
