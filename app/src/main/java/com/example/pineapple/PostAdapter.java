package com.example.pineapple;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postData;
    private Context context;
    private OnEditClickListener onEditClickListener;
    private OnPostClickListener onPostClickListener;
    private boolean isVoting = false; // Flag to prevent multiple vote operations

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Map<String, String> userCache = new HashMap<>(); // Cache to store userId -> username mapping

    public PostAdapter(Context context, List<Post> postData, OnEditClickListener onEditClickListener, OnPostClickListener onPostClickListener) {
        this.context = context;
        this.postData = postData;
        this.onEditClickListener = onEditClickListener;
        this.onPostClickListener = onPostClickListener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postData.get(position);

        // Set up click listener for the entire post item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);

            // Ensure post.getId() returns a valid post ID
            String postId = post.getId();
            Log.d("PostAdapter", "Passing postId: " + postId); // Add log to debug

            if (postId != null) {
                intent.putExtra("postId", postId);
                context.startActivity(intent);
            } else {
                Log.e("PostAdapter", "postId is null. Cannot navigate to PostDetailActivity.");
                Toast.makeText(context, "Error: Unable to load post details.", Toast.LENGTH_SHORT).show();
            }
        });

        // Get the current user's ID
        String currentUserId = FirebaseAuth.getInstance().getUid();

        // Check if the user is the owner of the post
        if (currentUserId != null && currentUserId.equals(post.getUserId())) {
            holder.editIcon.setVisibility(View.VISIBLE);
            holder.editIcon.setOnClickListener(v -> onEditClickListener.onEditClick(holder.getAdapterPosition()));
        } else {
            holder.editIcon.setVisibility(View.GONE);
        }

        // Populate other fields
        holder.textViewTitle.setText(post.getTitle() != null ? post.getTitle() : "No Title");
        holder.textViewContent.setText(post.getContent() != null ? post.getContent() : "No Content");
        holder.textViewCommunity.setText(post.getCommunity() != null ? post.getCommunity() : "Unknown Community");
        holder.upvoteCount.setText(String.valueOf(post.getUpvoteCount()));
        holder.downvoteCount.setText(String.valueOf(post.getDownvoteCount()));

        // Handle votes
        holder.upvoteIcon.setOnClickListener(v -> handleVote(holder, post, true));
        holder.downvoteIcon.setOnClickListener(v -> handleVote(holder, post, false));
    }

    private void handleVote(PostViewHolder holder, Post post, boolean isUpvote) {
        if (isVoting) return; // Prevent further voting if an operation is in progress
        isVoting = true;

        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            isVoting = false;
            return;
        }

        db.collection("votes")
                .whereEqualTo("postId", post.getId())
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // User has already voted
                        String existingVoteId = querySnapshot.getDocuments().get(0).getId();
                        String existingVoteType = querySnapshot.getDocuments().get(0).getString("voteType");

                        if ((isUpvote && "upvote".equals(existingVoteType)) ||
                                (!isUpvote && "downvote".equals(existingVoteType))) {
                            // Remove the vote
                            db.collection("votes").document(existingVoteId).delete().addOnSuccessListener(aVoid -> {
                                if (isUpvote && post.getUpvoteCount() > 0) {
                                    post.setUpvoteCount(post.getUpvoteCount() - 1);
                                    holder.upvoteCount.setText(String.valueOf(post.getUpvoteCount()));
                                } else if (!isUpvote && post.getDownvoteCount() > 0) {
                                    post.setDownvoteCount(post.getDownvoteCount() - 1);
                                    holder.downvoteCount.setText(String.valueOf(post.getDownvoteCount()));
                                }
                                updatePostVotes(post);
                                isVoting = false; // Reset voting state
                            }).addOnFailureListener(e -> isVoting = false);
                        } else {
                            // Switch vote
                            db.collection("votes").document(existingVoteId).update("voteType", isUpvote ? "upvote" : "downvote")
                                    .addOnSuccessListener(aVoid -> {
                                        if (isUpvote) {
                                            post.setUpvoteCount(post.getUpvoteCount() + 1);
                                            if (post.getDownvoteCount() > 0)
                                                post.setDownvoteCount(post.getDownvoteCount() - 1);
                                        } else {
                                            post.setDownvoteCount(post.getDownvoteCount() + 1);
                                            if (post.getUpvoteCount() > 0)
                                                post.setUpvoteCount(post.getUpvoteCount() - 1);
                                        }
                                        holder.upvoteCount.setText(String.valueOf(post.getUpvoteCount()));
                                        holder.downvoteCount.setText(String.valueOf(post.getDownvoteCount()));
                                        updatePostVotes(post);
                                        isVoting = false; // Reset voting state
                                    }).addOnFailureListener(e -> isVoting = false);
                        }
                    } else {
                        // Add a new vote
                        Map<String, Object> voteData = new HashMap<>();
                        voteData.put("postId", post.getId());
                        voteData.put("userId", currentUserId);
                        voteData.put("voteType", isUpvote ? "upvote" : "downvote");

                        db.collection("votes").add(voteData).addOnSuccessListener(aVoid -> {
                            if (isUpvote) {
                                post.setUpvoteCount(post.getUpvoteCount() + 1);
                                holder.upvoteCount.setText(String.valueOf(post.getUpvoteCount()));
                            } else {
                                post.setDownvoteCount(post.getDownvoteCount() + 1);
                                holder.downvoteCount.setText(String.valueOf(post.getDownvoteCount()));
                            }
                            updatePostVotes(post);
                            isVoting = false; // Reset voting state
                        }).addOnFailureListener(e -> isVoting = false);
                    }
                })
                .addOnFailureListener(e -> isVoting = false); // Reset voting state if query fails
    }

    private void updatePostVotes(Post post) {
        db.collection("posts").document(post.getId())
                .update("upvoteCount", post.getUpvoteCount(), "downvoteCount", post.getDownvoteCount())
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    @Override
    public int getItemCount() {
        return postData.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewContent, textViewUsername, textViewCommunity, upvoteCount, downvoteCount;
        ImageView editIcon, upvoteIcon, downvoteIcon;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.postTitle);
            textViewContent = itemView.findViewById(R.id.postContent);
            textViewUsername = itemView.findViewById(R.id.userName);
            textViewCommunity = itemView.findViewById(R.id.postCommunity);
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
