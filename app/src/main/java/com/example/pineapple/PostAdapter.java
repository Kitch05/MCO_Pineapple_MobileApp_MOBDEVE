package com.example.pineapple;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postData;
    private Context context;
    private OnEditClickListener onEditClickListener;
    private OnPostClickListener onPostClickListener;

    public PostAdapter(Context context, List<Post> postData,
                       OnEditClickListener onEditClickListener,
                       OnPostClickListener onPostClickListener) {
        this.context = context;
        this.postData = postData;
        this.onEditClickListener = onEditClickListener;
        this.onPostClickListener = onPostClickListener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        final Post post = postData.get(position);

        // Set title, content, username, and initial counts
        holder.textViewTitle.setText(post.getTitle());
        holder.textViewContent.setText(post.getContent());
        holder.textViewUsername.setText(post.getUser().getName());
        holder.upvoteCount.setText(String.valueOf(post.getUpvoteCount()));
        holder.downvoteCount.setText(String.valueOf(post.getDownvoteCount()));

        // Set up click listeners for upvote and downvote
        holder.upvoteIcon.setOnClickListener(v -> {
            post.setUpvoteCount(post.getUpvoteCount() + 1); // Increase upvote count
            holder.upvoteCount.setText(String.valueOf(post.getUpvoteCount())); // Update UI
        });

        holder.downvoteIcon.setOnClickListener(v -> {
            post.setDownvoteCount(post.getDownvoteCount() + 1); // Increase downvote count
            holder.downvoteCount.setText(String.valueOf(post.getDownvoteCount())); // Update UI
        });

        // Set up click listeners for other interactions
        holder.itemView.setOnClickListener(v -> onPostClickListener.onPostClick(position));
        holder.editIcon.setOnClickListener(v -> onEditClickListener.onEditClick(position));
    }

    @Override
    public int getItemCount() {
        return postData.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewContent;
        TextView textViewUsername;
        TextView upvoteCount;
        TextView downvoteCount;
        ImageView editIcon;
        ImageView upvoteIcon;
        ImageView downvoteIcon;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.postTitle);
            textViewContent = itemView.findViewById(R.id.postContent);
            textViewUsername = itemView.findViewById(R.id.userName);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            downvoteCount = itemView.findViewById(R.id.downvoteCount);
            editIcon = itemView.findViewById(R.id.editPostIcon);
            upvoteIcon = itemView.findViewById(R.id.upvoteIcon);
            downvoteIcon = itemView.findViewById(R.id.downvoteIcon);
        }
    }

    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    public interface OnPostClickListener {
        void onPostClick(int position);
    }
}


