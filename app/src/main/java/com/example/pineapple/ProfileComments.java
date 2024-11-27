package com.example.pineapple;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileComments extends ProfileActivity {
    private FirebaseFirestore db;
    private RecyclerView commentRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActivityLayout(R.layout.activity_profile_comments);

        commentRecyclerView = findViewById(R.id.commentRecyclerView);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        fetchAllComments(user.getUid());
    }

    private void fetchAllComments(String userId) {
        Map<String, List<Comment>> threads = new HashMap<>();
        db.collection("posts")
                .get()
                .addOnSuccessListener(postsQuerySnapshot -> {
                    List<Comment> allComments = new ArrayList<>(); // Consolidate all comments here
                    List<Task<QuerySnapshot>> tasks = new ArrayList<>(); // Track all comment-fetching tasks

                    for (DocumentSnapshot postDocument : postsQuerySnapshot) {
                        Task<QuerySnapshot> task = db.collection("posts")
                                .document(postDocument.getId())
                                .collection("comments")
                                .whereEqualTo("userId", userId)
                                .get();
                        tasks.add(task);
                    }

                    // Wait for all tasks to complete
                    Tasks.whenAllSuccess(tasks)
                            .addOnSuccessListener(results -> {
                                for (Object result : results) {
                                    QuerySnapshot querySnapshot = (QuerySnapshot) result;
                                    for (DocumentSnapshot snapshot : querySnapshot) {
                                        Comment comment = snapshot.toObject(Comment.class);
                                        if (comment != null) {
                                            comment.setId(snapshot.getId());
                                            allComments.add(comment);
                                        }
                                    }
                                }

                                // Update the adapter with consolidated comments
                                CommentAdapter adapter = new CommentAdapter(ProfileComments.this, allComments, threads, "0", 0);
                                commentRecyclerView.setAdapter(adapter);
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Error fetching all comments", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching posts", e));
    }
}