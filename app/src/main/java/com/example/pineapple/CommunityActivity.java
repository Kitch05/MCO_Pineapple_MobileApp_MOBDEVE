package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;

public class CommunityActivity extends BaseActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private CommunityAdapter communityAdapter;
    private List<Community> communityList;
    private Button createCommunityButton;
    private EditText searchBar;
    private int currentCommunityPosition = -1; // To track selected community position

    private static final int ADD_EDIT_COMMUNITY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityLayout(R.layout.activity_community);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.communityContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        communityList = new ArrayList<>();
        loadCommunitiesFromFirestore();

        communityAdapter = new CommunityAdapter(this, communityList, this::launchCommunityDetail);
        recyclerView.setAdapter(communityAdapter);

        createCommunityButton = findViewById(R.id.createCommunityButton);
        searchBar = findViewById(R.id.searchBar);

        createCommunityButton.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityActivity.this, AddEditCommunityActivity.class);
            startActivityForResult(intent, ADD_EDIT_COMMUNITY_REQUEST);
        });

        searchBar.setOnEditorActionListener((textView, actionId, event) -> {
            return false;
        });
    }

    // Load communities from Firestore
    private void loadCommunitiesFromFirestore() {
        communityList.clear();  // Clear existing list before fetching new data
        db.collection("community")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null) {
                            for (DocumentSnapshot document : snapshot.getDocuments()) {
                                String id = document.getId();
                                String name = document.getString("name");
                                String description = document.getString("description");

                                CollectionReference membersRef = document.getReference().collection("members");
                                membersRef.get().addOnCompleteListener(membersTask -> {
                                    if (membersTask.isSuccessful()) {
                                        int memberCount = membersTask.getResult().size();
                                        CollectionReference postsRef = document.getReference().collection("posts");
                                        postsRef.get().addOnCompleteListener(postsTask -> {
                                            if (postsTask.isSuccessful()) {
                                                int postCount = postsTask.getResult().size();
                                                Community community = new Community(id, name, description, memberCount, postCount);
                                                communityList.add(community);
                                                communityAdapter.notifyDataSetChanged();
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    } else {
                        Log.w("CommunityActivity", "Error getting communities.", task.getException());
                    }
                });
    }


    // Launch community detail view
    private void launchCommunityDetail(int position) {
        Community community = communityList.get(position);
        Intent intent = new Intent(CommunityActivity.this, CommunityDetailActivity.class);
        intent.putExtra("communityName", community.getName());
        intent.putExtra("communityDescription", community.getDescription());
        intent.putExtra("memberCount", community.getMemberCount());
        intent.putExtra("postCount", community.getPostCount());
        intent.putExtra("position", position);
        startActivityForResult(intent, ADD_EDIT_COMMUNITY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_EDIT_COMMUNITY_REQUEST && resultCode == RESULT_OK && data != null) {
            String communityName = data.getStringExtra("communityName");
            String communityDescription = data.getStringExtra("communityDescription");

            // If no position, it's a new community, so add it to the list
            if (currentCommunityPosition == -1) {
                Community newCommunity = new Community(communityName, communityDescription);
                addCommunityToFirestore(newCommunity);  // Add new community and notify adapter
            } else {
                // Update existing community
                Community existingCommunity = communityList.get(currentCommunityPosition);
                existingCommunity.setName(communityName);
                existingCommunity.setDescription(communityDescription);
                updateCommunityInFirestore(existingCommunity);  // Update existing community
            }

            // Refresh the list from Firestore after adding or editing
            loadCommunitiesFromFirestore();  // Fetch updated list from Firestore
        }
    }


    private void addCommunityToFirestore(Community community) {
        // Check if the community already exists in Firestore by name or other unique identifier
        db.collection("community")
                .whereEqualTo("name", community.getName())  // Assuming 'name' is unique
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null && snapshot.size() > 0) {
                            // Community already exists, do not add it again
                            Log.d("CommunityActivity", "Community already exists.");
                        } else {
                            // Community does not exist, proceed to add it
                            db.collection("community")
                                    .add(community)
                                    .addOnSuccessListener(documentReference -> {
                                        community.setId(documentReference.getId());  // Set Firestore document ID

                                        // Add the new community to the local list
                                        communityList.add(community);

                                        // Notify the adapter that a new item has been inserted
                                        communityAdapter.notifyItemInserted(communityList.size() - 1);  // Notify the adapter of the new item

                                        // Optionally scroll to the new community
                                        recyclerView.scrollToPosition(communityList.size() - 1);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("CommunityActivity", "Error adding community.", e);
                                    });
                        }
                    }
                });
    }



    private void updateCommunityInFirestore(Community community) {
        db.collection("community")
                .document(community.getId())
                .set(community)  // Use set() to replace the document if it exists
                .addOnSuccessListener(aVoid -> {
                    // Notify the adapter that the community has been updated
                    communityAdapter.notifyItemChanged(currentCommunityPosition);
                })
                .addOnFailureListener(e -> {
                    Log.w("CommunityActivity", "Error updating community.", e);
                });
    }



    public void setCurrentCommunityPosition(int position) {
        this.currentCommunityPosition = position;
    }
}