package com.marvel.communityforum.util;

public class RedisKeyUtil {
    private static final String SEPARATOR = ":";
    private static final String PREFIX_LIKE = "like";
    private static final String PREFIX_LIKE_USER = "like:user";

    public static String getLikeKey(int subjectType, int subjectId) {
        return PREFIX_LIKE + SEPARATOR + subjectType + SEPARATOR + subjectId;
    }

    public static String getLikeUserKey(int userId) {
        return PREFIX_LIKE_USER + SEPARATOR + userId;
    }
}