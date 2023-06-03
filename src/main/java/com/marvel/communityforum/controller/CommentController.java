package com.marvel.communityforum.controller;

import com.marvel.communityforum.annotation.LoginRequired;
import com.marvel.communityforum.entity.Comment;
import com.marvel.communityforum.entity.Event;
import com.marvel.communityforum.event.EventProducer;
import com.marvel.communityforum.service.CommentService;
import com.marvel.communityforum.service.PostService;
import com.marvel.communityforum.util.CommunityConstant;
import com.marvel.communityforum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@RestController
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private PostService postService;

    @LoginRequired
    @PostMapping("/publish/{postId}")
    public String publishComment(@RequestBody Comment comment, @PathVariable("postId") int postId, HttpServletResponse response) throws Exception {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date(System.currentTimeMillis()));
        commentService.addComment(comment);

        Event event = new Event();
        event.setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setSubjectType(comment.getSubjectType())
                .setSubjectId(comment.getSubjectId())
                .setData("postId", postId);
        int subjectUserId = 0;
        if (comment.getSubjectType() == COMMENT_SUBJECT_TYPE_POST) {
            subjectUserId = postService.getPostById(postId).getUserId();
        } else if (comment.getSubjectType() == COMMENT_SUBJECT_TYPE_COMMENT) {
            subjectUserId = comment.getTargetId();
        }
        event.setSubjectUserId(subjectUserId);
        eventProducer.fireEvent(event);

        response.sendRedirect("/post/detail/" + postId);
        return "comment published";
    }
}
