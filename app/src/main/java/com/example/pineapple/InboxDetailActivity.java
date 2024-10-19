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

        String messageContent = getIntent().getStringExtra("message_content");
        String senderName = getIntent().getStringExtra("sender_name");

        TextView messageTextView = findViewById(R.id.messageContent);
        messageTextView.setText(messageContent);

        TextView senderTextView = findViewById(R.id.senderName);
        senderTextView.setText(senderName);


        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
