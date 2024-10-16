package com.example.pineapple;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Date;

public class Profile extends AppCompatActivity {
    private String username;
    private Integer profilePic;
    private String description;
    private Date userSince;
    private String password;

    public Date getUserSince() {
        return userSince;
    }

    public String getDescription() {
        return description;
    }

    public String getPassword() {
        return password;
    }

    public Integer getProfilePic() {
        return profilePic;
    }

    public String getUsername() {
        return username;
    }
}