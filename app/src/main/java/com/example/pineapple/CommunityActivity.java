package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CommunityActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommunityAdapter communityAdapter;
    private List<Community> communityList;
    private Button createCommunityButton; // Keep only the create community button
    private EditText searchBar; // Search bar
    private int currentCommunityPosition = -1; // To track selected community position

    // Define a request code for launching the add/edit activity
    private static final int ADD_EDIT_COMMUNITY_REQUEST = 1; // Use a constant value for the request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        // Initialize RecyclerView and set up layout manager
        recyclerView = findViewById(R.id.communityContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize community list and load communities
        communityList = new ArrayList<>();
        loadCommunities();

        // Set up the adapter
        communityAdapter = new CommunityAdapter(this, communityList, this::launchCommunityDetail);
        recyclerView.setAdapter(communityAdapter);

        // Initialize create community button and search bar
        createCommunityButton = findViewById(R.id.createCommunityButton);
        searchBar = findViewById(R.id.searchBar);

        // Set up create community button listener
        createCommunityButton.setOnClickListener(v -> {
            Intent intent = new Intent(CommunityActivity.this, AddEditCommunityActivity.class);
            startActivityForResult(intent, ADD_EDIT_COMMUNITY_REQUEST); // Use startActivityForResult for receiving data back
        });

        // Set up search bar functionality (optional)
        searchBar.setOnEditorActionListener((textView, actionId, event) -> {
            // Implement search functionality here
            return false;
        });
    }

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
            int position = data.getIntExtra("position", -1);

            if (position == -1) {
                // Add new community
                Community newCommunity = new Community(communityName, communityDescription);
                communityList.add(newCommunity); // communityList is your list of communities
            } else {
                // Update existing community (assuming you implement a suitable update mechanism in the Community class)
                Community existingCommunity = communityList.get(position);
                existingCommunity.setMemberCount(data.getIntExtra("memberCount", existingCommunity.getMemberCount()));
                existingCommunity.setPostCount(data.getIntExtra("postCount", existingCommunity.getPostCount()));
            }

            // Notify the adapter of data changes
            communityAdapter.notifyDataSetChanged();
        }
    }

    // Method to update currentCommunityPosition
    public void setCurrentCommunityPosition(int position) {
        this.currentCommunityPosition = position;
    }

    private void loadCommunities() {
        // Create a sample User object
        User creator = new User("CreatorUser", R.drawable.placeholder_image); // Replace with a valid image resource ID

        communityList.add(new Community("Tech Enthusiasts", "A community for tech lovers.", 0, 0, creator));
        communityList.add(new Community("Music Fans", "Share and discuss your favorite music.", 0, 0, creator));
        communityList.add(new Community("Fitness Freaks", "A group for fitness enthusiasts.", 0, 0, creator));
    }
}
