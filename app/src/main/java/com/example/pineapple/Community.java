package com.example.pineapple;

public class Community {
    private String name;
    private String description;
    private int memberCount; // Optional: initialize with 0
    private int postCount;   // Optional: initialize with 0
    private final User creator; // Optional: this could also be set later if needed
    private boolean isJoined;

    // Constructor for creating a community with name and description only
    public Community(String name, String description) {
        this.name = name;
        this.description = description;
        this.memberCount = 0;
        this.postCount = 0;
        this.creator = null;
        this.isJoined = false;
    }

    // Full constructor if you want to provide more details
    public Community(String name, String description, int memberCount, int postCount, User creator) {
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
        this.postCount = postCount;
        this.creator = creator;
        this.isJoined = false; // By default, the user is not a member
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public User getCreator() {
        return creator;
    }

    public boolean isJoined() {
        return isJoined;
    }

    // Setters
    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    // Methods to join or leave the community
    public synchronized void joinCommunity() {
        if (!isJoined) {
            isJoined = true;
            memberCount++;
        }
    }

    public synchronized void leaveCommunity() {
        if (isJoined) {
            isJoined = false;
            if (memberCount > 0) {
                memberCount--;
            }
        }
    }

    // Method to toggle membership status
    public synchronized void toggleMembership() {
        if (isJoined) {
            leaveCommunity();
        } else {
            joinCommunity();
        }
    }
}
