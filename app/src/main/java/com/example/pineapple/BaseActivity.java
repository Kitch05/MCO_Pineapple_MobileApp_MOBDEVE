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
    private ImageView navProfile;
    private ImageView navNotifs;

    BottomNavigationView navbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        menuButton = findViewById(R.id.menuButton);
        homeButton = findViewById(R.id.homeButton);
        profileIcon = findViewById(R.id.profileIcon);

        navHome = findViewById(R.id.navHome);
        navCommunity = findViewById(R.id.navCommunity);
        navPost = findViewById(R.id.navPost);
        navProfile = findViewById(R.id.navProfile);
        navNotifs = findViewById(R.id.navNotifs);

        setupHeaderButtons();
        setupFooterButtons();
    }

    private void setupFooterButtons() {
        navHome.setOnClickListener( v -> {
            Intent intent = new Intent(BaseActivity.this, MainActivity.class);
            startActivity(intent);
        });

        navCommunity.setOnClickListener(v -> {
            Intent intent = new Intent(BaseActivity.this, CommunityActivity.class);
            startActivity(intent);
        });

        navPost.setOnClickListener(v -> {
            Intent intent = new Intent(BaseActivity.this, AddEditPostActivity.class);
            startActivity(intent);
        });

        navNotifs.setOnClickListener( v -> {
            Intent intent = new Intent(BaseActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        navProfile.setOnClickListener( v -> {
            Intent intent = new Intent(BaseActivity.this, ProfilePosts.class);
            startActivity(intent);
        });
    }

    private void setupHeaderButtons() {
        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(BaseActivity.this, MyCommunities.class);
            startActivity(intent);
        });

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(BaseActivity.this, MainActivity.class);
            startActivity(intent);
        });

        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(BaseActivity.this, ProfilePosts.class);
            startActivity(intent);
        });
    }


    protected void setActivityLayout(@LayoutRes int layoutResID) {
        FrameLayout contentFrame = findViewById(R.id.contentFrame);
        View view = getLayoutInflater().inflate(layoutResID, contentFrame, false);
        contentFrame.addView(view);
    }

    private void launchAddPost() {
        Intent intent = new Intent(getBaseContext(), AddEditPostActivity.class);
        addEditPostLauncher.launch(intent);
    }
}
