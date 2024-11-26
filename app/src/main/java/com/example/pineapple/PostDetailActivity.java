package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {
    private static final String TAG = "PostDetailActivity";

    private TextView postTitle, postContent, postCommunity, postAuthor;
    private EditText commentInput;
    private Button submitCommentButton;
    private RecyclerView commentRecyclerView;

    private String postId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        postTitle = findViewById(R.id.postDetailTitle);
        postContent = findViewById(R.id.postDetailContent);
        postCommunity = findViewById(R.id.postCommunity);
        postAuthor = findViewById(R.id.userDetailName);
        commentInput = findViewById(R.id.commentInput);
        submitCommentButton = findViewById(R.id.submitCommentButton);
        commentRecyclerView = findViewById(R.id.commentRecyclerView);

        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get postId from Intent
        postId = getIntent().getStringExtra("postId");

        if (postId == null) {
            Toast.makeText(this, "Error: Post not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load post details and comments
        fetchPostDetails();
        fetchComments();

        // Set up submit button
        submitCommentButton.setOnClickListener(v -> {
            String content = commentInput.getText().toString().trim();
            if (!content.isEmpty()) {
                submitComment(content, null); // Pass null for parentId to indicate a top-level comment
            } else {
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPostDetails() {
        db.collection("posts").document(postId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Post post = documentSnapshot.toObject(Post.class);
                        if (post != null) {
                            populatePostDetails(post);
                        } else {
                            Toast.makeText(this, "Error loading post details.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching post details", e));
    }

    private void populatePostDetails(Post post) {
        postTitle.setText(post.getTitle());
        postContent.setText(post.getContent());

        // Fetch the community name using the community ID (post.getCommunity())
        String communityId = post.getCommunity();
        db.collection("community").document(communityId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String communityName = documentSnapshot.getString("name");  // Fetch the community name
                        if (communityName != null) {
                            postCommunity.setText(communityName);  // Set the community name in the TextView
                        } else {
                            postCommunity.setText("Unknown Community");
                        }
                    } else {
                        postCommunity.setText("Community not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching community name", e);
                    postCommunity.setText("Error loading community");
                });

        // Fetch the author's username
        db.collection("users").document(post.getUserId()).get()
                .addOnSuccessListener(userSnapshot -> {
                    String username = userSnapshot.getString("username");
                    postAuthor.setText(username != null ? username : "Unknown User");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching username", e));
    }


    private void submitComment(String content, String parentId) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            Toast.makeText(this, "Please log in to comment.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new Comment object
        Comment newComment = new Comment(content, currentUserId, parentId, System.currentTimeMillis());

        // Add the comment to Firestore
        db.collection("posts").document(postId).collection("comments")
                .add(newComment)
                .addOnSuccessListener(documentReference -> {
                    // Update the Firestore document with its generated ID
                    String commentId = documentReference.getId();
                    db.collection("posts").document(postId)
                            .collection("comments").document(commentId)
                            .update("id", commentId)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Comment submitted successfully!", Toast.LENGTH_SHORT).show();
                                commentInput.setText(""); // Clear the input field
                                fetchComments(); // Refresh comments
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating comment ID", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error submitting comment", e));
    }


    private void fetchComments() {
        db.collection("posts").document(postId).collection("comments")
                .orderBy("timestamp") // Optional: Sort comments by timestamp
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> allComments = new ArrayList<>();
                    Map<String, List<Comment>> threads = new HashMap<>();

                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Comment comment = snapshot.toObject(Comment.class);
                        if (comment != null) {
                            comment.setId(snapshot.getId()); // Set the Firestore document ID as the comment ID
                            allComments.add(comment);

                            // Build a map of comment threads
                            if (comment.getParentId() != null) {
                                threads.computeIfAbsent(comment.getParentId(), k -> new ArrayList<>()).add(comment);
                            }
                        }
                    }

                    // Update the adapter with top-level comments and threads
                    List<Comment> topLevelComments = new ArrayList<>();
                    for (Comment comment : allComments) {
                        if (comment.getParentId() == null) {
                            topLevelComments.add(comment);
                        }
                    }

                    CommentAdapter adapter = new CommentAdapter(PostDetailActivity.this, topLevelComments, threads, postId, 0);
                    commentRecyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching comments", e));
    }


    private void displayComments(List<Comment> comments) {
        // Build a map of comment threads
        Map<String, List<Comment>> commentThreads = new HashMap<>();
        for (Comment comment : comments) {
            if (comment.getParentId() == null) {
                // Top-level comments
                commentThreads.computeIfAbsent("topLevel", k -> new ArrayList<>()).add(comment);
            } else {
                // Replies to comments
                commentThreads.computeIfAbsent(comment.getParentId(), k -> new ArrayList<>()).add(comment);
            }
        }

        // Create the adapter using top-level comments and commentThreads
        List<Comment> topLevelComments = commentThreads.get("topLevel"); // Retrieve top-level comments
        if (topLevelComments == null) {
            topLevelComments = new ArrayList<>(); // Ensure no null pointer exceptions
        }

        CommentAdapter adapter = new CommentAdapter(this, topLevelComments, commentThreads, postId, 0);
        commentRecyclerView.setAdapter(adapter);
    }

}
