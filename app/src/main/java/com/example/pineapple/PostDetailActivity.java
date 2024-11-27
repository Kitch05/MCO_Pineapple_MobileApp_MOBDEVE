package com.example.pineapple;

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
import com.google.firebase.firestore.FieldValue;
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

        db = FirebaseFirestore.getInstance();
        initializeViews();

        postId = getIntent().getStringExtra("postId");

        if (postId == null || postId.isEmpty()) {
            showToast("Error: Invalid post ID");
            finish();
            return;
        }

        // Load post details and comments
        fetchPostDetails();
        fetchComments();

        // Listen for real-time changes
        listenForCommentChanges();

        submitCommentButton.setOnClickListener(v -> {
            String content = commentInput.getText().toString().trim();
            if (!content.isEmpty()) {
                submitComment(content, null);
            } else {
                showToast("Comment cannot be empty");
            }
        });
    }

    private void initializeViews() {
        postTitle = findViewById(R.id.postDetailTitle);
        postContent = findViewById(R.id.postDetailContent);
        postCommunity = findViewById(R.id.postCommunity);
        postAuthor = findViewById(R.id.userDetailName);
        commentInput = findViewById(R.id.commentInput);
        submitCommentButton = findViewById(R.id.submitCommentButton);
        commentRecyclerView = findViewById(R.id.commentRecyclerView);
        ImageView backButton = findViewById(R.id.backButton);

        // Back button functionality
        backButton.setOnClickListener(v -> onBackPressed());

        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void fetchPostDetails() {
        db.collection("posts").document(postId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Post post = documentSnapshot.toObject(Post.class);
                        if (post != null) {
                            populatePostDetails(post);
                        } else {
                            showToast("Error loading post details.");
                        }
                    } else {
                        showToast("Post not found.");
                        finish();
                    }
                })
                .addOnFailureListener(e -> logError("Error fetching post details", e));
    }

    private void populatePostDetails(Post post) {
        postTitle.setText(post.getTitle());
        postContent.setText(post.getContent());

        fetchCommunityName(post.getCommunity());
        fetchAuthorUsername(post.getUserId());
    }

    private void fetchCommunityName(String communityId) {
        db.collection("community").document(communityId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String communityName = documentSnapshot.getString("name");
                    postCommunity.setText(communityName != null ? communityName : "Unknown Community");
                })
                .addOnFailureListener(e -> {
                    logError("Error fetching community name", e);
                    postCommunity.setText("Error loading community");
                });
    }

    private void fetchAuthorUsername(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(userSnapshot -> {
                    String username = userSnapshot.getString("username");
                    postAuthor.setText(username != null ? username : "Unknown User");
                })
                .addOnFailureListener(e -> logError("Error fetching username", e));
    }

    private void submitComment(String content, String parentId) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            showToast("Please log in to comment.");
            return;
        }

        Comment newComment = new Comment(content, currentUserId, parentId, System.currentTimeMillis());

        db.collection("posts").document(postId).collection("comments")
                .add(newComment)
                .addOnSuccessListener(documentReference -> {
                    String commentId = documentReference.getId();
                    updateCommentIdAndPostCount(commentId); // Update comment count after adding comment
                })
                .addOnFailureListener(e -> logError("Error submitting comment", e));
    }

    private void updateCommentIdAndPostCount(String commentId) {
        // Update the comment ID
        db.collection("posts").document(postId).collection("comments").document(commentId)
                .update("id", commentId)
                .addOnSuccessListener(aVoid -> {
                    // Increment commentCount in the post
                    db.collection("posts").document(postId)
                            .update("commentCount", FieldValue.increment(1))
                            .addOnSuccessListener(aVoid2 -> {
                                commentInput.setText(""); // Clear input field
                                fetchComments(); // Refresh comments
                            })
                            .addOnFailureListener(e -> logError("Error updating comment count", e));
                })
                .addOnFailureListener(e -> logError("Error updating comment ID", e));
    }
    private void listenForCommentChanges() {
        db.collection("posts").document(postId).collection("comments")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening for comments", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Comment> allComments = new ArrayList<>();
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            Comment comment = snapshot.toObject(Comment.class);
                            if (comment != null) {
                                comment.setId(snapshot.getId());
                                allComments.add(comment);
                            }
                        }

                        int totalComments = countNestedComments(null, allComments);

                        db.collection("posts").document(postId)
                                .update("commentCount", totalComments)
                                .addOnSuccessListener(aVoid -> {
                                });
                    }
                });
    }

    private int countNestedComments(String parentId, List<Comment> allComments) {
        int count = 0;
        for (Comment comment : allComments) {
            if ((parentId == null && comment.getParentId() == null) ||
                    (parentId != null && parentId.equals(comment.getParentId()))) {
                count += 1;
                count += countNestedComments(comment.getId(), allComments);
            }
        }
        return count;
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
                            comment.setId(snapshot.getId());
                            allComments.add(comment);

                            // Build a map of comment threads
                            if (comment.getParentId() != null) {
                                threads.computeIfAbsent(comment.getParentId(), k -> new ArrayList<>()).add(comment);
                            }
                        }
                    }

                    List<Comment> topLevelComments = new ArrayList<>();
                    for (Comment comment : allComments) {
                        if (comment.getParentId() == null) {
                            topLevelComments.add(comment);
                        }
                    }

                    CommentAdapter adapter = new CommentAdapter(
                            PostDetailActivity.this, topLevelComments, threads, postId, 0
                    );
                    commentRecyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> logError("Error fetching comments", e));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void logError(String message, Exception e) {
        Log.e(TAG, message, e);
    }
}
