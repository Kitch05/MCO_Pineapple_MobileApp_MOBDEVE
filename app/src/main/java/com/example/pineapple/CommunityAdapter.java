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

        // Update the Join button text based on whether the user has joined
        holder.joinButton.setText(community.isJoined() ? "Joined" : "Join");

        // Set onClickListener for the Join button to join/leave the community
        holder.joinButton.setOnClickListener(v -> {
            if (community.isJoined()) {
                community.leaveCommunity();
                updateCommunityStatusInFirestore(community, false);
            } else {
                community.joinCommunity();
                updateCommunityStatusInFirestore(community, true);
            }
            notifyItemChanged(position);
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
                intent.putExtra("isJoined", community.isJoined());
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
                intent.putExtra("isJoined", community.isJoined());
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

    private void updateCommunityStatusInFirestore(Community community, boolean isJoined) {
        // Determine increment or decrement
        int incrementValue = isJoined ? 1 : -1;

        // Firestore update with atomic increment
        DocumentReference communityRef = db.collection("community").document(community.getId());
        communityRef.update(
                "joined", isJoined,
                "memberCount", FieldValue.increment(incrementValue)
        ).addOnSuccessListener(aVoid -> {
            community.setJoined(isJoined); // Update local state
            community.setMemberCount(community.getMemberCount() + incrementValue); // Update local count

            // If the user is joining, update the user's profile to include the community
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DocumentReference userRef = db.collection("users").document(userId);
            if (isJoined) {
                // Add community to user's joined list
                userRef.update("joinedCommunities", FieldValue.arrayUnion(community.getId()));
            } else {
                // Remove community from user's joined list
                userRef.update("joinedCommunities", FieldValue.arrayRemove(community.getId()));
            }

            notifyDataSetChanged(); // Refresh the UI
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Failed to update membership: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


}


