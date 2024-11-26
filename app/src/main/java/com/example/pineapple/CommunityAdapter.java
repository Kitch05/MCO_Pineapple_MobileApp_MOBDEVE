package com.example.pineapple;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder> {

    private List<Community> communityList;
    private Context context;
    private OnCommunityClickListener onCommunityClickListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore instance

    public CommunityAdapter(Context context, List<Community> communityList,
                            OnCommunityClickListener onCommunityClickListener) {
        this.context = context;
        this.communityList = communityList;
        this.onCommunityClickListener = onCommunityClickListener;
    }

    @NonNull
    @Override
    public CommunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.community_item, parent, false);
        return new CommunityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommunityViewHolder holder, int position) {
        Community community = communityList.get(position);

        holder.textViewName.setText(community.getName());
        holder.textViewDescription.setText(community.getDescription());
        holder.membersCountTextView.setText(community.getMemberCount() + " Members");
        holder.postCountTextView.setText(community.getPostCount() + " Posts");

        // Check if the user has joined the community
        checkJoinStatus(holder, community);

        // Set onClickListener for the Join button to join/leave the community
        holder.joinButton.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (userId != null) {
                DocumentReference communityRef = db.collection("community").document(community.getId());
                communityRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> members = (List<String>) documentSnapshot.get("members");
                        if (members == null) {
                            members = new ArrayList<>(); // Initialize members list if it's null
                        }
                        if (members.contains(userId)) {
                            // User is leaving the community
                            Toast.makeText(context, "Leaving community...", Toast.LENGTH_SHORT).show();
                            members.remove(userId);
                            communityRef.update("members", members, "memberCount", FieldValue.increment(-1))
                                    .addOnSuccessListener(aVoid -> {
                                        holder.joinButton.setText("Join");
                                        community.setMemberCount(community.getMemberCount() - 1);
                                        Toast.makeText(context, "You have left the community.", Toast.LENGTH_SHORT).show();
                                        updateUserJoinedCommunities(false, community.getId());
                                        notifyItemChanged(position); // Notify adapter of changes
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Error updating membership: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // User is joining the community
                            Toast.makeText(context, "Joining community...", Toast.LENGTH_SHORT).show();
                            members.add(userId);
                            communityRef.update("members", members, "memberCount", FieldValue.increment(1))
                                    .addOnSuccessListener(aVoid -> {
                                        holder.joinButton.setText("Joined");
                                        community.setMemberCount(community.getMemberCount() + 1);
                                        Toast.makeText(context, "You have joined the community.", Toast.LENGTH_SHORT).show();
                                        updateUserJoinedCommunities(true, community.getId());
                                        notifyItemChanged(position); // Notify adapter of changes
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Error updating membership: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(context, "Community document does not exist", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Error fetching community document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(context, "User ID is null", Toast.LENGTH_SHORT).show();
            }
        });

        // Set onClickListener for the item view (community card) to view community details
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof CommunityActivity) {
                // Handle click for CommunityActivity
                ((CommunityActivity) context).setCurrentCommunityPosition(position);
                Intent intent = new Intent(context, CommunityDetailActivity.class);
                intent.putExtra("communityId", community.getId());
                intent.putExtra("communityName", community.getName());
                intent.putExtra("communityDescription", community.getDescription());
                intent.putExtra("memberCount", community.getMemberCount());
                intent.putExtra("postCount", community.getPostCount());
                context.startActivity(intent);
            } else if (context instanceof MyCommunities) {
                // Handle click for MyCommunities
                ((MyCommunities) context).setCurrentCommunityPosition(position);
                Intent intent = new Intent(context, CommunityDetailActivity.class);
                intent.putExtra("communityId", community.getId());
                intent.putExtra("communityName", community.getName());
                intent.putExtra("communityDescription", community.getDescription());
                intent.putExtra("memberCount", community.getMemberCount());
                intent.putExtra("postCount", community.getPostCount());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return communityList.size();
    }

    public void updateList(List<Community> newList) {
        communityList.clear();
        communityList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class CommunityViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewDescription;
        TextView membersCountTextView;
        TextView postCountTextView;
        Button joinButton;

        public CommunityViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.communityName);
            textViewDescription = itemView.findViewById(R.id.communityDescription);
            membersCountTextView = itemView.findViewById(R.id.membersCount);
            joinButton = itemView.findViewById(R.id.joinCommunityButton);
            postCountTextView = itemView.findViewById(R.id.postsCount);
        }
    }

    public interface OnCommunityClickListener {
        void onCommunityClick(int position);
    }

    private void checkJoinStatus(CommunityViewHolder holder, Community community) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId != null) {
            DocumentReference communityRef = db.collection("community").document(community.getId());
            communityRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> members = (List<String>) documentSnapshot.get("members");
                    if (members == null) {
                        members = new ArrayList<>(); // Initialize members list if it's null
                    }
                    if (members.contains(userId)) {
                        holder.joinButton.setText("Joined");
                    } else {
                        holder.joinButton.setText("Join");
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Error checking join status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateUserJoinedCommunities(boolean isJoined, String communityId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId != null) {
            DocumentReference userRef = db.collection("users").document(userId);

            if (isJoined) {
                // Add community to joinedCommunities if the user joins
                userRef.update("joinedCommunities", FieldValue.arrayUnion(communityId))
                        .addOnSuccessListener(aVoid -> Toast.makeText(context, "Community added to user.", Toast.LENGTH_SHORT).show());
            } else {
                // Remove community from joinedCommunities if the user leaves
                userRef.update("joinedCommunities", FieldValue.arrayRemove(communityId))
                        .addOnSuccessListener(aVoid -> Toast.makeText(context, "Community removed from user.", Toast.LENGTH_SHORT).show());
            }
        }
    }
}


