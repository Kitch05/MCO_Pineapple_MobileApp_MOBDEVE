package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class AddEditCommunityActivity extends AppCompatActivity {

    private EditText communityNameInput;
    private EditText communityDescriptionInput;
    private Button saveCommunityButton;
    private ImageView backButton;
    private int position = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_community);

        // Initialize views
        communityNameInput = findViewById(R.id.communityNameInput);
        communityDescriptionInput = findViewById(R.id.communityDescriptionInput);
        saveCommunityButton = findViewById(R.id.saveCommunityButton);
        backButton = findViewById(R.id.backButton);

        // Set up back button to return to previous screen
        backButton.setOnClickListener(v -> onBackPressed());

        // Check if editing an existing community or creating a new one
        Intent intent = getIntent();
        if (intent.hasExtra("communityName") && intent.hasExtra("communityDescription")) {
            position = intent.getIntExtra("position", -1);
            communityNameInput.setText(intent.getStringExtra("communityName"));
            communityDescriptionInput.setText(intent.getStringExtra("communityDescription"));
            saveCommunityButton.setText("Update");
        } else {
            saveCommunityButton.setText("Save");
        }

        // Save or update community on button click
        saveCommunityButton.setOnClickListener(v -> saveOrUpdateCommunity());
    }

    private void saveOrUpdateCommunity() {
        String name = communityNameInput.getText().toString().trim();
        String description = communityDescriptionInput.getText().toString().trim();

        if (!name.isEmpty() && !description.isEmpty()) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("communityName", name);
            resultIntent.putExtra("communityDescription", description);
            resultIntent.putExtra("position", position);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            // Optionally, show a message if inputs are invalid
            // Toast.makeText(this, "Please enter valid details", Toast.LENGTH_SHORT).show();
        }
    }
}
