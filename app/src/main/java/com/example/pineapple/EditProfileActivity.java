package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editUsername;
    private EditText editDescription;
    private ImageSwitcher editProfilePic;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        Intent intent = getIntent();
        editUsername = findViewById(R.id.editUsername);
        editDescription = findViewById(R.id.editDescription);
        editProfilePic = findViewById(R.id.profilePic);


    }
}