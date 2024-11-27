package com.example.pineapple;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> comments;
    private Map<String, List<Comment>> commentThreads;
    private String postId;
    private int depth;
    String user;

    public CommentAdapter(Context context, List<Comment> comments, Map<String, List<Comment>> commentThreads, String postId, int depth) {
        this.context = context;
        this.comments = comments;
        this.commentThreads = commentThreads;
        this.postId = postId;
        this.depth = depth;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.commentContent.setText(comment.getContent());
        holder.commentTimestamp.setText(formatTimestamp(comment.getTimestamp()));
        holder.upvoteCount.setText(String.valueOf(comment.getUpvotes()));
        holder.downvoteCount.setText(String.valueOf(comment.getDownvotes()));

        FirebaseFirestore.getInstance().collection("users").document(comment.getUserId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = documentSnapshot.getString("username");
                    holder.commentUsername.setText(username != null ? username : "Unknown User");
                })
                .addOnFailureListener(e -> holder.commentUsername.setText("Unknown User"));

        int paddingLeft = 40 * depth;
        holder.itemView.setPadding(paddingLeft, holder.itemView.getPaddingTop(),
                holder.itemView.getPaddingRight(), holder.itemView.getPaddingBottom());

        if (commentThreads.containsKey(comment.getId())) {
            List<Comment> replies = commentThreads.get(comment.getId());
            CommentAdapter replyAdapter = new CommentAdapter(context, replies, commentThreads, postId, depth + 1);
            holder.repliesRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            holder.repliesRecyclerView.setAdapter(replyAdapter);
            holder.repliesRecyclerView.setVisibility(View.VISIBLE);
        } else {
            holder.repliesRecyclerView.setVisibility(View.GONE);
        }

        holder.replyButton.setOnClickListener(v -> {
            if (holder.replyContainer.getVisibility() == View.GONE) {
                holder.replyContainer.setVisibility(View.VISIBLE);
            } else {
                holder.replyContainer.setVisibility(View.GONE);
            }
        });

        holder.submitReplyButton.setOnClickListener(v -> {
            String replyContent = holder.replyInput.getText().toString().trim();
            if (!replyContent.isEmpty()) {
                submitReply(replyContent, comment.getId());
                holder.replyInput.setText("");
                holder.replyContainer.setVisibility(View.GONE);
            }
        });

        holder.upvoteButton.setOnClickListener(v -> {
            Log.d("VoteDebug", "Upvote button clicked for comment: " + comment.getId());
            handleVote(holder, comment, true);
        });

        holder.downvoteButton.setOnClickListener(v -> {
            Log.d("VoteDebug", "Downvote button clicked for comment: " + comment.getId());
            handleVote(holder, comment, false);
        });
    }

    private void handleVote(CommentViewHolder holder, Comment comment, boolean isUpvote) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null || comment.getId() == null) {
            Log.e("VoteError", "User ID or Comment ID is null");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("votes")
                .whereEqualTo("commentId", comment.getId())
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String voteId = querySnapshot.getDocuments().get(0).getId();
                        String existingVoteType = querySnapshot.getDocuments().get(0).getString("voteType");

                        if ((isUpvote && "upvote".equals(existingVoteType)) || (!isUpvote && "downvote".equals(existingVoteType))) {
                            // Remove vote if it's the same as the current action
                            db.collection("votes").document(voteId).delete()
                                    .addOnSuccessListener(aVoid -> adjustVoteCounts(holder, comment, isUpvote, false, true));
                        } else {
                            // Change vote type
                            db.collection("votes").document(voteId)
                                    .update("voteType", isUpvote ? "upvote" : "downvote")
                                    .addOnSuccessListener(aVoid -> adjustVoteCounts(holder, comment, isUpvote, true, true));
                        }
                    } else {
                        // Add a new vote
                        Map<String, Object> voteData = new HashMap<>();
                        voteData.put("commentId", comment.getId());
                        voteData.put("userId", currentUserId);
                        voteData.put("voteType", isUpvote ? "upvote" : "downvote");

                        db.collection("votes").add(voteData)
                                .addOnSuccessListener(aVoid -> adjustVoteCounts(holder, comment, isUpvote, true, false));
                    }

                    db.collection("users").document(comment.getUserId()).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    user = documentSnapshot.getString("username");
                                }
                            });

                    db.collection("notifications")
                            .add(new Notification(
                                    comment.getUserId(),
                                    currentUserId,
                                    isUpvote ? "upvote" : "downvote",
                                    user
                            ));
                })
                .addOnFailureListener(e -> Log.e("VoteError", "Error handling vote", e));
    }

    private void adjustVoteCounts(CommentViewHolder holder, Comment comment, boolean isUpvote, boolean increment, boolean isSwitch) {
        if (increment) {
            if (isUpvote) {
                comment.setUpvotes(comment.getUpvotes() + 1);
                if (isSwitch && comment.getDownvotes() > 0) comment.setDownvotes(comment.getDownvotes() - 1);
            } else {
                comment.setDownvotes(comment.getDownvotes() + 1);
                if (isSwitch && comment.getUpvotes() > 0) comment.setUpvotes(comment.getUpvotes() - 1);
            }
        } else {
            if (isUpvote && comment.getUpvotes() > 0) {
                comment.setUpvotes(comment.getUpvotes() - 1);
            }
            if (!isUpvote && comment.getDownvotes() > 0) {
                comment.setDownvotes(comment.getDownvotes() - 1);
            }
        }

        // Update UI
        holder.upvoteCount.setText(String.valueOf(comment.getUpvotes()));
        holder.downvoteCount.setText(String.valueOf(comment.getDownvotes()));

        // Update Firestore
        FirebaseFirestore.getInstance().collection("comments")
                .document(comment.getId())
                .update("upvotes", comment.getUpvotes(), "downvotes", comment.getDownvotes())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Vote counts updated"))
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to update vote counts", e));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void submitReply(String content, String parentId) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null || postId == null || parentId == null) return;

        Comment reply = new Comment(content, currentUserId, parentId, System.currentTimeMillis());

        FirebaseFirestore.getInstance().collection("posts").document(postId)
                .collection("comments")
                .add(reply)
                .addOnSuccessListener(documentReference -> {
                    String replyId = documentReference.getId();
                    reply.setId(replyId);
                    FirebaseFirestore.getInstance().collection("posts").document(postId)
                            .collection("comments").document(replyId)
                            .update("id", replyId)
                            .addOnSuccessListener(aVoid -> {
                                if (!commentThreads.containsKey(parentId)) {
                                    commentThreads.put(parentId, new ArrayList<>());
                                }
                                commentThreads.get(parentId).add(reply);
                                notifyDataSetChanged();

                                FirebaseFirestore.getInstance().collection("posts")
                                        .whereEqualTo("id", parentId).get().addOnSuccessListener( v -> {

                                        });

                                FirebaseFirestore.getInstance().collection("notifications")
                                        .add(new Notification(
                                                postId,
                                                currentUserId,
                                                "replied",
                                                null
                                        ));
                            })
                            .addOnFailureListener(e -> Log.e("CommentAdapter", "Failed to set reply ID", e));
                })
                .addOnFailureListener(e -> Log.e("CommentAdapter", "Failed to submit reply", e));
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentContent, commentUsername, commentTimestamp, upvoteCount, downvoteCount;
        ImageButton upvoteButton, downvoteButton;
        RecyclerView repliesRecyclerView;
        Button replyButton, submitReplyButton;
        EditText replyInput;
        LinearLayout replyContainer;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            commentContent = itemView.findViewById(R.id.commentContent);
            commentUsername = itemView.findViewById(R.id.commentUsername);
            commentTimestamp = itemView.findViewById(R.id.commentTimestamp);
            upvoteCount = itemView.findViewById(R.id.upvoteCount);
            downvoteCount = itemView.findViewById(R.id.downvoteCount);
            upvoteButton = itemView.findViewById(R.id.upvoteButton);
            downvoteButton = itemView.findViewById(R.id.downvoteButton);
            replyButton = itemView.findViewById(R.id.replyButton);
            submitReplyButton = itemView.findViewById(R.id.submitReplyButton);
            replyInput = itemView.findViewById(R.id.replyInput);
            replyContainer = itemView.findViewById(R.id.replyContainer);
            repliesRecyclerView = itemView.findViewById(R.id.repliesRecyclerView);
        }
    }
}
