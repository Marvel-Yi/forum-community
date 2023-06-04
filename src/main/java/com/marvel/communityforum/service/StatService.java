package com.marvel.communityforum.service;

import com.marvel.communityforum.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class StatService {
    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    public void recordUV(String ip) {
        String uvKey = RedisKeyUtil.getDailyUVKEy(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(uvKey, ip);
    }

    public long getRangeUV(Date beginDate, Date endDate) {
        if (beginDate == null || endDate == null) {
            throw new IllegalArgumentException("date argument was null");
        }

        List<String> uvKeys = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(beginDate);
        while (!calendar.getTime().after(endDate)) {
            String key = RedisKeyUtil.getDailyUVKEy(df.format(calendar.getTime()));
            uvKeys.add(key);
            calendar.add(Calendar.DATE, 1);
        }

        String rangeUVKey = RedisKeyUtil.getRangeUVKey(df.format(beginDate), df.format(endDate));
        redisTemplate.opsForHyperLogLog().union(rangeUVKey, uvKeys.toArray());

        return redisTemplate.opsForHyperLogLog().size(rangeUVKey);
    }

    public void recordDAU(int userId) {
        String DAUKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(DAUKey, userId, true);
    }

    public long getRangeAU(Date beginDate, Date endDate) {
        if (beginDate == null || endDate == null) {
            throw new IllegalArgumentException("date argument was null");
        }

        List<byte[]> dauKeys = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(beginDate);
        while (!calendar.getTime().after(endDate)) {
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            dauKeys.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        return (Long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String rangeAUKey = RedisKeyUtil.getRangeAUKey(df.format(beginDate), df.format(endDate));
                connection.bitOp(RedisStringCommands.BitOperation.OR, rangeAUKey.getBytes(), dauKeys.toArray(new byte[0][0]));
                return connection.bitCount(rangeAUKey.getBytes());
            }
        });
    }
}
