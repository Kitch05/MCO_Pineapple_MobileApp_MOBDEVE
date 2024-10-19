package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

public class ProfileActivity extends BaseActivity {
    private TextView username;
    private ImageView profilePic;
    private TextView description;
    private TextView userSince;
    private Button editProfile;
    private ActivityResultLauncher<Intent> editProfileLauncher;

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
        Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
        intent.putExtra("username", username.getText());
        intent.putExtra("profilePic", profilePic.toString());
        intent.putExtra("userDescription", description.getText());
        startActivity(intent);
    }
}