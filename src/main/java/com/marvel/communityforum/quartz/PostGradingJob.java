package com.marvel.communityforum.quartz;

import com.marvel.communityforum.entity.Post;
import com.marvel.communityforum.service.LikeService;
import com.marvel.communityforum.service.PostService;
import com.marvel.communityforum.util.CommunityConstant;
import com.marvel.communityforum.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostGradingJob implements Job, CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(PostGradingJob.class);

    private static final Date DATE_OF_ESTABLISHMENT;

    static {
        try {
            DATE_OF_ESTABLISHMENT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("initializing date of project establishment failed", e);
        }
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PostService postService;

    @Autowired
    private LikeService likeService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        BoundSetOperations boundSetOps = redisTemplate.boundSetOps(RedisKeyUtil.getPostForGradingKey());
        if (boundSetOps.size() == 0) {
            logger.info("there is no post for grading");
            return;
        }

        logger.info("post grading job starts, and " + boundSetOps.size() + " posts to be done");
        while (boundSetOps.size() > 0) {
            int postId = (int) boundSetOps.pop();
            grading(postId);
        }
        logger.info("post grading job completed");
    }

    private void grading(int postId) {
        Post post = postService.getPostById(postId);
        if (post == null) {
            logger.info("post not exist, post id: " + postId);
            return;
        }

        int essenceScore = post.getPostType() == POST_TYPE_ESSENCE ? 50 : 0;
        int commentCnt = post.getCommentCount();
        long likeCnt = likeService.getLikeCount(COMMENT_SUBJECT_TYPE_POST, postId);
        double score = Math.log10(Math.max(essenceScore + commentCnt * 5 + likeCnt * 3, 1))
                + (post.getCreateTime().getTime() - DATE_OF_ESTABLISHMENT.getTime()) / (1000 * 3600 * 24);

        postService.updateScore(postId, score);

        // delete hot posts from redis cache as grading job may update their rankings
        deleteHotPostRedisCache();
    }

    private void deleteHotPostRedisCache() {
        Long delCount = (Long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                ScanOptions scanOptions = ScanOptions.scanOptions()
                        .match(RedisKeyUtil.PREFIX_HOT_POST + RedisKeyUtil.SEPARATOR + "*")
                        .count(5)
                        .build();
                Cursor<byte[]> cursor = connection.scan(scanOptions);
                long delCnt = 0;
                while (cursor.hasNext()) {
                    connection.del(cursor.next());
                    delCnt++;
                }
                return delCnt;
            }
        });
        logger.debug("number of hot post redis key deleted: " + delCount);
    }
}
