package com.marvel.communityforum.service;

import com.marvel.communityforum.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class FollowService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void follow(int userId, int subjectType, int subjectId) {
        String fansKey = RedisKeyUtil.getFansKey(subjectType, subjectId);
        String followKey = RedisKeyUtil.getFollowKey(userId, subjectType);

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();

                operations.opsForZSet().add(fansKey, userId, System.currentTimeMillis());
                operations.opsForZSet().add(followKey, subjectId, System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    public void unfollow(int userId, int subjectType, int subjectId) {
        String fansKey = RedisKeyUtil.getFansKey(subjectType, subjectId);
        String followKey = RedisKeyUtil.getFollowKey(userId, subjectType);

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();

                operations.opsForZSet().remove(fansKey, userId);
                operations.opsForZSet().remove(followKey, subjectId);

                return operations.exec();
            }
        });
    }

    public long getFansCount(int subjectType, int subjectId) {
        String fansKey = RedisKeyUtil.getFansKey(subjectType, subjectId);
        return redisTemplate.opsForZSet().zCard(fansKey);
    }

    public long getFollowCount(int userId, int subjectType) {
        String followKey = RedisKeyUtil.getFollowKey(userId, subjectType);
        return redisTemplate.opsForZSet().zCard(followKey);
    }

    public boolean hasFollowed(int userId, int subjectType, int subjectId) {
        String followKey = RedisKeyUtil.getFollowKey(userId, subjectType);
        return redisTemplate.opsForZSet().score(followKey, subjectId) != null;
    }
}
