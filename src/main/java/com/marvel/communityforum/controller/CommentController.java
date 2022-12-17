package com.marvel.communityforum.controller;

import com.marvel.communityforum.annotation.LoginRequired;
import com.marvel.communityforum.entity.Comment;
import com.marvel.communityforum.service.CommentService;
import com.marvel.communityforum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @PostMapping("/publish/{postId}")
    public String publishComment(@RequestBody Comment comment, @PathVariable("postId") int postId, HttpServletResponse response) throws Exception {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date(System.currentTimeMillis()));
        commentService.addComment(comment);
        response.sendRedirect("/post/detail/" + postId);
        return "comment published";
    }
}
