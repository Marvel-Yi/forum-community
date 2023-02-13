package com.marvel.communityforum.service;

import com.marvel.communityforum.entity.User;
import com.marvel.communityforum.util.CommunityConstant;
import com.marvel.communityforum.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

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

    public List<Map<String, Object>> getFollowList(int userId, int offset, int limit) {
        String followKey = RedisKeyUtil.getFollowKey(userId, SUBJECT_TYPE_USER);
        Set<Integer> followIds = redisTemplate.opsForZSet().reverseRange(followKey, offset, offset + limit - 1);

        if (followIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (int followId : followIds) {
            Map<String, Object> map = new HashMap<>();
            User followUser = userService.getUserById(followId);
            map.put("follow user", followUser);
            double score = redisTemplate.opsForZSet().score(followKey, followId);
            map.put("follow time", new Date((long) score));
            list.add(map);
        }

        return list;
    }

    public List<Map<String, Object>> getFansList(int userId, int offset, int limit) {
        String fansKey = RedisKeyUtil.getFansKey(SUBJECT_TYPE_USER, userId);
        Set<Integer> fansIds = redisTemplate.opsForZSet().reverseRange(fansKey, offset, offset + limit - 1);

        if (fansIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (int fansId : fansIds) {
            Map<String, Object> map = new HashMap<>();
            User fansUser = userService.getUserById(fansId);
            map.put("fan", fansUser);
            double score = redisTemplate.opsForZSet().score(fansKey, fansId);
            map.put("fan time", new Date((long) score));
            list.add(map);
        }

        return list;
    }
}
