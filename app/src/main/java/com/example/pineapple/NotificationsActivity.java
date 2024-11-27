package com.example.pineapple;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends BaseActivity {
    private RecyclerView notificationRecyclerView;
    private FirebaseFirestore db;
    private List<Notification> notifList;
    private NotificationAdapter notificationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityLayout(R.layout.activity_notifications);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize RecyclerView
        notificationRecyclerView = findViewById(R.id.notificationsRecyclerView);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        notifList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notifList);
        notificationRecyclerView.setAdapter(notificationAdapter);


        fetchNotifications(user.getUid());
    }

    private void fetchNotifications(String userId) {
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notifList.clear();
                    for(QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Notification notif = document.toObject(Notification.class);
                        notif.setId(document.getId());
                        notifList.add(notif);
                    }
                    notificationAdapter.notifyDataSetChanged();
                });
    }
}