package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class AddEditPostActivity extends AppCompatActivity {

    private EditText postTitleInput;
    private EditText postContentInput;
    private Button savePostButton;
    private ImageView backButton;
    private int position = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_post);

        // Initialize views
        postTitleInput = findViewById(R.id.postTitleInput);
        postContentInput = findViewById(R.id.postContentInput);
        savePostButton = findViewById(R.id.savePostButton);
        backButton = findViewById(R.id.backButton);

        // Set up back button to return to previous screen
        backButton.setOnClickListener(v -> onBackPressed());

        // Check if editing an existing post or creating a new one
        Intent intent = getIntent();
        if (intent.hasExtra("title") && intent.hasExtra("content")) {
            position = intent.getIntExtra("position", -1);
            postTitleInput.setText(intent.getStringExtra("title"));
            postContentInput.setText(intent.getStringExtra("content"));
            savePostButton.setText("Update");
        } else {
            savePostButton.setText("Save");
        }

        // Save or update post on button click
        savePostButton.setOnClickListener(v -> saveOrUpdatePost());
    }

    private void saveOrUpdatePost() {
        String title = postTitleInput.getText().toString().trim();
        String content = postContentInput.getText().toString().trim();

        if (!title.isEmpty() && !content.isEmpty()) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("title", title);
            resultIntent.putExtra("content", content);
            resultIntent.putExtra("position", position);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}
