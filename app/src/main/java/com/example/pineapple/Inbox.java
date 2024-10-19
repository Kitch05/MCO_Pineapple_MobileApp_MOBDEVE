package com.example.pineapple;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class Inbox extends BaseActivity {

    private RecyclerView inboxRecyclerView;
    private InboxAdapter inboxAdapter;
    private List<ForumInboxItem> inboxItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityLayout(R.layout.activity_inbox);

        inboxRecyclerView = findViewById(R.id.inbox_recyclerview);
        inboxRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        inboxItemList = getInboxItems();

        inboxAdapter = new InboxAdapter(this, inboxItemList);
        inboxRecyclerView.setAdapter(inboxAdapter);
    }

    private List<ForumInboxItem> getInboxItems() {
        List<ForumInboxItem> items = new ArrayList<>();

        items.add(new ForumInboxItem("Admin", "Welcome to the forum!", "10:00 AM", "We're glad to have you here. If you have any questions, feel free to ask!", false));
        items.add(new ForumInboxItem("User123", "Replied to your thread: Android Development", "9:30 AM", "I think you should consider using Kotlin for your Android development!", true));
        items.add(new ForumInboxItem("ModBot", "Your post was approved", "Yesterday", "Your recent post has been approved and is now visible to all users.", true));
        items.add(new ForumInboxItem("ForumTeam", "New update available", "2 days ago", "We've rolled out some new features. Check them out in the update section!", false));

        return items;
    }
}
