package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editUsername;
    private EditText editDescription;
    private ImageView editProfilePic;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);  // Use the new layout

        // Get the Intent data passed from ProfileActivity
        Intent intent = getIntent();
        editUsername = findViewById(R.id.editUsername);
        editDescription = findViewById(R.id.editDescription);
        editProfilePic = findViewById(R.id.editProfilePic);

        String newUsername = intent.getStringExtra("username");
        String newDescription = intent.getStringExtra("userDescription");
        int newProfilePic = intent.getIntExtra("profilePic", R.drawable.goombusken);

        // Set data in the UI
        editProfilePic.setImageResource(newProfilePic);
        editUsername.setText(newUsername);
        editDescription.setText(newDescription);

        // Set up the save button to return the updated data
        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedUsername", editUsername.getText().toString());
            resultIntent.putExtra("updatedDescription", editDescription.getText().toString());
            resultIntent.putExtra("updatedProfilePic", newProfilePic);  // Modify if needed

            // Set result and finish activity
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}
