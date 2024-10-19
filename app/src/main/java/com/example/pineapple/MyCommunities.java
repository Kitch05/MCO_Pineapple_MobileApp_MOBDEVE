package com.example.pineapple;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class MyCommunities extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_communities); // Verify this points to the correct layout

        drawerLayout = findViewById(R.id.drawer_layout);

        // Find the menuButton within the included header layout
        View header = findViewById(R.id.header);
        menuButton = header.findViewById(R.id.menuButton);

        if (menuButton == null) {
            Log.e("MyCommunities", "menuButton is null");
        } else {
            Log.d("MyCommunities", "menuButton is not null");
        }

        // Set up click listener for the menu button
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MyCommunities", "Menu button clicked");
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START);
                } else {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });
    }


// Temporary method to generate a list of communities
    private List<Community> getTemporaryCommunities() {
        List<Community> communities = new ArrayList<>();

        // Temporary User objects for creator
        User alice = new User("Alice", R.drawable.placeholder_image);
        User john = new User("John", R.drawable.placeholder_image);
        User sarah = new User("Sarah", R.drawable.placeholder_image);

        // Adding temporary community objects
        communities.add(new Community("Tech Enthusiasts", "A community for tech lovers", 1200, 150, alice));
        communities.add(new Community("Book Club", "For bookworms and literature enthusiasts", 800, 50, john));
        communities.add(new Community("Fitness Freaks", "Community for fitness and health enthusiasts", 500, 30, sarah));
        communities.add(new Community("Music Lovers", "A place for music fans and artists", 600, 120, john));
        communities.add(new Community("Gamers United", "All things gaming and eSports", 1400, 200, alice));

        return communities;
    }
}
