package com.marvel.communityforum.service;

import com.marvel.communityforum.dao.PostMapper;
import com.marvel.communityforum.entity.Post;
import com.marvel.communityforum.util.CommunityUtil;
import com.marvel.communityforum.util.SensitiveWordFilterTrie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostMapper postMapper;

    @Autowired
    private SensitiveWordFilterTrie sensitiveWordFilter;

    public List<Post> getAllPost(int current, int limit) {
        return postMapper.selectAllPosts(CommunityUtil.getOffset(current, limit), limit);
    }

    public int getAllPostCount() {
        return postMapper.selectAllPostCount();
    }

    public List<Post> getUserPost(int userId, int current, int limit) {
        return postMapper.selectUserPosts(userId, CommunityUtil.getOffset(current, limit), limit);
    }

    public int getUserPostCount(int userId) {
        return postMapper.selectUserPostCount(userId);
    }

    public int addPost(Post post) {
        if (post == null) {
            throw new IllegalArgumentException("post is null");
        }

        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        post.setTitle(sensitiveWordFilter.filter(post.getTitle()));
        post.setContent(sensitiveWordFilter.filter(post.getContent()));

        return postMapper.insertPost(post);
    }

    public Post getPostById(int id) {
        return postMapper.selectPostById(id);
    }
}