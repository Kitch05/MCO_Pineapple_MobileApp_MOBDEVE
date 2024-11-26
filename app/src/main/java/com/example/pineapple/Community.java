package com.example.pineapple;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Community {
    private String id; // Firestore document ID
    private String name;
    private String description;
    private int memberCount;
    private int postCount;
    private String creatorId;
    private List<String> members;

    public Community() {
        // Required for Firestore deserialization
    }

    public Community(String name, String description) {
        this.name = name;
        this.description = description;
        this.memberCount = 0;
        this.postCount = 0;
        this.creatorId = "";
        this.members = new ArrayList<>();
    }

    public Community(String id, String name, String description, int memberCount, int postCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
        this.postCount = postCount;
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

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public void setName(String updatedName) {
        this.name = updatedName;
    }

    public void setDescription(String updatedDescription) {
        this.description = updatedDescription;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
}