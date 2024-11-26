package com.example.pineapple;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private List<Community> communityList;
    private PostAdapter postAdapter;
    private Context context;

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
            addPostIntent.putExtra("communityId", communityId);
            startActivityForResult(addPostIntent, ADD_POST_REQUEST_CODE);
        });

        // Edit community button logic
        editCommunityButton.setOnClickListener(v -> {
            Intent editCommunityIntent = new Intent(CommunityDetailActivity.this, AddEditCommunityActivity.class);
            editCommunityIntent.putExtra("communityId", communityId); // Pass the community ID
            editCommunityIntent.putExtra("communityName", community.getName());  // Pass the community name
            editCommunityIntent.putExtra("communityDescription", community.getDescription());
            startActivityForResult(editCommunityIntent, EDIT_COMMUNITY_REQUEST_CODE); // Start the activity for result
        });

        // Back button logic
        backButton.setOnClickListener(v -> onBackPressed());

        // Join/Leave button logic
        joinLeaveButton.setOnClickListener(v -> toggleMembership(communityId));
    }


    private void fetchCommunityData(String communityId) {
        DocumentReference communityRef = db.collection("community").document(communityId);
        communityRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();
                community = document.toObject(Community.class);

                if (community != null) {
                    community.setId(communityId);
                    // Fetch the creatorId
                    String creatorId = document.getString("creatorId");

                    // Check if the current user is the creator
                    if (creatorId != null && creatorId.equals(getCurrentUserId())) {
                        // Enable or show the edit button if the user is the creator
                        editCommunityButton.setVisibility(View.VISIBLE); // Show button if creator
                    } else {
                        // Hide or disable the edit button if the user is not the creator
                        editCommunityButton.setVisibility(View.GONE); // Hide the button if not creator
                    }

                    updateUIWithCommunityData();
                }
            } else {
                // Handle the error or no data case
                Log.e("CommunityDetailActivity", "Community not found or failed to fetch.");
                Toast.makeText(this, "Community data could not be loaded.", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity if the data cannot be fetched
            }
        });
    }


    private void updateUIWithCommunityData() {
        communityNameTextView.setText(community.getName());
        communityDescriptionTextView.setText(community.getDescription());
        memberCount = community.getMemberCount();
        postCount = community.getPostCount();

        memberCountTextView.setText(String.valueOf(memberCount));
        postCountTextView.setText(String.valueOf(postCount));
        updateJoinLeaveButtonText();
        fetchPosts(community.getId());
    }


    private void fetchPosts(String communityId) {
        db.collection("posts")
                .whereEqualTo("community", communityId) // Filter by the community field
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Post post = document.toObject(Post.class);
                            if (post != null) {
                                post.setId(document.getId()); // Set Firestore document ID
                                postList.add(post); // Add post to the list
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("CommunityDetailActivity", "Error fetching posts", task.getException());
                    }
                });
    }



    private void toggleMembership(String communityId) {
        boolean isJoined = !community.isJoined();  // Toggle the joined status
        community.setJoined(isJoined);  // Update the local model to reflect the new joined status
        memberCount = isJoined ? memberCount + 1 : memberCount - 1;  // Adjust member count based on the new status

        // Update UI with new member count
        memberCountTextView.setText(String.valueOf(memberCount));
        updateJoinLeaveButtonText();  // Update the button text based on membership status

        // Update Firestore: Modify 'joined' instead of 'isJoined'
        DocumentReference communityRef = db.collection("community").document(communityId);
        communityRef.update("joined", isJoined, "memberCount", memberCount)
                .addOnSuccessListener(aVoid -> {
                    // Update the community list directly
                    fetchCommunityData(communityId);  // Re-fetch community data to reflect the updated state

                    // Update the user's joined communities list
                    updateUserJoinedCommunities(isJoined, communityId); // Call to update user's document
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update membership.", Toast.LENGTH_SHORT).show());
    }

    private void updateUserJoinedCommunities(boolean isJoined, String communityId) {
        String userId = getCurrentUserId();  // Assume you have a method to get the current user's ID
        if (userId != null) {
            DocumentReference userRef = db.collection("users").document(userId);

            if (isJoined) {
                // Add community to joinedCommunities if the user joins
                userRef.update("joinedCommunities", FieldValue.arrayUnion(communityId))
                        .addOnSuccessListener(aVoid -> Log.d("CommunityDetailActivity", "Community added to user."));
            } else {
                // Remove community from joinedCommunities if the user leaves
                userRef.update("joinedCommunities", FieldValue.arrayRemove(communityId))
                        .addOnSuccessListener(aVoid -> Log.d("CommunityDetailActivity", "Community removed from user."));
            }
        }
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
        editPostIntent.putExtra("communityId", community.getId());
        startActivityForResult(editPostIntent, EDIT_POST_REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("communityId", community.getId()); // Make sure the community ID is passed back
        resultIntent.putExtra("communityName", community.getName()); // Pass back the name if modified
        resultIntent.putExtra("communityDescription", community.getDescription()); // Pass back the description if modified
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
                String userId = data.getStringExtra("userId"); // Assuming userId is passed
                Post newPost = new Post(postTitle, postContent, userId, community.getId());
                postList.add(newPost);
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
                if (community != null && data != null) {
                    String updatedName = data.getStringExtra("communityName");
                    String updatedDescription = data.getStringExtra("communityDescription");

                    if (updatedName != null && updatedDescription != null) {
                        community.setName(updatedName);
                        community.setDescription(updatedDescription);

                        communityNameTextView.setText(updatedName);
                        communityDescriptionTextView.setText(updatedDescription);
                    } else {
                        Log.e("CommunityDetailActivity", "Received invalid data for community update.");
                    }
                }
            }

            }
        }

    private void savePost(String title, String content, String userId, String communityId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Post post = new Post(title, content, userId, communityId);

        db.collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Post added successfully!", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("title", title);
                    resultIntent.putExtra("content", content);
                    setResult(RESULT_OK, resultIntent);
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return null;
        }
    }

}
