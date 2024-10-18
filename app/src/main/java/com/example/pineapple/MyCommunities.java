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

public class MyCommunities extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommunityAdapter communityAdapter;
    private List<Community> myCommunityList;
    private Button createCommunityButton; // Keep only the create community button
    private EditText searchBar; // Search bar
    private static final int ADD_EDIT_COMMUNITY_REQUEST = 1; // Use a constant value for the request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_communities);

        // Initialize RecyclerView and set up layout manager
        recyclerView = findViewById(R.id.myCommunityContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize my community list and load communities
        myCommunityList = new ArrayList<>();
        loadMyCommunities();

        // Set up the adapter
        communityAdapter = new CommunityAdapter(this, myCommunityList, this::launchCommunityDetail);
        recyclerView.setAdapter(communityAdapter);

        // Initialize create community button and search bar
        createCommunityButton = findViewById(R.id.createCommunityButton);
        searchBar = findViewById(R.id.searchBar);

        // Set up create community button listener
        createCommunityButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyCommunities.this, AddEditCommunityActivity.class);
            startActivityForResult(intent, ADD_EDIT_COMMUNITY_REQUEST); // Use startActivityForResult for receiving data back
        });
    }

    private void launchCommunityDetail(int position) {
        Community community = myCommunityList.get(position);
        Intent intent = new Intent(MyCommunities.this, CommunityDetailActivity.class);
        intent.putExtra("communityName", community.getName());
        intent.putExtra("communityDescription", community.getDescription());
        intent.putExtra("memberCount", community.getMemberCount());
        intent.putExtra("postCount", community.getPostCount());
        startActivity(intent); // Launch community detail activity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_EDIT_COMMUNITY_REQUEST && resultCode == RESULT_OK && data != null) {
            String communityName = data.getStringExtra("communityName");
            String communityDescription = data.getStringExtra("communityDescription");

            // Create new community
            Community newCommunity = new Community(communityName, communityDescription, 1, 0, new User("CreatorUser", R.drawable.placeholder_image));
            newCommunity.joinCommunity(); // Simulate joining the new community
            myCommunityList.add(newCommunity); // Add to my community list

            // Notify the adapter of data changes
            communityAdapter.notifyDataSetChanged();
        }
    }

    private void loadMyCommunities() {
        // Simulate loading communities the user has joined
        // Create a sample User object
        User creator = new User("CreatorUser", R.drawable.placeholder_image); // Replace with a valid image resource ID

        // Add communities that the user has joined
        Community community1 = new Community("Tech Enthusiasts", "A community for tech lovers.", 10, 5, creator);
        community1.joinCommunity(); // Simulating user joining the community
        myCommunityList.add(community1);

        Community community2 = new Community("Music Fans", "Share and discuss your favorite music.", 8, 3, creator);
        community2.joinCommunity(); // Simulating user joining the community
        myCommunityList.add(community2);

        Community community3 = new Community("Fitness Freaks", "A group for fitness enthusiasts.", 12, 7, creator);
        community3.joinCommunity(); // Simulating user joining the community
        myCommunityList.add(community3);
    }
}
