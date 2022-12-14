package com.marvel.communityforum.controller;

import com.marvel.communityforum.entity.Post;
import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.service.PostService;
import com.marvel.communityforum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class HomeController {
    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @GetMapping("/index")
    public List<Map<String, Object>> getIndexPage(@RequestParam(name = "current", defaultValue = "1") int current,
                                                  @RequestParam(name = "limit", defaultValue = "5") int limit) {
        List<Post> postList = postService.getAllPost(current, limit);
        List<Map<String, Object>> userPostMapList = new ArrayList<>();
        for (Post post : postList) {
            User user = userService.getUserById(post.getUserId());
            Map<String, Object> userPostMap = new HashMap<>();
            userPostMap.put("post", post);
            userPostMap.put("post author", user);
            userPostMapList.add(userPostMap);
        }
        return userPostMapList;
    }
}
