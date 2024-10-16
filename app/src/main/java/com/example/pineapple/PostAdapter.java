package com.example.pineapple;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postData;  // Single List of Post objects
    private Context context;

    // Constructor with Context and List<Post>
    public PostAdapter(Context context, List<Post> postData) {
        this.context = context;
        this.postData = postData;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        final Post postDataItem = postData.get(position);  // Access individual Post
        holder.textViewTitle.setText(postDataItem.getTitle());
        holder.textViewContent.setText(postDataItem.getContent());

        // OnClickListener to start PostDetailActivity without expecting a result
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("title", postDataItem.getTitle());
            intent.putExtra("content", postDataItem.getContent());
            context.startActivity(intent);  // Start activity without expecting a result
        });
    }

    @Override
    public int getItemCount() {
        return postData.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewContent;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.postTitle);
            textViewContent = itemView.findViewById(R.id.postContent);
        }
    }
}
