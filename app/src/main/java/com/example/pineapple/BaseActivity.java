package com.example.pineapple;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.LayoutRes;
import android.widget.FrameLayout;



public class BaseActivity extends AppCompatActivity {

    private ImageView menuButton;
    private ImageView homeButton;
    private ImageView profileIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base); // Set the base layout that includes the header and the content frame

        // Initialize header buttons
        menuButton = findViewById(R.id.menuButton);
        homeButton = findViewById(R.id.homeButton);
        profileIcon = findViewById(R.id.profileIcon);

        // Set up button click listeners for the header
        setupHeaderButtons();
    }

    private void setupHeaderButtons() {
        menuButton.setOnClickListener(v -> {
            // Handle menu button click
            Intent intent = new Intent(BaseActivity.this, CommunityActivity.class);
            startActivity(intent);
        });

        homeButton.setOnClickListener(v -> {
            // Handle home button click
            Intent intent = new Intent(BaseActivity.this, MainActivity.class);
            startActivity(intent);
        });

        profileIcon.setOnClickListener(v -> {
            // Handle profile button click
            Intent intent = new Intent(BaseActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Set the content layout for the activity that extends BaseActivity.
     */
    protected void setActivityLayout(@LayoutRes int layoutResID) {
        FrameLayout contentFrame = findViewById(R.id.contentFrame);
        View view = getLayoutInflater().inflate(layoutResID, contentFrame, false);
        contentFrame.addView(view); // Inflate and add the content to the frame
    }
}
