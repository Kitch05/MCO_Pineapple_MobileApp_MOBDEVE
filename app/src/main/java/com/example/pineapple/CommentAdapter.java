package com.example.pineapple;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> comments;
    private Map<String, List<Comment>> commentThreads; // Map for nested replies
    private String postId; // ID of the current post
    private int depth; // Depth of the current comment for indentation

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

        // Fetch and display the username
        FirebaseFirestore.getInstance().collection("users").document(comment.getUserId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = documentSnapshot.getString("username");
                    holder.commentUsername.setText(username != null ? username : "Unknown User");
                })
                .addOnFailureListener(e -> holder.commentUsername.setText("Unknown User"));

        // Adjust padding for nested replies
        int paddingLeft = 40 * depth; // Indent each level by 40dp
        holder.itemView.setPadding(paddingLeft, holder.itemView.getPaddingTop(),
                holder.itemView.getPaddingRight(), holder.itemView.getPaddingBottom());

        // Handle nested replies
        if (commentThreads.containsKey(comment.getId())) {
            List<Comment> replies = commentThreads.get(comment.getId());
            CommentAdapter replyAdapter = new CommentAdapter(context, replies, commentThreads, postId, depth + 1);
            holder.repliesRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            holder.repliesRecyclerView.setAdapter(replyAdapter);
            holder.repliesRecyclerView.setVisibility(View.VISIBLE);
        } else {
            holder.repliesRecyclerView.setVisibility(View.GONE);
        }

        // Show reply box on button click
        holder.replyButton.setOnClickListener(v -> {
            if (holder.replyInput.getVisibility() == View.GONE) {
                holder.replyInput.setVisibility(View.VISIBLE);
                holder.submitReplyButton.setVisibility(View.VISIBLE);
            } else {
                holder.replyInput.setVisibility(View.GONE);
                holder.submitReplyButton.setVisibility(View.GONE);
            }

            holder.submitReplyButton.setOnClickListener(v2 -> {
                String replyContent = holder.replyInput.getText().toString().trim();
                if (!replyContent.isEmpty()) {
                    submitReply(replyContent, comment.getId()); // Pass parent comment ID
                    holder.replyInput.setText("");
                    holder.replyInput.setVisibility(View.GONE);
                    holder.submitReplyButton.setVisibility(View.GONE);
                }
            });
        });

    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    // Helper: Format timestamp
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // Helper: Submit a reply to Firestore
    private void submitReply(String content, String parentId) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null || postId == null) return;

        // Create reply with parentId
        Comment reply = new Comment(content, currentUserId, parentId, System.currentTimeMillis());

        FirebaseFirestore.getInstance().collection("posts").document(postId)
                .collection("comments")
                .add(reply)
                .addOnSuccessListener(documentReference -> {
                    // Update the Firestore document with its generated ID
                    String replyId = documentReference.getId();
                    FirebaseFirestore.getInstance().collection("posts").document(postId)
                            .collection("comments").document(replyId)
                            .update("id", replyId)
                            .addOnSuccessListener(aVoid -> {
                                // Optional: Notify the adapter or reload comments
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }



    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentContent, commentUsername, commentTimestamp;
        RecyclerView repliesRecyclerView;
        Button replyButton, submitReplyButton;
        EditText replyInput;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentContent = itemView.findViewById(R.id.commentContent);
            commentUsername = itemView.findViewById(R.id.commentUsername);
            commentTimestamp = itemView.findViewById(R.id.commentTimestamp);
            repliesRecyclerView = itemView.findViewById(R.id.repliesRecyclerView);
            replyButton = itemView.findViewById(R.id.replyButton);
            submitReplyButton = itemView.findViewById(R.id.submitReplyButton);
            replyInput = itemView.findViewById(R.id.replyInput);
        }
    }
}
