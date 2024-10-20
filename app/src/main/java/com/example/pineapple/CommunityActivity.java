package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CommunityActivity extends BaseActivity {

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

        recyclerView = findViewById(R.id.communityContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        communityList = new ArrayList<>();
        loadCommunities();

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
                communityList.add(newCommunity);
            } else {
                // Update existing community
                Community existingCommunity = communityList.get(position);
                existingCommunity.setMemberCount(data.getIntExtra("memberCount", existingCommunity.getMemberCount()));
                existingCommunity.setPostCount(data.getIntExtra("postCount", existingCommunity.getPostCount()));
            }

            communityAdapter.notifyDataSetChanged();
        }
    }

    public void setCurrentCommunityPosition(int position) {
        this.currentCommunityPosition = position;
    }

    private void loadCommunities() {
        User creator = new User("CreatorUser", R.drawable.placeholder_image);

        communityList.add(new Community("Tech Enthusiasts", "A community for tech lovers.", 0, 0, creator));
        communityList.add(new Community("Music Fans", "Share and discuss your favorite music.", 0, 0, creator));
        communityList.add(new Community("Fitness Freaks", "A group for fitness enthusiasts.", 0, 0, creator));
    }
}
