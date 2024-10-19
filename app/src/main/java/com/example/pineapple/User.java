package com.example.pineapple;

public class User {
    private String name;
    private int profilePicture;

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
