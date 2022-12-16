package com.marvel.communityforum.service;

import com.marvel.communityforum.dao.CommentMapper;
import com.marvel.communityforum.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;

    public List<Comment> getCommentBySubject(int subjectType, int subjectId, int offset, int limit) {
        return commentMapper.selectBySubject(subjectType, subjectId, offset, limit);
    }

    public int getCommentCountBySubject(int subjectType, int subjectId) {
        return commentMapper.selectCountBySubject(subjectType, subjectId);
    }
}
