package com.example.pineapple;

public class Comment {
    private String id; // Firestore ID for the comment
    private String content; // The comment content
    private String userId; // ID of the user who posted the comment
    private String parentId; // The ID of the parent comment (null for top-level comments)
    private long timestamp; // Timestamp when the comment was created

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
