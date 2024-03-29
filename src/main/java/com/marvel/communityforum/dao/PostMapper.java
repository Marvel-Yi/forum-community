package com.marvel.communityforum.dao;

import com.marvel.communityforum.entity.Post;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostMapper {
    List<Post> selectAllPosts(int offset, int limit);

    List<Post> selectAllPostsOrderByScores(int offset, int limit);

    int selectAllPostCount();

    List<Post> selectUserPosts(int userId, int offset, int limit);

    int selectUserPostCount(int userId);

    int insertPost(Post post);

    Post selectPostById(int id);

    int updateCommentCount(int postId, int commentCount);

    int updateScore(int postId, double score);

    int updateStatus(int postId, int status);

    int updateType(int postId, int type);
}
