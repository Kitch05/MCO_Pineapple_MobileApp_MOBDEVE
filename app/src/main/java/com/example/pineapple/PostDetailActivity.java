package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PostDetailActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView contentTextView;
    private TextView userNameTextView;
    private ImageView userProfileImageView;
    private TextView communityTextView;  // Add TextView for community
    private TextView upvoteCountTextView;
    private TextView downvoteCountTextView;
    private ImageView upvoteButton;
    private ImageView downvoteButton;
    private EditText commentInput;
    private ImageView backButton;

    private int upvoteCount = 0;
    private int downvoteCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        titleTextView = findViewById(R.id.postDetailTitle);
        contentTextView = findViewById(R.id.postDetailContent);
        userNameTextView = findViewById(R.id.userDetailName);
        userProfileImageView = findViewById(R.id.userDetailProfilePicture);
        communityTextView = findViewById(R.id.postCommunity);  // Initialize community TextView
        upvoteCountTextView = findViewById(R.id.upvoteCount);
        downvoteCountTextView = findViewById(R.id.downvoteCount);
        upvoteButton = findViewById(R.id.upvoteIcon);
        downvoteButton = findViewById(R.id.downvoteIcon);
        commentInput = findViewById(R.id.commentInput);
        backButton = findViewById(R.id.backButton);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String username = intent.getStringExtra("username");
        String community = intent.getStringExtra("community");  // Get community from intent
        int profilePicture = intent.getIntExtra("profilePicture", R.drawable.placeholder_image);

        // Set post details
        titleTextView.setText(title);
        contentTextView.setText(content);
        userNameTextView.setText(username);
        communityTextView.setText(community);  // Display community name
        userProfileImageView.setImageResource(profilePicture);

        // Upvote Button Logic
        upvoteButton.setOnClickListener(v -> {
            upvoteCount++;
            upvoteCountTextView.setText(String.valueOf(upvoteCount));
        });

        // Downvote Button Logic
        downvoteButton.setOnClickListener(v -> {
            downvoteCount++;
            downvoteCountTextView.setText(String.valueOf(downvoteCount));
        });

        // Comment Section
        commentInput.setOnEditorActionListener((v, actionId, event) -> {
            String comment = commentInput.getText().toString().trim();
            if (!comment.isEmpty()) {
                // Add logic to handle and display submitted comments
                commentInput.setText(""); // Clear the input field after submission
            }
            return true;
        });

        // Back Button Logic
        backButton.setOnClickListener(v -> onBackPressed()); // Handle back navigation
    }
}
