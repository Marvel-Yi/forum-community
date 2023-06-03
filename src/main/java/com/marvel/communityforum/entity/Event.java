package com.marvel.communityforum.entity;

import java.util.HashMap;
import java.util.Map;

public class Event {
    private String topic;
    private int userId;
    private int subjectType;
    private int subjectId;
    private int subjectUserId;
    private Map<String, Object> data = new HashMap<>();

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public Event setSubjectType(int subjectType) {
        this.subjectType = subjectType;
        return this;
    }

    public Event setSubjectId(int subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public Event setSubjectUserId(int subjectUserId) {
        this.subjectUserId = subjectUserId;
        return this;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public int getUserId() {
        return userId;
    }

    public int getSubjectType() {
        return subjectType;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public int getSubjectUserId() {
        return subjectUserId;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
