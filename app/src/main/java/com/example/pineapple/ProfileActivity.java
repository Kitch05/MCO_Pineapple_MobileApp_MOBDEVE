package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

public class ProfileActivity extends BaseActivity {
    private TextView username;
    private ImageView profilePic;
    private TextView description;
    private Button editProfile;

    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        username = findViewById(R.id.userName);
        profilePic = findViewById(R.id.profilePic);
        description = findViewById(R.id.userDescription);
        editProfile = findViewById(R.id.editProfileButton);


        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {

                            String updatedUsername = data.getStringExtra("updatedUsername");
                            String updatedDescription = data.getStringExtra("updatedDescription");
                            int updatedProfilePic = data.getIntExtra("updatedProfilePic", R.drawable.goombusken);


                            username.setText(updatedUsername);
                            description.setText(updatedDescription);
                            profilePic.setImageResource(updatedProfilePic);
                        }
                    }
                });


        editProfile.setOnClickListener(this::editProfile);
    }

    public void editProfile(View view) {

        Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
        intent.putExtra("username", username.getText().toString());
        intent.putExtra("profilePic", R.drawable.goombusken);  // Replace with actual drawable or resource
        intent.putExtra("userDescription", description.getText().toString());


        editProfileLauncher.launch(intent);
    }
}
