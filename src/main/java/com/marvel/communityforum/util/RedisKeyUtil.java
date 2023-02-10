package com.marvel.communityforum.util;

public class RedisKeyUtil {
    private static final String SEPARATOR = ":";
    private static final String PREFIX_LIKE = "like";

    public static String getLikeKeyPrefix(int subjectType, int subjectId) {
        return PREFIX_LIKE + SEPARATOR + subjectType + SEPARATOR + subjectId;
    }
}