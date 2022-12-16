package com.marvel.communityforum.util;

public interface CommunityConstant {
    int LOGIN_STATUS_EXPIRED = -1;

    int ACTIVATION_SUCCESS = 0;

    int ACTIVATION_REPEAT = 1;

    int ACTIVATION_FAILURE = 2;

    int DEFAULT_LOGIN_EXPIRED_SECONDS = 3600;

    int REMEMBER_LOGIN_EXPIRED_SECONDS = 7 * 24 * 3600;

    int PASSWORD_MODIFY_SUCCESS = 0;

    int PASSWORD_INCORRECT = 1;

    int PASSWORD_REPEAT_INCORRECT = 2;

    int PASSWORD_BLANK = 3;

    int COMMENT_SUBJECT_TYPE_POST = 1;

    int COMMENT_SUBJECT_TYPE_COMMENT = 2;
}
