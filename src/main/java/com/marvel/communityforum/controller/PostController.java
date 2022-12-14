package com.marvel.communityforum.controller;

import com.marvel.communityforum.annotation.LoginRequired;
import com.marvel.communityforum.entity.Comment;
import com.marvel.communityforum.entity.Post;
import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.service.CommentService;
import com.marvel.communityforum.service.PostService;
import com.marvel.communityforum.service.UserService;
import com.marvel.communityforum.util.CommunityConstant;
import com.marvel.communityforum.util.CommunityUtil;
import com.marvel.communityforum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/post")
public class PostController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

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
    public Map<String, Object> getPostDetailById(@PathVariable("postId") int postId,
                                                 @RequestParam(name = "current", defaultValue = "1") int current,
                                                 @RequestParam(name = "limit", defaultValue = "5") int limit) {
        Map<String, Object> postMap = new HashMap<>();

        Post post = postService.getPostById(postId);
        User user = userService.getUserById(post.getUserId());
        postMap.put("post", post);
        postMap.put("post author", user);

        List<Comment> commentsUnderPost = commentService.getCommentBySubject(COMMENT_SUBJECT_TYPE_POST, postId,
                CommunityUtil.getOffset(current, limit), limit);
        List<Map<String, Object>> commentVOListUnderPost = new ArrayList<>();
        if (commentsUnderPost != null) {
            for (Comment comment : commentsUnderPost) {
                Map<String, Object> commentUnderPostVO = new HashMap<>();
                commentUnderPostVO.put("comment", comment);
                commentUnderPostVO.put("comment author", userService.getUserById(comment.getUserId()));

                List<Comment> commentsUnderComment = commentService.getCommentBySubject(COMMENT_SUBJECT_TYPE_COMMENT, comment.getId(),
                        0, Integer.MAX_VALUE);
                List<Map<String, Object>> replyVOList = new ArrayList<>();
                if (commentsUnderComment != null) {
                    for (Comment reply : commentsUnderComment) {
                        Map<String, Object> replyVO = new HashMap<>();
                        replyVO.put("reply", reply);
                        replyVO.put("reply author", userService.getUserById(reply.getUserId()));

                        User replyTargetUser = userService.getUserById(reply.getTargetId());
                        replyVO.put("reply target user", replyTargetUser);

                        replyVOList.add(replyVO);
                    }
                }
                commentUnderPostVO.put("reply list", replyVOList);

                int replyCount = commentService.getCommentCountBySubject(COMMENT_SUBJECT_TYPE_COMMENT, comment.getId());
                commentUnderPostVO.put("reply count", replyCount);

                commentVOListUnderPost.add(commentUnderPostVO);
            }
        }
        postMap.put("comments", commentVOListUnderPost);

        return postMap;
    }
}
