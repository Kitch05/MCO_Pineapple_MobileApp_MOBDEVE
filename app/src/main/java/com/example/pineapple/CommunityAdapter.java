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

        holder.joinButton.setOnClickListener(v -> {
            if (community.isJoined()) {
                community.leaveCommunity();
                holder.joinButton.setText("Join");
            } else {
                community.joinCommunity();
                holder.joinButton.setText("Joined");
            }
        });

        // Safely check if the context is an instance of CommunityActivity
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof CommunityActivity) {
                ((CommunityActivity) context).setCurrentCommunityPosition(position);
            }
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
        Button joinButton;

        public CommunityViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.communityName);
            textViewDescription = itemView.findViewById(R.id.communityDescription);
            joinButton = itemView.findViewById(R.id.joinCommunityButton);
        }
    }

    public interface OnCommunityClickListener {
        void onCommunityClick(int position);
    }
}
