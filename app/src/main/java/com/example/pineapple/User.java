package com.example.pineapple;

public class User {
    private String name;
    private String email;
    private String profilePicture;

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
}
