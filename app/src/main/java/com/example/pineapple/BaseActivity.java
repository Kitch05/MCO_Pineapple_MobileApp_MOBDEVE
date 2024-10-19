package com.example.pineapple;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.annotation.LayoutRes;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class BaseActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> addEditPostLauncher;

    private ImageView menuButton;
    private ImageView homeButton;
    private ImageView profileIcon;

    private ImageView navHome;
    private ImageView navCommunity;
    private ImageView navPost;
    private ImageView navInbox;
    private ImageView navNotifs;

    BottomNavigationView navbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base); // Set the base layout that includes the header and the content frame

        // Initialize header buttons
        menuButton = findViewById(R.id.menuButton);
        homeButton = findViewById(R.id.homeButton);
        profileIcon = findViewById(R.id.profileIcon);

        navHome = findViewById(R.id.navHome);
        navCommunity = findViewById(R.id.navCommunity);
        navPost = findViewById(R.id.navPost);
        navInbox = findViewById(R.id.navInbox);
        navNotifs = findViewById(R.id.navNotifs);

        // Set up button click listeners for the header
        setupHeaderButtons();
        setupFooterButtons();
    }

    private void setupFooterButtons() {
        navHome.setOnClickListener( v -> {
            Intent intent = new Intent(BaseActivity.this, MainActivity.class);
            startActivity(intent);
        });

        navCommunity.setOnClickListener(v -> {
            Intent intent = new Intent(BaseActivity.this, MyCommunities.class);
            startActivity(intent);
        });

        navPost.setOnClickListener(v -> {
            Button addPostButton = findViewById(R.id.addPostButton);

            // Programmatically "click" the addPostButton
            if (addPostButton != null) {
                addPostButton.performClick();
            }
        });

        navInbox.setOnClickListener( v -> {
            Intent intent = new Intent(BaseActivity.this, Inbox.class);
            startActivity(intent);
        });

        navNotifs.setOnClickListener( v -> {
            Intent intent = new Intent(BaseActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });
    }

    private void setupHeaderButtons() {
        menuButton.setOnClickListener(v -> {
            // Handle menu button click
            Intent intent = new Intent(BaseActivity.this, MyCommunities.class);
            startActivity(intent);
        });

        homeButton.setOnClickListener(v -> {
            // Handle home button click
            Intent intent = new Intent(BaseActivity.this, MainActivity.class);
            startActivity(intent);
        });

        profileIcon.setOnClickListener(v -> {
            // Handle profile button click
            Intent intent = new Intent(BaseActivity.this, ProfileActivity.class);
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

    private void launchAddPost() {
        Intent intent = new Intent(getBaseContext(), AddEditPostActivity.class);
        addEditPostLauncher.launch(intent);
    }
}
