package com.example.pineapple;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileDetailActivity extends AppCompatActivity {

    private EditText editUsername;
    private EditText editDescription;
    private ImageSwitcher editProfilePic;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);


    }
}