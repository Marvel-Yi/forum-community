package com.marvel.communityforum.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.marvel.communityforum.dao.PostMapper;
import com.marvel.communityforum.entity.Post;
import com.marvel.communityforum.util.CommunityUtil;
import com.marvel.communityforum.util.RedisKeyUtil;
import com.marvel.communityforum.util.SensitiveWordFilterTrie;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    @Value("${caffeine.hotpost.max-size}")
    private int caffeineMaxSize;

    @Value("${caffeine.hotpost.expire-seconds}")
    private int caffeineExpireSeconds;

    private LoadingCache<String, List<Post>> hotPostListCache;

    private LoadingCache<String, Integer> allPostCountCache;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private SensitiveWordFilterTrie sensitiveWordFilter;

    @PostConstruct
    public void init() {
        hotPostListCache = Caffeine.newBuilder()
                .maximumSize(caffeineMaxSize)
                .expireAfterWrite(caffeineExpireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<Post>>() {
                    @Override
                    public @Nullable List<Post> load(String key) throws Exception {
                        logger.debug("hot post caffeine cache missing");
                        return getHotPostFromRedis(key);
                    }
                });

        allPostCountCache = Caffeine.newBuilder()
                .maximumSize(caffeineMaxSize)
                .expireAfterWrite(caffeineExpireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public @Nullable Integer load(String key) throws Exception {
                        return getAllPostCountFromRedis(key);
                    }
                });
    }

    public List<Post> getAllPost(int current, int limit) {
        return postMapper.selectAllPosts(CommunityUtil.getOffset(current, limit), limit);
    }

    public List<Post> getAllPostOrderByScore(int current, int limit) {
        String hotPostCacheKey = RedisKeyUtil.getHotPostKey(CommunityUtil.getOffset(current, limit), limit);
        return hotPostListCache.get(hotPostCacheKey);
    }

    private List<Post> getHotPostFromRedis(String hotPostKey) {
        List<Post> hotPosts = (List<Post>) redisTemplate.opsForValue().get(hotPostKey);
        if (hotPosts == null) {
            logger.debug("hot post redis cache missing");
            String[] params = hotPostKey.split(RedisKeyUtil.SEPARATOR);
            int offset = Integer.parseInt(params[2]);
            int limit = Integer.parseInt(params[3]);
            hotPosts = postMapper.selectAllPostsOrderByScores(offset, limit);
            redisTemplate.opsForValue().set(hotPostKey, hotPosts, 30, TimeUnit.MINUTES);
        } else {
            logger.debug("hot post caffeine cache missing, redis cache hit");
        }
        return hotPosts;
    }

    public int getAllPostCount() {
        String allPostCountKey = RedisKeyUtil.getPostTotalCountKey();
        return allPostCountCache.get(allPostCountKey);
    }

    private int getAllPostCountFromRedis(String key) {
        Integer totalCount = (Integer) redisTemplate.opsForValue().get(key);
        if (totalCount == null) {
            logger.debug("total post count redis cache missing");
            totalCount = postMapper.selectAllPostCount();
            redisTemplate.opsForValue().set(key, totalCount);
        }
        return totalCount;
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

        int res = postMapper.insertPost(post);
        redisTemplate.delete(RedisKeyUtil.getPostTotalCountKey());
        return res;
    }

    public Post getPostById(int id) {
        return postMapper.selectPostById(id);
    }

    public int updateCommentCount(int postId, int commentCount) {
        return postMapper.updateCommentCount(postId, commentCount);
    }

    public int updateScore(int postId, double score) {
        return postMapper.updateScore(postId, score);
    }

    public int updateStatus(int postId, int status) {
        return postMapper.updateStatus(postId, status);
    }

    public int updateType(int postId, int type) {
        return postMapper.updateType(postId, type);
    }
}