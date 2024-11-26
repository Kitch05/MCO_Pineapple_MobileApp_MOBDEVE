package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEditPostActivity extends AppCompatActivity {

    private EditText postTitleInput;
    private EditText postContentInput;
    private Spinner communitySpinner;  // Added as a class field
    private Button savePostButton;
    private ImageView backButton;
    private int position = -1;
    private FirebaseFirestore db;

    // Map to store the community name and its ID
    private Map<String, String> communityMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_post);

        Toast.makeText(this, "AddEditPostActivity opened", Toast.LENGTH_SHORT).show();

        // Initialize UI components
        postTitleInput = findViewById(R.id.postTitleInput);
        postContentInput = findViewById(R.id.postContentInput);
        savePostButton = findViewById(R.id.savePostButton);
        backButton = findViewById(R.id.backButton);
        communitySpinner = findViewById(R.id.communitySpinner);

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        backButton.setOnClickListener(v -> onBackPressed());

        // Fetch and populate communities in the Spinner
        populateCommunitiesSpinner();

        Intent intent = getIntent();
        String defaultCommunityId = intent.getStringExtra("communityId");

        if (intent.hasExtra("title") && intent.hasExtra("content")) {
            position = intent.getIntExtra("position", -1);
            String title = intent.getStringExtra("title");
            String content = intent.getStringExtra("content");
            String communityId = intent.getStringExtra("community");

            Post post = new Post(title, content, null, communityId);
            displayPost(post);

            savePostButton.setText("Update");
        } else {
            savePostButton.setText("Save");
            if (defaultCommunityId != null) {
                setDefaultCommunity(defaultCommunityId);
            }
        }

        savePostButton.setOnClickListener(v -> saveOrUpdatePost());
    }

    private void setDefaultCommunity(String communityId) {
        db.collection("community").document(communityId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String communityName = documentSnapshot.getString("name");
                        if (communityName != null) {
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) communitySpinner.getAdapter();
                            if (adapter != null) {
                                int spinnerPosition = adapter.getPosition(communityName);
                                communitySpinner.setSelection(spinnerPosition);
                                Toast.makeText(this, "Default community set: " + communityName, Toast.LENGTH_SHORT).show(); // Debugging Toast
                            } else {
                                Toast.makeText(this, "Spinner adapter is null", Toast.LENGTH_SHORT).show(); // Debugging Toast
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error setting default community: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void populateCommunitiesSpinner() {
        // Fetch communities from Firestore
        db.collection("community")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> communityList = new ArrayList<>();
                        communityList.add("Select Community");  // Add a default option

                        // Add community names to the list and store their IDs
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String communityName = document.getString("name");  // Assuming 'name' is the field for the community's name
                            String communityId = document.getId();  // Get the community ID (document ID)
                            if (communityName != null && communityId != null) {
                                communityList.add(communityName);
                                communityMap.put(communityName, communityId);  // Map community name to its ID
                            }
                        }

                        // Set up the adapter with the fetched community names
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, communityList);
                        adapter.setDropDownViewResource(R.layout.spinner_item);
                        communitySpinner.setAdapter(adapter);

                        // Set the default community if available
                        Intent intent = getIntent();
                        String defaultCommunityId = intent.getStringExtra("communityId");
                        if (defaultCommunityId != null) {
                            setDefaultCommunity(defaultCommunityId);
                        }
                    } else {
                        Toast.makeText(this, "Error fetching communities: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveOrUpdatePost() {
        String title = postTitleInput.getText().toString().trim();
        String content = postContentInput.getText().toString().trim();
        String communityName = communitySpinner.getSelectedItem().toString();  // Get selected community name
        String currentUserId = FirebaseAuth.getInstance().getUid();  // Get the current user's UID

        if (!title.isEmpty() && !content.isEmpty() && !communityName.equals("Select Community")) {
            if (currentUserId == null) {
                Toast.makeText(this, "User not logged in. Cannot save post.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the community ID from the map
            String communityId = communityMap.get(communityName);

            // Create a Post object with community ID
            Post newPost = new Post(title, content, currentUserId, communityId);

            // Save post to Firestore
            db.collection("posts")
                    .add(newPost)  // Add the post to Firestore
                    .addOnSuccessListener(documentReference -> {
                        // Set the Firestore document ID as the postId
                        String postId = documentReference.getId();
                        newPost.setId(postId);  // Update the Post object with the ID

                        // Update the post in Firestore with the postId
                        db.collection("posts").document(postId)
                                .update("id", postId)
                                .addOnSuccessListener(aVoid -> {
                                    // Increment the post count in the community document
                                    incrementCommunityPostCount(communityId);

                                    Toast.makeText(this, "Post saved successfully!", Toast.LENGTH_SHORT).show();
                                    finish();  // Close the activity
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error updating post ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error saving post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
        }
    }

    private void incrementCommunityPostCount(String communityId) {
        db.collection("community").document(communityId)
                .update("postCount", FieldValue.increment(1))  // Increment the post count by 1
                .addOnSuccessListener(aVoid -> {
                    // Log success
                    Toast.makeText(this, "Community post count updated.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Log failure
                    Toast.makeText(this, "Failed to update community post count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void displayPost(Post post) {
        postTitleInput.setText(post.getTitle());
        postContentInput.setText(post.getContent());

        // Fetch the community name using the community ID
        String communityId = post.getCommunity();
        db.collection("community").document(communityId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String communityName = documentSnapshot.getString("name");
                        if (communityName != null) {
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) communitySpinner.getAdapter();
                            int spinnerPosition = adapter.getPosition(communityName);
                            communitySpinner.setSelection(spinnerPosition);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching community name: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
