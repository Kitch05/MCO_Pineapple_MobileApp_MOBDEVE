package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
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
    private int currentCommunityPosition = -1;

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
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot userDocument = task.getResult();
                            if (userDocument != null && userDocument.exists()) {
                                List<String> joinedCommunities = (List<String>) userDocument.get("joinedCommunities");
                                if (joinedCommunities != null && !joinedCommunities.isEmpty()) {
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
                                                                community.setJoined(true); // Mark as joined
                                                                communitiesToLoad.add(community);
                                                            }
                                                        }
                                                    }
                                                    if (communitiesToLoad.size() == joinedCommunities.size()) {
                                                        originalCommunityList.clear();
                                                        originalCommunityList.addAll(communitiesToLoad);
                                                        myCommunityList.clear();
                                                        myCommunityList.addAll(communitiesToLoad);
                                                        communityAdapter.notifyDataSetChanged();
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
        if (position < 0 || position >= myCommunityList.size()) {
            Toast.makeText(MyCommunities.this, "Invalid community position", Toast.LENGTH_SHORT).show();
            return;
        }

        Community community = myCommunityList.get(position);
        Toast.makeText(MyCommunities.this, "Opening community: " + community.getName(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MyCommunities.this, CommunityDetailActivity.class);
        intent.putExtra("communityId", community.getId()); // Pass the community ID
        intent.putExtra("communityName", community.getName());
        intent.putExtra("communityDescription", community.getDescription());
        intent.putExtra("memberCount", community.getMemberCount());
        intent.putExtra("isJoined", community.isJoined());
        intent.putExtra("postCount", community.getPostCount());

        // Log the data being passed to confirm
        Log.d("MyCommunities", "Intent Data: " + community.getName() + ", ID: " + community.getId());

        startActivityForResult(intent, ADD_EDIT_COMMUNITY_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String communityId = data.getStringExtra("communityId");
            boolean isJoined = data.getBooleanExtra("isJoined", false);
            int updatedMemberCount = data.getIntExtra("memberCount", -1);

            // Update community data
            for (Community community : myCommunityList) {
                if (community.getId().equals(communityId)) {
                    community.setJoined(isJoined);
                    community.setMemberCount(updatedMemberCount);
                    break;
                }
            }

            // Notify adapter to refresh the UI
            communityAdapter.notifyDataSetChanged();
        }
    }


    private void updateUserJoinedCommunities(boolean isJoined, String communityId) {
        String userId = getCurrentUserId();
        if (userId != null) {
            db.collection("users").document(userId)
                    .update("joinedCommunities", isJoined ? FieldValue.arrayUnion(communityId) : FieldValue.arrayRemove(communityId))
                    .addOnSuccessListener(aVoid -> {
                        // Do not reload all communities here; rely on the local update instead
                        Toast.makeText(MyCommunities.this, "User data updated.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(MyCommunities.this, "Error updating user data.", Toast.LENGTH_SHORT).show());
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

    public void setCurrentCommunityPosition(int position) {
        this.currentCommunityPosition = position;
    }
}
