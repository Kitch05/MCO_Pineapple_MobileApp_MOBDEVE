package com.example.pineapple;

public class Post {
    private String title;
    private String content;
    private User user;
    private String community; // New community field
    private int upvoteCount;
    private int downvoteCount;

    public Post(String title, String content, User user, String community) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.community = community;
        this.upvoteCount = 0;
        this.downvoteCount = 0;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public User getUser() {
        return user;
    }

    public String getCommunity() {
        return community;
    }

    public int getUpvoteCount() {
        return upvoteCount;
    }

    public int getDownvoteCount() {
        return downvoteCount;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public void setUpvoteCount(int upvoteCount) {
        this.upvoteCount = upvoteCount;
    }

    public void setDownvoteCount(int downvoteCount) {
        this.downvoteCount = downvoteCount;
    }

    // Upvote and Downvote methods
    public void upvote() {
        upvoteCount++;
    }

    public void downvote() {
        downvoteCount++;
    }
}
