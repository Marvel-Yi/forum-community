package com.marvel.communityforum.dao;

import com.marvel.communityforum.entity.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
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

    @Test
    public void insertPostTest() {
        Post post = new Post();
        post.setUserId(8);
        post.setTitle("平平无奇");
        post.setContent("凭借着阿斯麦过硬的招牌以及自己臭不要脸的吹牛，提桶之后很快就面试进了一家半导体日企，正社员(划重点)。在这终于有了渴望已久的归属感。这人啊，有了奔头之后干劲都足了，刚入职那会每天培训完写学习报告写到晚上九十点也不觉得累。后面出差去现场工作也经常加班到半夜，月均八九十个小时的加班，但是没有丝毫怨言(有加班费hhhh)，毕竟自己一个带专生终于也可以亲自去操作这些先进的设备，可以实现自己的价值，现在回想起来真是充实的岁月，这份工作也是做了最久的，将近三年。");
        post.setPostType(0);
        post.setPostStatus(0);
        post.setCreateTime(new Date(System.currentTimeMillis()));
        post.setCommentCount(0);
        post.setScore(0);
        System.out.println(postMapper.insertPost(post));
    }

    @Test
    public void batchInsertPost() {
        for (int i = 0; i < 200000; i++) {
            Post post = new Post();
            post.setUserId(10);
            post.setTitle("Stress Test Post " + i);
            post.setContent("压力测试贴");
            post.setCreateTime(new Date(System.currentTimeMillis()));
            postMapper.insertPost(post);
        }
    }
}
