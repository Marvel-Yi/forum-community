package com.marvel.communityforum.service;

import com.marvel.communityforum.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void like(int userId, int subjectType, int subjectId) {
        String likeKey = RedisKeyUtil.getLikeKeyPrefix(subjectType, subjectId);
        if (redisTemplate.opsForSet().isMember(likeKey, userId)) {
            redisTemplate.opsForSet().remove(likeKey, userId);
        } else {
            redisTemplate.opsForSet().add(likeKey, userId);
        }
    }

    public long getLikeCount(int subjectType, int subjectId) {
        String likeKey = RedisKeyUtil.getLikeKeyPrefix(subjectType, subjectId);
        return redisTemplate.opsForSet().size(likeKey);
    }

    public int getLikeStatus(int userId, int subjectType, int subjectId) {
        String likeKey = RedisKeyUtil.getLikeKeyPrefix(subjectType, subjectId);
        return redisTemplate.opsForSet().isMember(likeKey, userId) ? 1 : 0;
    }
}
