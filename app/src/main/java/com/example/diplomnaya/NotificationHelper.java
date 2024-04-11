package com.example.diplomnaya;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private Context mContext;
    private static final String CHANNEL_ID = "your_channel_id";
    private static final String CHANNEL_NAME = "Your Channel Name";

    public NotificationHelper(Context context) {
        mContext = context;
    }

    public void createNotification(String title, String message, int notificationId) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext, CHANNEL_ID)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(notificationId, builder.build());
    }
}
