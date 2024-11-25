package com.example.pineapple;

import com.google.firebase.firestore.Exclude;

public class Community {
    private String id; // Firestore document ID
    private String name;
    private String description;
    private int memberCount;
    private int postCount;
    private boolean isJoined;

    public Community() {
        // Required for Firestore deserialization
    }

    public Community(String name, String description) {
        this.name = name;
        this.description = description;
        this.memberCount = 0;
        this.postCount = 0;
        this.isJoined = false;
    }

    public Community(String id, String name, String description, int memberCount, int postCount, boolean isJoined) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
        this.postCount = postCount;
        this.isJoined = isJoined;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isJoined() {
        return isJoined;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public void setJoined(boolean joined) {
        isJoined = joined;
    }

    public void setName(String updatedName) {
        this.name = updatedName;
    }

    public void setDescription(String updatedDescription) {
        this.description = updatedDescription;
    }

    public void joinCommunity() {
        if (!isJoined) {
            isJoined = true;  // Mark the user as a member
            memberCount++;  // Increase the member count
        }
    }

    public void leaveCommunity() {
        if (isJoined) {
            isJoined = false;  // Mark the user as not a member
            memberCount--;  // Decrease the member count
        }
    }
}
