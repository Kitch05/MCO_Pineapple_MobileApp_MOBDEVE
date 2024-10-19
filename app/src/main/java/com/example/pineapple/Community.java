package com.example.pineapple;

public class Community{
    private String name;
    private String description;

    private int memberCount;
    private int postCount;
    private final User creator;
    private boolean isJoined;

    public Community(String name, String description) {
        this.name = name;
        this.description = description;
        this.memberCount = 0;
        this.postCount = 0;
        this.creator = null;
        this.isJoined = false;
    }

    public Community(String name, String description, int memberCount, int postCount, User creator) {
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
        this.postCount = postCount;
        this.creator = creator;
        this.isJoined = false;
    }

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

    public synchronized void toggleMembership() {
        if (isJoined) {
            leaveCommunity();
        } else {
            joinCommunity();
        }
    }
}
