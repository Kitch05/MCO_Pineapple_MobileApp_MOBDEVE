package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

public class CommunityActivity extends BaseActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private CommunityAdapter communityAdapter;
    private List<Community> communityList;
    private Button createCommunityButton;
    private EditText searchBar;
    private int currentCommunityPosition = -1; // To track selected community position

    private static final int ADD_EDIT_COMMUNITY_REQUEST = 1;

    private List<Community> originalCommunityList; // Keep the unfiltered full list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityLayout(R.layout.activity_community);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.communityContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        communityList = new ArrayList<>();
        originalCommunityList = new ArrayList<>(); // Initialize the full list

        loadCommunitiesFromFirestore();

        communityAdapter = new CommunityAdapter(this, communityList, this::launchCommunityDetail);
        recyclerView.setAdapter(communityAdapter);

        createCommunityButton = findViewById(R.id.createCommunityButton);
        searchBar = findViewById(R.id.searchBar);

        createCommunityButton.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityActivity.this, AddEditCommunityActivity.class);
            startActivityForResult(intent, ADD_EDIT_COMMUNITY_REQUEST);
        });

        // Add a listener to the search bar
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCommunities(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // No action needed here
            }
        });
    }

    private void loadCommunitiesFromFirestore() {
        db.collection("community")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        communityList.clear();
                        originalCommunityList.clear(); // Clear and repopulate the original list

                        for (DocumentSnapshot document : task.getResult()) {
                            Community community = document.toObject(Community.class);
                            community.setId(document.getId());
                            communityList.add(community);
                            originalCommunityList.add(community); // Add to the original list
                        }

                        communityAdapter.notifyDataSetChanged();
                    } else {
                        Log.w("CommunityActivity", "Error getting communities.", task.getException());
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadCommunitiesFromFirestore(); // Reload community data
    }

    // Modified filter method
    private void filterCommunities(String query) {
        if (query.isEmpty()) {
            // Reset to the original list
            communityList.clear();
            communityList.addAll(originalCommunityList);
        } else {
            List<Community> filteredList = new ArrayList<>();
            for (Community community : originalCommunityList) {
                if (community.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(community);
                }
            }

            communityList.clear();
            communityList.addAll(filteredList);
        }
        communityAdapter.notifyDataSetChanged();

        // Show a Toast for debugging purposes
        if (communityList.isEmpty()) {
            Toast.makeText(this, "No communities found for: " + query, Toast.LENGTH_SHORT).show();
        }
    }


    private void launchCommunityDetail(int position) {
        Community community = communityList.get(position);
        Intent intent = new Intent(CommunityActivity.this, CommunityDetailActivity.class);
        intent.putExtra("communityId", community.getId()); // Pass the ID
        intent.putExtra("communityName", community.getName());
        intent.putExtra("communityDescription", community.getDescription());
        intent.putExtra("memberCount", community.getMemberCount());
        intent.putExtra("postCount", community.getPostCount());
        intent.putExtra("position", position);
        startActivityForResult(intent, ADD_EDIT_COMMUNITY_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            String communityId = data.getStringExtra("communityId");
            int updatedMemberCount = data.getIntExtra("memberCount", -1);

            // Find and update the community in the list
            for (int i = 0; i < communityList.size(); i++) {
                Community community = communityList.get(i);
                if (community.getId().equals(communityId)) {
                    if (updatedMemberCount == 0) {
                        // Remove the community if the user has left it
                        communityList.remove(i);
                        communityAdapter.notifyItemRemoved(i);
                        Toast.makeText(this, "Community removed from list.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Update the community details if still joined
                        community.setMemberCount(updatedMemberCount);
                        communityAdapter.notifyItemChanged(i);
                        Toast.makeText(this, "Community updated in list.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
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
                .set(community)  // Use set() to update the community
                .addOnSuccessListener(aVoid -> {
                    Log.d("CommunityActivity", "Community updated successfully.");
                    Toast.makeText(this, "Community updated in Firestore: " + community.getName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.w("CommunityActivity", "Error updating community.", e);
                    Toast.makeText(this, "Error updating community in Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    public void setCurrentCommunityPosition(int position) {
        this.currentCommunityPosition = position;
    }
}