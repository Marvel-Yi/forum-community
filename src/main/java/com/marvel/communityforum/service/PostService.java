package com.marvel.communityforum.service;

import com.marvel.communityforum.dao.PostMapper;
import com.marvel.communityforum.entity.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostMapper postMapper;

    public List<Post> getAllPost(int current, int limit) {
        return postMapper.selectAllPosts(getOffset(current, limit), limit);
    }

    public int getAllPostCount() {
        return postMapper.selectAllPostCount();
    }

    public List<Post> getUserPost(int userId, int current, int limit) {
        return postMapper.selectUserPosts(userId, getOffset(current, limit), limit);
    }

    public int getUserPostCount(int userId) {
        return postMapper.selectUserPostCount(userId);
    }

    public int getOffset(int current, int limit) {
        return (current - 1) * limit;
    }

    public int getPageCount(int limit) {
        int postCount = postMapper.selectAllPostCount();
        return (postCount + limit - 1) / limit;
    }
}
