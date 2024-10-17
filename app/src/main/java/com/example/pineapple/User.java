package com.example.pineapple;

public class User {
    private String name;
    private int profilePicture; // This will store the resource ID for the placeholder image

    public User(String name, int profilePicture) {
        this.name = name;
        this.profilePicture = profilePicture;
    }

    public String getName() {
        return name;
    }

    public int getProfilePicture() {
        return profilePicture;
    }
}
