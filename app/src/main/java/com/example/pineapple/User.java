package com.example.pineapple;

import java.util.List;

public class User {
    private String name;
    private String email;
    private String profilePicture;
    private List<String> joinedCommunities;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.profilePicture = "default_profile_picture"; // Add a default profile picture
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public List<String> getJoinedCommunities() {
        return joinedCommunities;
    }

    public void setJoinedCommunities(List<String> joinedCommunities) {
        this.joinedCommunities = joinedCommunities;
    }
}
