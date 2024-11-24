package com.example.pineapple;

public class Comment {
    private String id; // Firestore ID
    private String content;
    private String userId;
    private String parentId; // For nested replies
    private long timestamp;

    // Firestore requires an empty constructor
    public Comment() {}

    public Comment(String content, String userId, String parentId, long timestamp) {
        this.content = content;
        this.userId = userId;
        this.parentId = parentId;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
