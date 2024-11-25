package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.CollectionReference;

import java.util.HashMap;

public class AddEditCommunityActivity extends AppCompatActivity {

    private EditText communityNameInput;
    private EditText communityDescriptionInput;
    private Button saveCommunityButton;
    private ImageView backButton;

    private FirebaseFirestore db;
    private CollectionReference communitiesRef;

    private String communityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_community);

        // Initialize UI components
        communityNameInput = findViewById(R.id.communityNameInput);
        communityDescriptionInput = findViewById(R.id.communityDescriptionInput);
        saveCommunityButton = findViewById(R.id.saveCommunityButton);
        backButton = findViewById(R.id.backButton);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        communitiesRef = db.collection("community");

        // Back button functionality
        backButton.setOnClickListener(v -> onBackPressed());

        // Handle intent data
        Intent intent = getIntent();
        communityId = intent.getStringExtra("communityId"); // Firestore document ID
        String communityName = intent.getStringExtra("communityName");
        String communityDescription = intent.getStringExtra("communityDescription");

        if (communityId != null) {
            // Pre-fill fields for editing
            communityNameInput.setText(communityName);
            communityDescriptionInput.setText(communityDescription);
            saveCommunityButton.setText("Update");

        } else {
            // Set for new community creation
            saveCommunityButton.setText("Save");
        }

        // Save or update community
        saveCommunityButton.setOnClickListener(v -> saveOrUpdateCommunity());
    }

    private void saveOrUpdateCommunity() {
        String name = communityNameInput.getText().toString().trim();
        String description = communityDescriptionInput.getText().toString().trim();

        if (!name.isEmpty() && !description.isEmpty()) {
            if (communityId != null) {
                // Update existing community
                DocumentReference communityRef = communitiesRef.document(communityId);
                communityRef.update("name", name, "description", description)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Community updated successfully!", Toast.LENGTH_SHORT).show();

                            // Send updated data back to the calling activity
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("communityId", communityId);
                            resultIntent.putExtra("communityName", name);
                            resultIntent.putExtra("communityDescription", description);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error updating community: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Create new community
                Community newCommunity = new Community(name, description);
                communitiesRef.add(newCommunity)
                        .addOnSuccessListener(documentReference -> {
                            String userId = getCurrentUserId();
                            if (userId != null) {
                                // Optionally add the current user as a member
                                DocumentReference membersRef = documentReference.collection("members").document(userId);
                                membersRef.set(new HashMap<>()); // Empty map or user-specific data
                            }

                            Toast.makeText(this, "Community created successfully!", Toast.LENGTH_SHORT).show();

                            // Send new data back to the calling activity
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("communityId", documentReference.getId());
                            resultIntent.putExtra("communityName", name);
                            resultIntent.putExtra("communityDescription", description);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error creating community: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        } else {
            Toast.makeText(this, "Please enter valid details", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return null;
        }
    }
}
