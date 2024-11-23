package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {
    private TextView upvoteCountTextView, downvoteCountTextView;
    private ImageView upvoteIcon, downvoteIcon;
    private int upvoteCount = 0, downvoteCount = 0;
    private String postId;
    private boolean isVoting = false;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Get postId from Intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        // Log the postId to verify
        if (postId != null) {
            Log.d("PostDetailActivity", "Post ID retrieved: " + postId);
            fetchPostDetails(postId);
        } else {
            Log.e("PostDetailActivity", "Post ID is null!");
            Toast.makeText(this, "Error: Post not found!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void fetchPostDetails(String postId) {
        db.collection("posts").document(postId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Post post = documentSnapshot.toObject(Post.class);
                        if (post != null) {
                            populatePostDetails(post);
                        } else {
                            Log.e("PostDetailActivity", "Post object is null!");
                        }
                    } else {
                        Log.e("PostDetailActivity", "No document found for postId: " + postId);
                        Toast.makeText(this, "Post not found!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PostDetailActivity", "Error fetching post details", e);
                    Toast.makeText(this, "Failed to load post details.", Toast.LENGTH_SHORT).show();
                });
    }
    private void populatePostDetails(Post post) {
        TextView titleTextView = findViewById(R.id.postDetailTitle);
        TextView contentTextView = findViewById(R.id.postDetailContent);
        TextView communityTextView = findViewById(R.id.postCommunity);
        TextView usernameTextView = findViewById(R.id.userDetailName);

        titleTextView.setText(post.getTitle() != null ? post.getTitle() : "No Title");
        contentTextView.setText(post.getContent() != null ? post.getContent() : "No Content");
        communityTextView.setText(post.getCommunity() != null ? post.getCommunity() : "Unknown Community");
    }


    private void handleVote(boolean isUpvote) {
        if (isVoting) return; // Prevent spamming
        isVoting = true;

        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            isVoting = false;
            return;
        }

        db.collection("votes")
                .whereEqualTo("postId", postId)
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String existingVoteId = querySnapshot.getDocuments().get(0).getId();
                        String existingVoteType = querySnapshot.getDocuments().get(0).getString("voteType");

                        if ((isUpvote && "upvote".equals(existingVoteType)) ||
                                (!isUpvote && "downvote".equals(existingVoteType))) {
                            // Remove the vote
                            db.collection("votes").document(existingVoteId).delete().addOnSuccessListener(aVoid -> {
                                if (isUpvote && upvoteCount > 0) upvoteCount--;
                                if (!isUpvote && downvoteCount > 0) downvoteCount--;
                                updateVoteCounts();
                            }).addOnFailureListener(e -> isVoting = false);
                        } else {
                            // Switch vote
                            db.collection("votes").document(existingVoteId).update("voteType", isUpvote ? "upvote" : "downvote")
                                    .addOnSuccessListener(aVoid -> {
                                        if (isUpvote) {
                                            upvoteCount++;
                                            if (downvoteCount > 0) downvoteCount--;
                                        } else {
                                            downvoteCount++;
                                            if (upvoteCount > 0) upvoteCount--;
                                        }
                                        updateVoteCounts();
                                    }).addOnFailureListener(e -> isVoting = false);
                        }
                    } else {
                        // Add a new vote
                        Map<String, Object> voteData = new HashMap<>();
                        voteData.put("postId", postId);
                        voteData.put("userId", currentUserId);
                        voteData.put("voteType", isUpvote ? "upvote" : "downvote");

                        db.collection("votes").add(voteData).addOnSuccessListener(aVoid -> {
                            if (isUpvote) upvoteCount++;
                            else downvoteCount++;
                            updateVoteCounts();
                        }).addOnFailureListener(e -> isVoting = false);
                    }
                })
                .addOnFailureListener(e -> isVoting = false);
    }

    private void updateVoteCounts() {
        db.collection("posts").document(postId)
                .update("upvoteCount", upvoteCount, "downvoteCount", downvoteCount)
                .addOnSuccessListener(aVoid -> {
                    updateVoteUI();
                    isVoting = false;
                })
                .addOnFailureListener(e -> isVoting = false);
    }

    private void updateVoteUI() {
        upvoteCountTextView.setText(String.valueOf(upvoteCount));
        downvoteCountTextView.setText(String.valueOf(downvoteCount));
    }
}
