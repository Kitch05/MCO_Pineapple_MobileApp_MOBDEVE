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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CommunityDetailActivity extends BaseActivity {

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
    private ListenerRegistration postCountListener;


    private Community community;
    private int memberCount;
    private int postCount;
    private List<Post> postList;
    private List<Community> communityList;
    private PostAdapter postAdapter;
    private CommunityAdapter communityAdapter;
    private Context context;
    private ListenerRegistration postsListener;

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
            Toast.makeText(this, "Add Post Button Clicked", Toast.LENGTH_SHORT).show(); // Debugging Toast
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

        listenForPostCountUpdates(communityId);

        // Set up Firestore listener for posts
        postsListener = db.collection("posts")
                .whereEqualTo("community", communityId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error listening for posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        postList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Post post = document.toObject(Post.class);
                            post.setId(document.getId());
                            postList.add(post);
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void listenForPostCountUpdates(String communityId) {
        DocumentReference communityRef = db.collection("community").document(communityId);

        postCountListener = communityRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e("CommunityDetailActivity", "Error listening to post count updates", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Long updatedPostCount = snapshot.getLong("postCount");
                if (updatedPostCount != null) {
                    postCount = updatedPostCount.intValue();
                    postCountTextView.setText(String.valueOf(postCount)); // Update the UI
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (postsListener != null) {
            postsListener.remove(); // Stop listening to Firestore updates when activity is destroyed
        }
        if (postCountListener != null) {
            postCountListener.remove(); // Stop listening to Firestore updates when activity is destroyed
        }
    }


    private void fetchCommunityData(String communityId) {
        DocumentReference communityRef = db.collection("community").document(communityId);
        communityRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                community = documentSnapshot.toObject(Community.class);
                if (community != null) {
                    community.setId(documentSnapshot.getId());

                    // Update UI with community data
                    updateUIWithCommunityData();

                    // Check if the current user is the creator
                    String creatorId = documentSnapshot.getString("creatorId");
                    String currentUserId = getCurrentUserId();
                    if (creatorId != null && currentUserId != null && !creatorId.equals(currentUserId)) {
                        // Hide the edit button if the user is not the creator
                        editCommunityButton.setVisibility(View.GONE);
                    }

                    checkJoinStatus(communityId);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching community data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }



    private void updateUIWithCommunityData() {
        communityNameTextView.setText(community.getName());
        communityDescriptionTextView.setText(community.getDescription());
        memberCount = community.getMemberCount();
        postCount = community.getPostCount();

        memberCountTextView.setText(String.valueOf(memberCount));
        postCountTextView.setText(String.valueOf(postCount));
        fetchPosts(community.getId());
    }


    private void fetchPosts(String communityId) {
        db.collection("posts")
                .whereEqualTo("community", communityId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Post post = document.toObject(Post.class);
                            post.setId(document.getId());
                            postList.add(post);
                        }
                        postAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Error getting posts: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchPostCount(String communityId) {
        db.collection("posts")
                .whereEqualTo("community", communityId)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("CommunityDetailActivity", "Error fetching posts", e);
                        return;
                    }

                    if (querySnapshot != null) {
                        int postCount = querySnapshot.size(); // Get count from snapshot size
                        postCountTextView.setText(String.valueOf(postCount)); // Update UI

                        // Optionally update Firestore community document

                        updateCommunityPostCount(communityId, postCount);
                        fetchUpdatedPostCount(communityId);
                    }
                });
    }


    private void toggleMembership(String communityId) {
        String userId = getCurrentUserId();
        if (userId != null) {
            DocumentReference communityRef = db.collection("community").document(communityId);
            communityRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> members = (List<String>) documentSnapshot.get("members");
                    if (members == null) {
                        members = new ArrayList<>(); // Initialize members list if it's null
                    }
                    if (members.contains(userId)) {
                        // User is leaving the community
                        Toast.makeText(this, "Leaving community...", Toast.LENGTH_SHORT).show();
                        members.remove(userId);
                        communityRef.update("members", members, "memberCount", FieldValue.increment(-1))
                                .addOnSuccessListener(aVoid -> {
                                    joinLeaveButton.setText("Join");
                                    memberCount--;
                                    memberCountTextView.setText(String.valueOf(memberCount));
                                    Toast.makeText(this, "You have left the community.", Toast.LENGTH_SHORT).show();
                                    updateUserJoinedCommunities(false, communityId);
                                    updateUserJoinedCommunities(false, communityId);
                                    // Notify the adapter of changes
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("communityId", communityId);
                                    resultIntent.putExtra("memberCount", memberCount);
                                    setResult(RESULT_OK, resultIntent);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error updating membership: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // User is joining the community
                        Toast.makeText(this, "Joining community...", Toast.LENGTH_SHORT).show();
                        members.add(userId);
                        communityRef.update("members", members, "memberCount", FieldValue.increment(1))
                                .addOnSuccessListener(aVoid -> {
                                    joinLeaveButton.setText("Leave");
                                    memberCount++;
                                    memberCountTextView.setText(String.valueOf(memberCount));
                                    Toast.makeText(this, "You have joined the community.", Toast.LENGTH_SHORT).show();
                                    updateUserJoinedCommunities(true, communityId);
                                    // Notify the adapter of changes
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("communityId", communityId);
                                    resultIntent.putExtra("memberCount", memberCount);
                                    setResult(RESULT_OK, resultIntent);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error updating membership: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    Toast.makeText(this, "Community document does not exist", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error fetching community document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void notifyAdapterOfChanges(String communityId) {
        // Notify the adapter of changes to the community data
        for (int i = 0; i < communityList.size(); i++) {
            if (communityList.get(i).getId().equals(communityId)) {
                communityAdapter.notifyItemChanged(i); // Use the instance of your adapter
                break;
            }
        }
    }

    private void updateUserJoinedCommunities(boolean isJoined, String communityId) {
        String userId = getCurrentUserId();
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

    private void checkJoinStatus(String communityId) {
        String userId = getCurrentUserId();
        if (userId != null) {
            DocumentReference communityRef = db.collection("community").document(communityId);
            communityRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> members = (List<String>) documentSnapshot.get("members");
                    if (members != null && members.contains(userId)) {
                        joinLeaveButton.setText("Leave");
                    } else {
                        joinLeaveButton.setText("Join");
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error checking join status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
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
                Toast.makeText(this, "Post Button passed", Toast.LENGTH_SHORT).show();
                String postTitle = data.getStringExtra("title");
                String postContent = data.getStringExtra("content");
                String userId = data.getStringExtra("userId"); // Assuming userId is passed

                // Save post to Firestore
                savePost(postTitle, postContent, userId, community.getId());
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
                        Toast.makeText(this, "Failed to pass", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void savePost(String title, String content, String userId, String communityId) {
        Post post = new Post(title, content, userId, communityId);

        db.collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Post added successfully!", Toast.LENGTH_SHORT).show();
                    // The real-time listener will update the UI, no need to call fetchPosts()
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void updateCommunityPostCount(String communityId, int newPostCount) {
        DocumentReference communityRef = db.collection("community").document(communityId);
        communityRef.update("postCount", newPostCount)
                .addOnSuccessListener(aVoid -> Log.d("CommunityDetailActivity", "Post count updated in Firestore"))
                .addOnFailureListener(e -> Log.e("CommunityDetailActivity", "Failed to update post count", e));
    }

    private void fetchUpdatedPostCount(String communityId) {
        DocumentReference communityRef = db.collection("community").document(communityId);
        communityRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long updatedPostCount = documentSnapshot.getLong("postCount");
                if (updatedPostCount != null) {
                    postCountTextView.setText(String.valueOf(updatedPostCount));
                }
            }
        }).addOnFailureListener(e -> Log.e("CommunityDetailActivity", "Failed to fetch updated post count", e));
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
