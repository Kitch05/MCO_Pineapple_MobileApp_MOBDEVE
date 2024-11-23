package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CommunityDetailActivity extends AppCompatActivity {

    private static final int ADD_POST_REQUEST_CODE = 1;
    private static final int EDIT_POST_REQUEST_CODE = 2;
    private static final int EDIT_COMMUNITY_REQUEST_CODE = 3;

    private FirebaseFirestore db;
    private TextView communityNameTextView;
    private TextView communityDescriptionTextView;
    private ImageView communityIconImageView;
    private TextView memberCountTextView;
    private TextView postCountTextView;
    private Button joinLeaveButton;
    private ImageView backButton;
    private RecyclerView postListRecyclerView;
    private ImageView editCommunityButton;

    private Community community;
    private int memberCount;
    private int postCount;
    private List<Post> postList;
    private PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_detail);

        // Initialize views
        communityNameTextView = findViewById(R.id.communityName);
        communityDescriptionTextView = findViewById(R.id.communityDescription);
        communityIconImageView = findViewById(R.id.communityIcon);
        memberCountTextView = findViewById(R.id.memberCount);
        postCountTextView = findViewById(R.id.postCount);
        joinLeaveButton = findViewById(R.id.joinLeaveButton);
        backButton = findViewById(R.id.backButton);
        postListRecyclerView = findViewById(R.id.communityPostList);
        editCommunityButton = findViewById(R.id.editCommunityButton);
        Button addPostButton = findViewById(R.id.addPostButton);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get community ID from Intent
        String communityId = getIntent().getStringExtra("communityId");

        // Fetch community data from Firestore
        fetchCommunityData(communityId);

        // Set up Post RecyclerView
        postListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList, this::onPostClick, this::onPostEditClick);
        postListRecyclerView.setAdapter(postAdapter);

        // Add post button logic
        addPostButton.setOnClickListener(v -> {
            Intent addPostIntent = new Intent(CommunityDetailActivity.this, AddEditPostActivity.class);
            startActivityForResult(addPostIntent, ADD_POST_REQUEST_CODE);
        });

        // Edit community button logic
        editCommunityButton.setOnClickListener(v -> {
            Intent editCommunityIntent = new Intent(CommunityDetailActivity.this, AddEditCommunityActivity.class);
            editCommunityIntent.putExtra("communityId", communityId);
            startActivityForResult(editCommunityIntent, EDIT_COMMUNITY_REQUEST_CODE);
        });

        // Back button logic
        backButton.setOnClickListener(v -> onBackPressed());

        // Join/Leave button logic
        updateJoinLeaveButtonText();
        joinLeaveButton.setOnClickListener(v -> {
            toggleMembership();
        });
    }

    private void fetchCommunityData(String communityId) {
        // Fetch the community document from Firestore
        DocumentReference communityRef = db.collection("community").document(communityId);
        communityRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get the community data
                if (task.getResult() != null) {
                    String name = task.getResult().getString("name");
                    String description = task.getResult().getString("description");
                    memberCount = task.getResult().getLong("memberCount").intValue();
                    postCount = task.getResult().getLong("postCount").intValue();

                    // Set community details
                    communityNameTextView.setText(name);
                    communityDescriptionTextView.setText(description);
                    memberCountTextView.setText(String.valueOf(memberCount));
                    postCountTextView.setText(String.valueOf(postCount));

                    // Fetch posts
                    fetchPosts(communityId);
                }
            } else {
                Log.e("CommunityDetailActivity", "Error fetching community data", task.getException());
            }
        });
    }

    private void fetchPosts(String communityId) {
        // Fetch posts from the "posts" subcollection
        db.collection("community")
                .document(communityId)
                .collection("posts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            String content = document.getString("content");
                            String author = document.getString("author");
                            postList.add(new Post(title, content, new User(author, R.drawable.placeholder_image), communityId));
                        }
                        postAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("CommunityDetailActivity", "Error fetching posts", task.getException());
                    }
                });
    }

    private void toggleMembership() {
        // Here you can update the membership status in Firestore and update UI accordingly
        boolean isJoined = !community.isJoined(); // Toggle membership
        community.setJoined(isJoined);
        memberCount = isJoined ? memberCount + 1 : memberCount - 1;
        memberCountTextView.setText(String.valueOf(memberCount));
        updateJoinLeaveButtonText();

        // Update Firestore membership
        DocumentReference communityRef = db.collection("community").document(community.getId());
        communityRef.update("memberCount", memberCount);
    }

    private void updateJoinLeaveButtonText() {
        joinLeaveButton.setText(community.isJoined() ? "Leave" : "Join");
    }

    private void onPostClick(int position) {
        Post post = postList.get(position);
        Intent postDetailIntent = new Intent(CommunityDetailActivity.this, PostDetailActivity.class);
        postDetailIntent.putExtra("postId", post.getId());
        startActivity(postDetailIntent);
    }

    private void onPostEditClick(int position) {
        Post post = postList.get(position);
        Intent editPostIntent = new Intent(CommunityDetailActivity.this, AddEditPostActivity.class);
        editPostIntent.putExtra("postId", post.getId());
        startActivityForResult(editPostIntent, EDIT_POST_REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("memberCount", memberCount);
        resultIntent.putExtra("postCount", postCount);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == ADD_POST_REQUEST_CODE) {
                String postTitle = data.getStringExtra("title");
                String postContent = data.getStringExtra("content");
                postList.add(new Post(postTitle, postContent, new User("User", R.drawable.placeholder_image), community.getId()));
                postAdapter.notifyItemInserted(postList.size() - 1);

                postCount++;
                postCountTextView.setText(String.valueOf(postCount));
            } else if (requestCode == EDIT_POST_REQUEST_CODE) {
                String postTitle = data.getStringExtra("title");
                String postContent = data.getStringExtra("content");
                int position = data.getIntExtra("position", -1);

                if (position != -1) {
                    Post post = postList.get(position);
                    post.setTitle(postTitle);
                    post.setContent(postContent);
                    postAdapter.notifyItemChanged(position);
                }
            } else if (requestCode == EDIT_COMMUNITY_REQUEST_CODE) {
                String updatedName = data.getStringExtra("communityName");
                String updatedDescription = data.getStringExtra("communityDescription");
                community.setName(updatedName);
                community.setDescription(updatedDescription);

                communityNameTextView.setText(updatedName);
                communityDescriptionTextView.setText(updatedDescription);
            }
        }
    }
}
