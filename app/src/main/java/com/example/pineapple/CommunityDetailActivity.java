package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CommunityDetailActivity extends AppCompatActivity {

    private static final int ADD_POST_REQUEST_CODE = 1;
    private static final int EDIT_POST_REQUEST_CODE = 2;

    private TextView communityNameTextView;
    private TextView communityDescriptionTextView;
    private ImageView communityIconImageView;
    private TextView memberCountTextView;
    private TextView postCountTextView;
    private Button joinLeaveButton;
    private ImageView backButton;
    private RecyclerView postListRecyclerView;

    private Community community;
    private boolean isMember = false; // Initial membership status
    private int memberCount; // Member count
    private int postCount; // Post count

    private PostAdapter postAdapter;
    private List<Post> postList; // List of posts

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_detail);

        // Initialize Views
        communityNameTextView = findViewById(R.id.communityName);
        communityDescriptionTextView = findViewById(R.id.communityDescription);
        communityIconImageView = findViewById(R.id.communityIcon);
        memberCountTextView = findViewById(R.id.memberCount);
        postCountTextView = findViewById(R.id.postCount);
        joinLeaveButton = findViewById(R.id.joinLeaveButton);
        backButton = findViewById(R.id.backButton);
        postListRecyclerView = findViewById(R.id.communityPostList);
        Button addPostButton = findViewById(R.id.addPostButton);

        // Get community details from intent
        Intent intent = getIntent();
        String communityName = intent.getStringExtra("communityName");
        String communityDescription = intent.getStringExtra("communityDescription");
        int communityIcon = intent.getIntExtra("communityIcon", R.drawable.placeholder_image);
        memberCount = intent.getIntExtra("memberCount", 0);
        postCount = intent.getIntExtra("postCount", 0);

        // Create Community object
        community = new Community(communityName, communityDescription, memberCount, postCount, new User("Creator", R.drawable.placeholder_image));

        // Set community details with null checks
        communityNameTextView.setText(communityName != null ? communityName : "Unknown Community");
        communityDescriptionTextView.setText(communityDescription != null ? communityDescription : "No description available.");
        communityIconImageView.setImageResource(communityIcon);
        memberCountTextView.setText(String.valueOf(memberCount));
        postCountTextView.setText(String.valueOf(postCount));

        // Back Button Logic
        backButton.setOnClickListener(v -> onBackPressed());

        // Join/Leave Button Logic
        updateJoinLeaveButtonText();
        joinLeaveButton.setOnClickListener(v -> {
            community.toggleMembership(); // Toggle membership status
            memberCountTextView.setText(String.valueOf(community.getMemberCount())); // Update member count
            updateJoinLeaveButtonText(); // Update button text based on membership
        });

        // Initialize Post RecyclerView
        postListRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set layout manager

        // Initialize post list and adapter
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList, position -> {
            // Handle post click
            Intent postDetailIntent = new Intent(CommunityDetailActivity.this, PostDetailActivity.class);
            postDetailIntent.putExtra("postTitle", postList.get(position).getTitle());
            postDetailIntent.putExtra("postContent", postList.get(position).getContent());
            postDetailIntent.putExtra("postUser", postList.get(position).getUser().getName());
            startActivity(postDetailIntent);
        }, position -> {
            // Handle edit post click
            Intent editPostIntent = new Intent(CommunityDetailActivity.this, AddEditPostActivity.class);
            editPostIntent.putExtra("title", postList.get(position).getTitle());
            editPostIntent.putExtra("content", postList.get(position).getContent());
            editPostIntent.putExtra("position", position);
            startActivityForResult(editPostIntent, EDIT_POST_REQUEST_CODE);
        });
        postListRecyclerView.setAdapter(postAdapter); // Set the adapter

        // Add Post Button Logic
        addPostButton.setOnClickListener(v -> {
            Intent addPostIntent = new Intent(CommunityDetailActivity.this, AddEditPostActivity.class);
            startActivityForResult(addPostIntent, ADD_POST_REQUEST_CODE);
        });
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("position", getIntent().getIntExtra("position", -1));
        resultIntent.putExtra("memberCount", community.getMemberCount());
        resultIntent.putExtra("postCount", community.getPostCount());
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String postTitle = data.getStringExtra("title");
            String postContent = data.getStringExtra("content");
            int position = data.getIntExtra("position", -1);

            if (requestCode == ADD_POST_REQUEST_CODE) {
                // Add new post to the list and notify the adapter
                postList.add(new Post(postTitle, postContent, new User("User", R.drawable.placeholder_image)));
                postAdapter.notifyItemInserted(postList.size() - 1);

                // Increment post count and update the TextView
                postCount++;
                community.setPostCount(postCount); // Update community post count
                postCountTextView.setText(String.valueOf(postCount));
            } else if (requestCode == EDIT_POST_REQUEST_CODE && position != -1) {
                // Update the existing post and notify the adapter
                Post post = postList.get(position);
                post.setTitle(postTitle);
                post.setContent(postContent);
                postAdapter.notifyItemChanged(position);
            }
        }
    }

    private void updateJoinLeaveButtonText() {
        joinLeaveButton.setText(community.isJoined() ? "Leave" : "Join");
    }
}
