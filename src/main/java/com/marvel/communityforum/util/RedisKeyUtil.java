package com.marvel.communityforum.util;

public class RedisKeyUtil {
    private static final String SEPARATOR = ":";
    private static final String PREFIX_LIKE = "like";
    private static final String PREFIX_LIKE_USER = "like:user";
    private static final String PREFIX_FANS = "fans";
    private static final String PREFIX_FOLLOW = "follow";
    private static final String PREFIX_LOGIN_TICKET = "ticket";
    private static final String PREFIX_USER = "user";

    private static final String PREFIX_UV = "uv";

    private static final String PREFIX_DAU = "dau";

    private static final String PREFIX_POST_FOR_GRADING = "grading:post";

    public static String getLikeKey(int subjectType, int subjectId) {
        return PREFIX_LIKE + SEPARATOR + subjectType + SEPARATOR + subjectId;
    }

    public static String getLikeUserKey(int userId) {
        return PREFIX_LIKE_USER + SEPARATOR + userId;
    }

    public static String getFansKey(int subjectType, int subjectId) {
        return PREFIX_FANS + SEPARATOR + subjectType + SEPARATOR + subjectId;
    }

    public static String getFollowKey(int userId, int subjectType) {
        return PREFIX_FOLLOW + SEPARATOR + userId + SEPARATOR + subjectType;
    }

    public static String getLoginTicketKey(String ticket) {
        return PREFIX_LOGIN_TICKET + SEPARATOR + ticket;
    }

    public static String getUserKey(int userId) {
        return PREFIX_USER + SEPARATOR + userId;
    }

    public static String getDailyUVKEy(String date) {
        return PREFIX_UV + SEPARATOR + date;
    }

    public static String getRangeUVKey(String beginDate, String endDate) {
        return PREFIX_UV + SEPARATOR + beginDate + SEPARATOR + endDate;
    }

    public static String getDAUKey(String date) {
        return PREFIX_DAU + SEPARATOR + date;
    }

    public static String getRangeAUKey(String beginDate, String endDate) {
        return PREFIX_DAU + SEPARATOR + beginDate + SEPARATOR + endDate;
    }

    public static String getPostForGradingKey() {
        return PREFIX_POST_FOR_GRADING;
    }
}