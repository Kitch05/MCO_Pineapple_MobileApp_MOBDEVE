package com.example.pineapple;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MyCommunitiesDropdownAdapter extends RecyclerView.Adapter<MyCommunitiesDropdownAdapter.ViewHolder> {

    private List<Community> communityList;

    public MyCommunitiesDropdownAdapter(List<Community> communityList) {
        this.communityList = communityList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the mycommunity_item.xml layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mycommunity_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Community community = communityList.get(position);
        holder.communityName.setText(community.getName());
    }

    @Override
    public int getItemCount() {
        return communityList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView communityName;

        public ViewHolder(View itemView) {
            super(itemView);
            // Ensure the ID matches the one in mycommunity_item.xml
            communityName = itemView.findViewById(R.id.communityName);
        }
    }
}

