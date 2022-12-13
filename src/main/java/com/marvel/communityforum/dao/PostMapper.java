package com.marvel.communityforum.dao;

import com.marvel.communityforum.entity.Post;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostMapper {
    List<Post> selectAllPosts(int offset, int limit);

    int selectAllPostCount();

    List<Post> selectUserPosts(int userId, int offset, int limit);

    int selectUserPostCount(int userId);

    int insertPost(Post post);
}
