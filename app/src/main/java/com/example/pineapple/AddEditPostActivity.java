package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AddEditPostActivity extends AppCompatActivity {

    private EditText postTitleInput;
    private EditText postContentInput;
    private Spinner communitySpinner;  // Added as a class field
    private Button savePostButton;
    private ImageView backButton;
    private int position = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_post);

        postTitleInput = findViewById(R.id.postTitleInput);
        postContentInput = findViewById(R.id.postContentInput);
        savePostButton = findViewById(R.id.savePostButton);
        backButton = findViewById(R.id.backButton);
        communitySpinner = findViewById(R.id.communitySpinner);

        backButton.setOnClickListener(v -> onBackPressed());

        List<String> communityList = new ArrayList<>();
        communityList.add("Select Community");
        communityList.add("Tech Enthusiasts");
        communityList.add("Photography Lovers");
        communityList.add("Fitness Buffs");
        communityList.add("Foodies");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, communityList);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        communitySpinner.setAdapter(adapter);


        Intent intent = getIntent();
        if (intent.hasExtra("title") && intent.hasExtra("content")) {
            position = intent.getIntExtra("position", -1);
            postTitleInput.setText(intent.getStringExtra("title"));
            postContentInput.setText(intent.getStringExtra("content"));
            String community = intent.getStringExtra("community");

            if (community != null) {
                int spinnerPosition = adapter.getPosition(community);
                communitySpinner.setSelection(spinnerPosition);
            }

            savePostButton.setText("Update");
        } else {
            savePostButton.setText("Save");
        }

        savePostButton.setOnClickListener(v -> saveOrUpdatePost());
    }

    private void saveOrUpdatePost() {
        String title = postTitleInput.getText().toString().trim();
        String content = postContentInput.getText().toString().trim();
        String community = communitySpinner.getSelectedItem().toString();  // Get selected community

        if (!title.isEmpty() && !content.isEmpty() && !community.equals("Select Community")) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("title", title);
            resultIntent.putExtra("content", content);
            resultIntent.putExtra("community", community);  // Pass selected community back
            resultIntent.putExtra("position", position);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}
