package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.CollectionReference;

import java.util.HashMap;

public class AddEditCommunityActivity extends AppCompatActivity {

    private EditText communityNameInput;
    private EditText communityDescriptionInput;
    private Button saveCommunityButton;
    private ImageView backButton;
    private int position = -1;

    private FirebaseFirestore db;
    private CollectionReference communitiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_community);

        communityNameInput = findViewById(R.id.communityNameInput);
        communityDescriptionInput = findViewById(R.id.communityDescriptionInput);
        saveCommunityButton = findViewById(R.id.saveCommunityButton);
        backButton = findViewById(R.id.backButton);

        db = FirebaseFirestore.getInstance();  // Initialize Firestore instance
        communitiesRef = db.collection("community");  // Firestore collection for communities

        backButton.setOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        if (intent.hasExtra("communityName") && intent.hasExtra("communityDescription")) {
            position = intent.getIntExtra("position", -1);
            communityNameInput.setText(intent.getStringExtra("communityName"));
            communityDescriptionInput.setText(intent.getStringExtra("communityDescription"));
            saveCommunityButton.setText("Update");
        } else {
            saveCommunityButton.setText("Save");
        }

        saveCommunityButton.setOnClickListener(v -> saveOrUpdateCommunity());
    }

    private void saveOrUpdateCommunity() {
        String name = communityNameInput.getText().toString().trim();
        String description = communityDescriptionInput.getText().toString().trim();

        if (!name.isEmpty() && !description.isEmpty()) {
            if (position == -1) {
                // New community: Add to Firestore
                Community newCommunity = new Community(name, description);
                communitiesRef.add(newCommunity)
                        .addOnSuccessListener(documentReference -> {
                            // Add user to members collection (optional)
                            String userId = getCurrentUserId();
                            DocumentReference membersRef = documentReference.collection("members").document(userId);
                            membersRef.set(new HashMap<>()); // Empty map or user-specific data

                            Toast.makeText(this, "Community created successfully!", Toast.LENGTH_SHORT).show();

                            // Send data back to the calling activity (CommunityActivity)
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("communityId", documentReference.getId()); // Return the ID
                            resultIntent.putExtra("communityName", name);
                            resultIntent.putExtra("communityDescription", description);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error creating community: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Existing community: Update Firestore
                String communityId = getIntent().getStringExtra("communityId"); // Pass communityId when editing
                if (communityId != null) {
                    DocumentReference communityRef = communitiesRef.document(communityId);
                    communityRef.update("name", name, "description", description)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Community updated successfully!", Toast.LENGTH_SHORT).show();

                                // Send data back to the calling activity (CommunityActivity)
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("communityId", communityId); // Return the ID
                                resultIntent.putExtra("communityName", name);
                                resultIntent.putExtra("communityDescription", description);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error updating community: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(this, "Community ID is missing.", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Please enter valid details", Toast.LENGTH_SHORT).show();
        }
    }



    private String getCurrentUserId() {
        // Replace with actual method to get the current logged-in user ID
        return "currentUserId"; // Sample user ID
    }
}
