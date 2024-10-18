package com.example.pineapple;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder> {

    private List<Community> communityList;
    private Context context;
    private OnCommunityClickListener onCommunityClickListener;

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

        // Set up join button functionality
        holder.joinButton.setText(community.isJoined() ? "Joined" : "Join");
        holder.joinButton.setEnabled(true); // Always enable the button

        holder.joinButton.setOnClickListener(v -> {
            if (community.isJoined()) {
                community.leaveCommunity(); // Unjoin if already joined
                holder.joinButton.setText("Join");
            } else {
                community.joinCommunity(); // Join if not joined
                holder.joinButton.setText("Joined");
            }
        });

        // Set up click listener for the item view
        holder.itemView.setOnClickListener(v -> {
            ((CommunityActivity) context).setCurrentCommunityPosition(position); // Update the current community position
            onCommunityClickListener.onCommunityClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return communityList.size();
    }

    public static class CommunityViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewDescription;
        Button joinButton; // Add join button

        public CommunityViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.communityName);
            textViewDescription = itemView.findViewById(R.id.communityDescription);
            joinButton = itemView.findViewById(R.id.joinCommunityButton); // Initialize join button
        }
    }

    public interface OnCommunityClickListener {
        void onCommunityClick(int position);
    }
}
