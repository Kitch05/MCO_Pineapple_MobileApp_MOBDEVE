package com.example.pineapple;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.InboxViewHolder> {

    private List<ForumInboxItem> inboxItemList;
    private Context context;

    public InboxAdapter(Context context, List<ForumInboxItem> inboxItemList) {
        this.context = context;
        this.inboxItemList = inboxItemList;
    }

    @NonNull
    @Override
    public InboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inbox_message, parent, false);
        return new InboxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxViewHolder holder, int position) {
        ForumInboxItem item = inboxItemList.get(position);
        holder.senderName.setText(item.getSender());
        holder.messagePreview.setText(item.getMessagePreview());
        holder.timestamp.setText(item.getTimestamp());

        // Change text style based on whether the message is read or unread
        if (item.isRead()) {
            holder.messagePreview.setAlpha(0.6f); // Dim the preview for read messages
        } else {
            holder.messagePreview.setAlpha(1.0f); // Full opacity for unread messages
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, InboxDetailActivity.class);

                intent.putExtra("message_content", item.getFullMessage());
                intent.putExtra("sender_name", item.getSender()); // Optionally pass the sender name too


                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return inboxItemList.size();
    }

    public static class InboxViewHolder extends RecyclerView.ViewHolder {
        TextView senderName, messagePreview, timestamp;

        public InboxViewHolder(@NonNull View itemView) {
            super(itemView);
            senderName = itemView.findViewById(R.id.sender_name);
            messagePreview = itemView.findViewById(R.id.message_preview);
            timestamp = itemView.findViewById(R.id.message_timestamp);
        }
    }
}
