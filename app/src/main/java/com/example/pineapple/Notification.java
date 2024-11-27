package com.example.pineapple;

import java.util.Date;

public class Notification {
    private String id;
    private boolean isRead;
    private Date date;
    private String type;
    private String fromId;
    private String userId;
    private String description;

    public Notification() {}

    public Notification(String userId, String fromId, String type, String fromName) {
        this.userId = userId;
        this.fromId = fromId;
        this.type = type;
        this.isRead = false;
        this.date = new Date();
        switch (type) {
            case "replied":
                this.description = "A user has replied in your post.";
                break;
            case "upvote":
                this.description = fromName + " upvoted your post";
                break;
            case "downvote":
                this.description = fromName + " downvoted your post";
                break;
            default:
                this.description = null;
        }
    }

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getFromId() {
        return fromId;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}