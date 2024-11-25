package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyCommunities extends BaseActivity {

    private RecyclerView recyclerView;
    private CommunityAdapter communityAdapter;
    private List<Community> myCommunityList;
    private EditText searchBar;

    private static final int ADD_EDIT_COMMUNITY_REQUEST = 1;

    private FirebaseFirestore db;

    private List<Community> originalCommunityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActivityLayout(R.layout.activity_my_communities);

        recyclerView = findViewById(R.id.myCommunityContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        myCommunityList = new ArrayList<>();
        originalCommunityList = new ArrayList<>();
        communityAdapter = new CommunityAdapter(this, myCommunityList, this::launchCommunityDetail);
        recyclerView.setAdapter(communityAdapter);

        searchBar = findViewById(R.id.searchBar);

        // Add TextWatcher for search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCommunities(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        loadMyCommunities();
    }

    private void loadMyCommunities() {
        String userId = getCurrentUserId();
        if (userId != null) {
            // Fetch the user's joined communities from the users collection
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot userDocument = task.getResult();
                            if (userDocument != null && userDocument.exists()) {
                                List<String> joinedCommunities = (List<String>) userDocument.get("joinedCommunities");
                                if (joinedCommunities != null && !joinedCommunities.isEmpty()) {
                                    // Query the communities using the document IDs (community IDs) in the joinedCommunities list
                                    List<Community> communitiesToLoad = new ArrayList<>();
                                    for (String communityId : joinedCommunities) {
                                        db.collection("community").document(communityId).get()
                                                .addOnCompleteListener(communityTask -> {
                                                    if (communityTask.isSuccessful()) {
                                                        DocumentSnapshot document = communityTask.getResult();
                                                        if (document != null && document.exists()) {
                                                            Community community = document.toObject(Community.class);
                                                            if (community != null) {
                                                                community.setId(document.getId());
                                                                community.setMemberCount(document.getLong("memberCount").intValue());
                                                                community.setPostCount(document.getLong("postCount").intValue());
                                                                communitiesToLoad.add(community);
                                                            }
                                                        }
                                                    }
                                                    // Once all communities are fetched, update the list
                                                    if (communitiesToLoad.size() == joinedCommunities.size()) {
                                                        originalCommunityList.clear();
                                                        originalCommunityList.addAll(communitiesToLoad);
                                                        myCommunityList.clear();
                                                        myCommunityList.addAll(communitiesToLoad);
                                                        communityAdapter.notifyDataSetChanged();

                                                        // Toast to confirm data loading
                                                        Toast.makeText(MyCommunities.this, "Loaded " + communitiesToLoad.size() + " communities.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(MyCommunities.this, "No communities found for the user.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(MyCommunities.this, "Error fetching user data.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }



    private void filterCommunities(String query) {
        if (query.isEmpty()) {
            // Reset the main list to the full list
            myCommunityList.clear();
            myCommunityList.addAll(originalCommunityList);
        } else {
            // Filter the original list
            List<Community> filteredList = new ArrayList<>();
            for (Community community : originalCommunityList) {
                if (community.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(community);
                }
            }

            // Show Toast for debugging
            if (filteredList.isEmpty()) {
                Toast.makeText(this, "No communities found for: " + query, Toast.LENGTH_SHORT).show();
            }

            myCommunityList.clear();
            myCommunityList.addAll(filteredList);
        }

        // Notify the adapter of data changes
        communityAdapter.notifyDataSetChanged();
    }

    private void launchCommunityDetail(int position) {
        Community community = myCommunityList.get(position);
        Intent intent = new Intent(MyCommunities.this, CommunityDetailActivity.class);
        intent.putExtra("communityName", community.getName());
        intent.putExtra("communityDescription", community.getDescription());
        intent.putExtra("memberCount", community.getMemberCount());
        intent.putExtra("postCount", community.getPostCount());
        intent.putExtra("communityId", community.getId()); // Pass the community ID
        startActivityForResult(intent, ADD_EDIT_COMMUNITY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_EDIT_COMMUNITY_REQUEST && resultCode == RESULT_OK && data != null) {
            String communityId = data.getStringExtra("communityId");
            int position = findCommunityPositionById(communityId);

            if (position != -1) {
                // Update the community in Firestore
                Community updatedCommunity = myCommunityList.get(position);
                updatedCommunity.setMemberCount(data.getIntExtra("memberCount", updatedCommunity.getMemberCount()));
                updatedCommunity.setPostCount(data.getIntExtra("postCount", updatedCommunity.getPostCount()));

                // Update Firestore document
                db.collection("community").document(communityId)
                        .update("memberCount", updatedCommunity.getMemberCount(), "postCount", updatedCommunity.getPostCount())
                        .addOnSuccessListener(aVoid -> communityAdapter.notifyItemChanged(position))
                        .addOnFailureListener(e -> Toast.makeText(MyCommunities.this, "Error updating community.", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private int findCommunityPositionById(String communityId) {
        for (int i = 0; i < myCommunityList.size(); i++) {
            if (myCommunityList.get(i).getId().equals(communityId)) {
                return i;
            }
        }
        return -1;
    }

    private String getCurrentUserId() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        // Get the current user ID (UID) if the user is logged in
        if (firebaseAuth.getCurrentUser() != null) {
            return firebaseAuth.getCurrentUser().getUid();
        } else {
            return null; // Return null if the user is not logged in
        }
    }
}
