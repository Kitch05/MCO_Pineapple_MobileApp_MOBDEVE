package com.example.pineapple;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pineapple.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private Button addPostButton;
    private ActivityResultLauncher<Intent> addEditPostLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recyclerView = findViewById(R.id.contentContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        loadPosts();

        postAdapter = new PostAdapter(this, postList, this::launchEditPost, this::launchPostDetail);
        recyclerView.setAdapter(postAdapter);

        addPostButton = findViewById(R.id.addPostButton);
        addPostButton.setOnClickListener(v -> launchAddPost());

        addEditPostLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String title = data.getStringExtra("title");
                        String content = data.getStringExtra("content");
                        int position = data.getIntExtra("position", -1);

                        if (position == -1) {
                            User defaultUser = new User("Default User", R.drawable.placeholder_image);
                            postList.add(0, new Post(title, content, defaultUser));
                            postAdapter.notifyItemInserted(0);
                        } else {
                            Post post = postList.get(position);
                            post.setTitle(title);
                            post.setContent(content);
                            postAdapter.notifyItemChanged(position);
                        }
                        recyclerView.scrollToPosition(0);
                    }
                }
        );
    }

    private void launchAddPost() {
        Intent intent = new Intent(MainActivity.this, AddEditPostActivity.class);
        addEditPostLauncher.launch(intent);
    }

    private void launchEditPost(int position) {
        Post post = postList.get(position);
        Intent intent = new Intent(MainActivity.this, AddEditPostActivity.class);
        intent.putExtra("title", post.getTitle());
        intent.putExtra("content", post.getContent());
        intent.putExtra("position", position);
        addEditPostLauncher.launch(intent);
    }

    private void launchPostDetail(int position) {
        Post post = postList.get(position);
        Intent intent = new Intent(MainActivity.this, PostDetailActivity.class);
        intent.putExtra("title", post.getTitle());
        intent.putExtra("content", post.getContent());
        intent.putExtra("username", post.getUser().getName());
        intent.putExtra("profilePicture", post.getUser().getProfilePicture());
        startActivity(intent);
    }

    private void loadPosts() {
        int placeholderProfilePicture = R.drawable.placeholder_image;
        User user1 = new User("Alice Smith", placeholderProfilePicture);
        User user2 = new User("John Doe", placeholderProfilePicture);

        postList.add(new Post("Post Title 1", "This is the content of the first post.", user1));
        postList.add(new Post("Post Title 2", "This is the content of the second post.", user2));
        postList.add(new Post("Post Title 3", "This is the content of the third post.", user1));
    }
}
