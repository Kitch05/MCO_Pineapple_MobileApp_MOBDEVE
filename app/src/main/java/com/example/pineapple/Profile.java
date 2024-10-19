package com.example.pineapple;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Date;

public class Profile extends BaseActivity {
    private TextView username;
    private ImageView profilePic;
    private TextView description;
    private TextView userSince;
    private Button editProfile;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        username = findViewById(R.id.userName);
        profilePic = findViewById(R.id.profilePic);
        description = findViewById(R.id.userDescription);
        editProfile = findViewById(R.id.editProfileButton);
    }

    public void back(View view) {
        finish();
    }

    public void editProfile(View view) {

    }
}