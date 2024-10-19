package com.example.pineapple;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class InboxDetailActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView messageContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_detail);

        // Retrieve the message content and sender name from the intent
        String messageContent = getIntent().getStringExtra("message_content");
        String senderName = getIntent().getStringExtra("sender_name");

        // Find the TextViews and set the message content and sender name
        TextView messageTextView = findViewById(R.id.messageContent);
        messageTextView.setText(messageContent);

        TextView senderTextView = findViewById(R.id.senderName);
        senderTextView.setText(senderName);

        // Optional: Setup the back button
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // Close the activity on back button click

        // Back button functionality
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close this activity and return to inbox
                finish();
            }
        });
    }
}
