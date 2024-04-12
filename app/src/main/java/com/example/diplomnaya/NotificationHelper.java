package com.example.diplomnaya;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.content.BroadcastReceiver;
import android.media.RingtoneManager;

import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;

public class NotificationHelper extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String taskText = intent.getStringExtra("TASK_TEXT");
        int taskId = intent.getIntExtra("TASK_ID", 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_1")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Напоминание о задаче")
                .setContentText(taskText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{1000, 500, 1000});

        // Здесь задается массив, который определяет паузы и длительности вибрации (в миллисекундах)

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(taskId, builder.build());
    }
}

