package com.example.pineapple;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.contentContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadPosts();

        postAdapter = new PostAdapter(postList);
        recyclerView.setAdapter(postAdapter);
    }

    private void loadPosts() {
        postList = new ArrayList<>();
        postList.add(new Post("Post Title 1", "This is the content of the first post."));
        postList.add(new Post("Post Title 2", "This is the content of the second post."));
        postList.add(new Post("Post Title 3", "This is the content of the third post."));
    }
}
