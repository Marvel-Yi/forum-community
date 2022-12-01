package com.marvel.communityforum.dao;

import com.marvel.communityforum.entity.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class PostMapperTest {
    @Autowired
    private PostMapper postMapper;

    @Test
    public void selectAllPostsTest() {
        int limit = 5;
        int page = 1;
        int offset = (page - 1) * limit;
        List<Post> postList = postMapper.selectAllPosts(offset, limit);
        for (Post post : postList) {
            System.out.println(post);
        }
    }

    @Test
    public void selectAllPostCountTest() {
        int count = postMapper.selectAllPostCount();
        System.out.println(count);
    }

    @Test
    public void selectUserPostsTest() {
        int limit = 5;
        int page = 2;
        int offset = (page - 1) * limit;
        int userId = 1;
        List<Post> userPostList = postMapper.selectUserPosts(userId, offset, limit);
        for (Post post : userPostList) {
            System.out.println(post);
        }
    }

    @Test
    public void selectUserPostCountTest() {
        int userId = 1;
        int count = postMapper.selectUserPostCount(userId);
        System.out.println(count);
    }
}
