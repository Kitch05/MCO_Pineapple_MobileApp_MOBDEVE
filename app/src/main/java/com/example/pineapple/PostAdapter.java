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

        // Populate post data
        holder.textViewTitle.setText(post.getTitle() != null ? post.getTitle() : "No Title");
        holder.textViewContent.setText(post.getContent() != null ? post.getContent() : "No Content");
        holder.upvoteCount.setText(String.valueOf(post.getUpvoteCount()));
        holder.downvoteCount.setText(String.valueOf(post.getDownvoteCount()));

        // Fetch and display community name
        String communityId = post.getCommunity();
        if (communityId != null && !communityId.isEmpty()) {
            db.collection("community").document(communityId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String communityName = documentSnapshot.getString("name");
                            if (communityName != null) {
                                holder.textViewCommunity.setText(communityName);
                            } else {
                                holder.textViewCommunity.setText("Unknown Community");
                            }
                        } else {
                            holder.textViewCommunity.setText("Community not found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("PostAdapter", "Error fetching community name", e);
                        holder.textViewCommunity.setText("Error loading community");
                    });
        } else {
            holder.textViewCommunity.setText("Unknown Community");
        }

        // Handle username fetching
        String userId = post.getUserId();
        if (userId != null && !userId.isEmpty()) {
            if (userCache.containsKey(userId)) {
                holder.textViewUsername.setText(userCache.get(userId));
            } else {
                fetchAndCacheUsername(userId, holder.textViewUsername);
            }
        } else {
            holder.textViewUsername.setText("Unknown User");
        }

        // Handle post click for details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("postId", post.getId());
            context.startActivity(intent);
        });

        // Handle edit button visibility
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId != null && currentUserId.equals(post.getUserId())) {
            holder.editIcon.setVisibility(View.VISIBLE);
            holder.editIcon.setOnClickListener(v -> onEditClickListener.onEditClick(position));
        } else {
            holder.editIcon.setVisibility(View.GONE);
        }

        // Handle voting
        holder.upvoteIcon.setOnClickListener(v -> handleVote(holder, post, true));
        holder.downvoteIcon.setOnClickListener(v -> handleVote(holder, post, false));
    }

    private void fetchAndCacheUsername(String userId, TextView usernameView) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        if (username != null) {
                            userCache.put(userId, username);
                            usernameView.setText(username);
                        } else {
                            usernameView.setText("Unknown User");
                        }
                    } else {
                        usernameView.setText("Unknown User");
                    }
                })
                .addOnFailureListener(e -> {
                    usernameView.setText("Error fetching username");
                    Log.e("PostAdapter", "Error fetching username", e);
                });
    }

    private void handleVote(PostViewHolder holder, Post post, boolean isUpvote) {
        if (isVoting) return; // Prevent multiple clicks
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
                        String existingVoteId = querySnapshot.getDocuments().get(0).getId();
                        String existingVoteType = querySnapshot.getDocuments().get(0).getString("voteType");

                        if ((isUpvote && "upvote".equals(existingVoteType)) ||
                                (!isUpvote && "downvote".equals(existingVoteType))) {
                            // Remove the vote
                            db.collection("votes").document(existingVoteId).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        adjustVoteCounts(holder, post, isUpvote, false);
                                    })
                                    .addOnFailureListener(e -> isVoting = false);
                        } else {
                            // Switch vote
                            db.collection("votes").document(existingVoteId).update("voteType", isUpvote ? "upvote" : "downvote")
                                    .addOnSuccessListener(aVoid -> {
                                        adjustVoteCounts(holder, post, isUpvote, true);
                                    })
                                    .addOnFailureListener(e -> isVoting = false);
                        }
                    } else {
                        // Add a new vote
                        Map<String, Object> voteData = new HashMap<>();
                        voteData.put("postId", post.getId());
                        voteData.put("userId", currentUserId);
                        voteData.put("voteType", isUpvote ? "upvote" : "downvote");

                        db.collection("votes").add(voteData)
                                .addOnSuccessListener(aVoid -> {
                                    adjustVoteCounts(holder, post, isUpvote, true);
                                })
                                .addOnFailureListener(e -> isVoting = false);
                    }
                })
                .addOnFailureListener(e -> isVoting = false); // Reset voting state if query fails
    }

    private void adjustVoteCounts(PostViewHolder holder, Post post, boolean isUpvote, boolean increment) {
        if (increment) {
            if (isUpvote) {
                post.setUpvoteCount(post.getUpvoteCount() + 1);
                if (post.getDownvoteCount() > 0) post.setDownvoteCount(post.getDownvoteCount() - 1);
            } else {
                post.setDownvoteCount(post.getDownvoteCount() + 1);
                if (post.getUpvoteCount() > 0) post.setUpvoteCount(post.getUpvoteCount() - 1);
            }
        } else {
            if (isUpvote && post.getUpvoteCount() > 0) post.setUpvoteCount(post.getUpvoteCount() - 1);
            if (!isUpvote && post.getDownvoteCount() > 0) post.setDownvoteCount(post.getDownvoteCount() - 1);
        }

        // Update Firestore
        db.collection("posts").document(post.getId())
                .update("upvoteCount", post.getUpvoteCount(), "downvoteCount", post.getDownvoteCount())
                .addOnSuccessListener(aVoid -> {
                    holder.upvoteCount.setText(String.valueOf(post.getUpvoteCount()));
                    holder.downvoteCount.setText(String.valueOf(post.getDownvoteCount()));
                    isVoting = false;
                })
                .addOnFailureListener(e -> isVoting = false);
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
