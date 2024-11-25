package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
        communityList.clear();
        originalCommunityList.clear();
        db.collection("community")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w("CommunityActivity", "Listen failed.", e);
                        return;
                    }
                    communityList.clear();
                    originalCommunityList.clear();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        // Same logic as before to add communities to lists
                        Community community = new Community(
                                document.getId(),
                                document.getString("name"),
                                document.getString("description"),
                                document.get("memberCount", Integer.class),
                                document.get("postCount", Integer.class),
                                document.getBoolean("joined")
                        );
                        communityList.add(community);
                        originalCommunityList.add(community);
                    }
                    communityAdapter.notifyDataSetChanged();
                });
    }


    // Modified filter method
    private void filterCommunities(String query) {
        if (query.isEmpty()) {
            // If the query is empty, reset to the original list
            communityAdapter.updateList(originalCommunityList);
        } else {
            List<Community> filteredList = new ArrayList<>();
            for (Community community : originalCommunityList) {
                if (community.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(community);
                }
            }

            communityAdapter.updateList(filteredList);

            // Show a Toast for debugging purposes
            if (filteredList.isEmpty()) {
                Toast.makeText(this, "No communities found for: " + query, Toast.LENGTH_SHORT).show();
            }
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
        intent.putExtra("isJoined", community.isJoined());
        intent.putExtra("position", position);
        startActivityForResult(intent, ADD_EDIT_COMMUNITY_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_EDIT_COMMUNITY_REQUEST && resultCode == RESULT_OK && data != null) {
            String communityId = data.getStringExtra("communityId");
            String communityName = data.getStringExtra("communityName");
            String communityDescription = data.getStringExtra("communityDescription");
            int memberCount = data.getIntExtra("memberCount", -1); // Ensure you receive the updated member count
            int postCount = data.getIntExtra("postCount", -1); // Ensure you receive the updated post count
            boolean isJoined = data.getBooleanExtra("isJoined", false);
            int position = data.getIntExtra("position", -1); // Ensure you receive the position of the community

            if (position >= 0 && position < communityList.size()) {
                // Update the existing community in the list
                Community community = communityList.get(position);
                community.setName(communityName);
                community.setDescription(communityDescription);
                community.setMemberCount(memberCount);
                community.setPostCount(postCount);
                community.setJoined(isJoined);

                // Update Firestore with the new data
                updateCommunityInFirestore(community);

                // Notify the adapter that the community has been updated
                communityAdapter.notifyItemChanged(position);
            } else {
                // If position is -1, this means it's a new community, so add it
                Community newCommunity = new Community(communityId, communityName, communityDescription, memberCount, postCount, isJoined);
                newCommunity.setJoined(isJoined);
                addCommunityToFirestore(newCommunity);
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
                .set(community)  // Use set() to replace the document if it exists
                .addOnSuccessListener(aVoid -> {
                    // You could optionally notify the adapter here, though it's done in onActivityResult already
                    Log.d("CommunityActivity", "Community updated successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.w("CommunityActivity", "Error updating community.", e);
                });
    }



    public void setCurrentCommunityPosition(int position) {
        this.currentCommunityPosition = position;
    }
}