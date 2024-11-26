package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private BottomNavigationView navbar;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private List<Post> originalPostList; // Full unfiltered list
    private Button addPostButton;
    private ActivityResultLauncher<Intent> addEditPostLauncher;
    private EditText searchBar;

    // Firestore instance and collection reference
    private FirebaseFirestore db;
    private CollectionReference postsCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActivityLayout(R.layout.activity_main);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        postsCollection = db.collection("posts");

        recyclerView = findViewById(R.id.contentContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        originalPostList = new ArrayList<>(); // Initialize unfiltered list
        postAdapter = new PostAdapter(this, postList, this::launchEditPost, this::launchPostDetail);
        recyclerView.setAdapter(postAdapter);

        addPostButton = findViewById(R.id.addPostButton);
        addPostButton.setOnClickListener(v -> launchAddPost());

        searchBar = findViewById(R.id.searchBar);

        // Load posts from Firestore
        loadPostsFromFirestore();

        // Register for activity results
        addEditPostLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String title = data.getStringExtra("title");
                        String content = data.getStringExtra("content");
                        String community = data.getStringExtra("community");
                        String userId = data.getStringExtra("userId");
                        int position = data.getIntExtra("position", -1);

                        if (position == -1) {
                            Post newPost = new Post(title, content, userId, community);
                            savePostToFirestore(newPost);
                        } else {
                            Post post = postList.get(position);
                            post.setTitle(title);
                            post.setContent(content);
                            post.setCommunity(community);
                            updatePostInFirestore(post);
                        }
                    }
                }
        );

        // Add search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPosts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });
    }

    private void launchAddPost() {
        Intent intent = new Intent(MainActivity.this, AddEditPostActivity.class);
        addEditPostLauncher.launch(intent);
    }

    private void launchEditPost(int position) {
        Post post = postList.get(position);
        Intent intent = new Intent(MainActivity.this, AddEditPostActivity.class);
        intent.putExtra("title", post.getTitle());
        intent.putExtra("content", post.getContent());
        intent.putExtra("community", post.getCommunity());
        intent.putExtra("position", position);
        intent.putExtra("userId", post.getUserId());
        addEditPostLauncher.launch(intent);
    }

    private void launchPostDetail(int position) {
        Post post = postList.get(position);
        Intent intent = new Intent(MainActivity.this, PostDetailActivity.class);
        intent.putExtra("title", post.getTitle());
        intent.putExtra("content", post.getContent());
        intent.putExtra("community", post.getCommunity());
        intent.putExtra("userId", post.getUserId());
        startActivity(intent);
    }

    private void loadPostsFromFirestore() {
        postsCollection.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(this, "Error loading posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            postList.clear();
            originalPostList.clear();
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                Post post = document.toObject(Post.class);
                if (post != null) {
                    post.setId(document.getId()); // Save Firestore document ID
                    postList.add(post);
                    originalPostList.add(post);
                }
            }

            postAdapter.notifyDataSetChanged();
        });
    }

    private void savePostToFirestore(Post post) {
        postsCollection.add(post)
                .addOnSuccessListener(documentReference -> {
                    post.setId(documentReference.getId());
                    postList.add(0, post);
                    originalPostList.add(0, post); // Keep the original list updated
                    postAdapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updatePostInFirestore(Post post) {
        postsCollection.document(post.getId())
                .set(post)
                .addOnSuccessListener(aVoid -> {
                    int position = postList.indexOf(post);
                    if (position != -1) {
                        postAdapter.notifyItemChanged(position);
                    }
                    Toast.makeText(this, "Post updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void filterPosts(String query) {
        if (query.isEmpty()) {
            postAdapter.updateList(originalPostList); // Reset to the original list
        } else {
            List<Post> filteredList = new ArrayList<>();
            for (Post post : originalPostList) {
                if (post.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        (post.getContent() != null && post.getContent().toLowerCase().contains(query.toLowerCase()))) {
                    filteredList.add(post);
                }
            }
            postAdapter.updateList(filteredList);

            if (filteredList.isEmpty()) {
                Toast.makeText(this, "No posts found for: " + query, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
