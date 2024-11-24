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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class MyCommunities extends BaseActivity {

    private RecyclerView recyclerView;
    private CommunityAdapter communityAdapter;
    private List<Community> myCommunityList;
    private EditText searchBar;

    private FirebaseFirestore db;
    private static final int ADD_EDIT_COMMUNITY_REQUEST = 1;

    private List<Community> originalCommunityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActivityLayout(R.layout.activity_my_communities);

        recyclerView = findViewById(R.id.myCommunityContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        myCommunityList = new ArrayList<>();
        originalCommunityList = new ArrayList<>(); // Holds the unfiltered list
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


    private void loadMyCommunities() {
        db.collection("communities")
                .whereArrayContains("members", getCurrentUserId()) // Assuming members is an array of user IDs
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null) {
                            for (DocumentSnapshot document : documents) {
                                Community community = document.toObject(Community.class);
                                if (community != null) {
                                    // Add additional data, like member count and post count
                                    community.setId(document.getId());
                                    community.setMemberCount(document.getLong("memberCount").intValue());
                                    community.setPostCount(document.getLong("postCount").intValue());
                                    originalCommunityList.add(community); // Populate the original list
                                }
                            }
                            myCommunityList.clear();
                            myCommunityList.addAll(originalCommunityList); // Sync both lists
                            communityAdapter.notifyDataSetChanged();

                            // Toast to confirm data loading
                            Toast.makeText(this, "Loaded " + originalCommunityList.size() + " communities.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MyCommunities.this, "Error loading communities.", Toast.LENGTH_SHORT).show();
                    }
                });
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
            int position = IntStream.range(0, myCommunityList.size()).filter(i -> myCommunityList.get(i).getId().equals(communityId)).findFirst().orElse(-1);

            // Find the community by ID

            if (position != -1) {
                // Update the community in Firestore
                Community updatedCommunity = myCommunityList.get(position);
                updatedCommunity.setMemberCount(data.getIntExtra("memberCount", updatedCommunity.getMemberCount()));
                updatedCommunity.setPostCount(data.getIntExtra("postCount", updatedCommunity.getPostCount()));

                // Update Firestore document
                DocumentReference communityRef = db.collection("communities").document(communityId);
                communityRef.update("memberCount", updatedCommunity.getMemberCount(), "postCount", updatedCommunity.getPostCount())
                        .addOnSuccessListener(aVoid -> communityAdapter.notifyItemChanged(position))
                        .addOnFailureListener(e -> Toast.makeText(MyCommunities.this, "Error updating community.", Toast.LENGTH_SHORT).show());
            }
        }
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
