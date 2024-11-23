package com.example.pineapple;

public class Post {
    private String id; // Firestore document ID
    private String title;
    private String content;
    private String userId; // ID of the user who created the post
    private String community; // Name or ID of the community
    private int upvoteCount;
    private int downvoteCount;

    // Default constructor required for Firestore
    public Post() {}

    public Post(String title, String content, String userId, String community) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.community = community;
        this.upvoteCount = 0;
        this.downvoteCount = 0;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public int getUpvoteCount() {
        return upvoteCount;
    }

    public void setUpvoteCount(int upvoteCount) {
        this.upvoteCount = upvoteCount;
    }

    public int getDownvoteCount() {
        return downvoteCount;
    }

    public void setDownvoteCount(int downvoteCount) {
        this.downvoteCount = downvoteCount;
    }
}
