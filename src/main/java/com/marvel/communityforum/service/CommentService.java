package com.marvel.communityforum.service;

import com.marvel.communityforum.dao.CommentMapper;
import com.marvel.communityforum.entity.Comment;
import com.marvel.communityforum.util.CommunityConstant;
import com.marvel.communityforum.util.SensitiveWordFilterTrie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveWordFilterTrie sensitiveWordFilter;

    @Autowired
    private PostService postService;

    public List<Comment> getCommentBySubject(int subjectType, int subjectId, int offset, int limit) {
        return commentMapper.selectBySubject(subjectType, subjectId, offset, limit);
    }

    public int getCommentCountBySubject(int subjectType, int subjectId) {
        return commentMapper.selectCountBySubject(subjectType, subjectId);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("comment is null");
        }

        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveWordFilter.filter(comment.getContent()));

        int rows = commentMapper.insertComment(comment);

        if (comment.getSubjectType() == COMMENT_SUBJECT_TYPE_POST) {
            int postId = comment.getSubjectId();
            int commentCount = commentMapper.selectCountBySubject(COMMENT_SUBJECT_TYPE_POST, postId);
            postService.updateCommentCount(postId, commentCount);
        }

        return rows;
    }
}
