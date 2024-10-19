package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyCommunities extends BaseActivity {

    private RecyclerView recyclerView;
    private CommunityAdapter communityAdapter; // Using the same adapter
    private List<Community> myCommunityList; // List for user's joined communities
    private EditText searchBar;

    // Define a request code for launching the add/edit activity
    private static final int ADD_EDIT_COMMUNITY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActivityLayout(R.layout.activity_my_communities);

        recyclerView = findViewById(R.id.myCommunityContainer); // Update this ID to match your layout
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        myCommunityList = new ArrayList<>();
        loadMyCommunities();

        communityAdapter = new CommunityAdapter(this, myCommunityList, this::launchCommunityDetail);
        recyclerView.setAdapter(communityAdapter);


        searchBar = findViewById(R.id.searchBar);
    }

    private void launchCommunityDetail(int position) {
        Community community = myCommunityList.get(position);
        Intent intent = new Intent(MyCommunities.this, CommunityDetailActivity.class);
        intent.putExtra("communityName", community.getName());
        intent.putExtra("communityDescription", community.getDescription());
        intent.putExtra("memberCount", community.getMemberCount());
        intent.putExtra("postCount", community.getPostCount());
        startActivityForResult(intent, ADD_EDIT_COMMUNITY_REQUEST);
    }

    private void loadMyCommunities() {
        User creator = new User("CreatorUser", R.drawable.placeholder_image);

        myCommunityList.add(new Community("Tech Enthusiasts", "A community for tech lovers.", 0, 0, creator));
        myCommunityList.add(new Community("Music Fans", "Share and discuss your favorite music.", 0, 0, creator));
        myCommunityList.add(new Community("Fitness Freaks", "A group for fitness enthusiasts.", 0, 0, creator));

        for (Community community : myCommunityList) {
            community.setMemberCount(10); // Sample member count
            community.setPostCount(5); // Sample post count
        }
    }
}
