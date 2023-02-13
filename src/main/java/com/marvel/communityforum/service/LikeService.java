package com.marvel.communityforum.service;

import com.marvel.communityforum.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    public void like(int userId, int subjectType, int subjectId, int subjectAuthorId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String likeKey = RedisKeyUtil.getLikeKey(subjectType, subjectId);
                String likeUserKey = RedisKeyUtil.getLikeUserKey(subjectAuthorId);

                boolean isMember = operations.opsForSet().isMember(likeKey, userId);

                operations.multi();

                if (isMember) {
                    operations.opsForSet().remove(likeKey, userId);
                    operations.opsForValue().decrement(likeUserKey);
                } else {
                    operations.opsForSet().add(likeKey, userId);
                    operations.opsForValue().increment(likeUserKey);
                }

                return operations.exec();
            }
        });
    }

    public long getLikeCount(int subjectType, int subjectId) {
        String likeKey = RedisKeyUtil.getLikeKey(subjectType, subjectId);
        return redisTemplate.opsForSet().size(likeKey);
    }

    public int getLikeStatus(int userId, int subjectType, int subjectId) {
        String likeKey = RedisKeyUtil.getLikeKey(subjectType, subjectId);
        return redisTemplate.opsForSet().isMember(likeKey, userId) ? 1 : 0;
    }

    public int getUserLikeCount(int userId) {
        String likeUserKey = RedisKeyUtil.getLikeUserKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(likeUserKey);
        return count == null ? 0 : count;
    }
}
