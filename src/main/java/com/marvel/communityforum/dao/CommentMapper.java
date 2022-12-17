package com.marvel.communityforum.dao;

import com.marvel.communityforum.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> selectBySubject(int subjectType, int subjectId, int offset, int limit);

    int selectCountBySubject(int subjectType, int subjectId);

    int insertComment(Comment comment);
}
