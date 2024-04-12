package com.example.diplomnaya;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;

import androidx.core.app.NotificationManagerCompat;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;


public class NotificationHelper extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String taskText = intent.getStringExtra("TASK_TEXT");
        int taskId = intent.getIntExtra("TASK_ID", 0);

        // Создание намерения для открытия активности WorkSpace
        Intent workspaceIntent = new Intent(context, WorkSpace.class);
        // Если вы хотите передать дополнительные данные в активность, вы можете использовать putExtra()
        // workspaceIntent.putExtra("key", "value");

        // Создание PendingIntent для открытия активности WorkSpace
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, workspaceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Создание уведомления с установленным PendingIntent
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_name")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Напоминание о задаче")
                .setContentText(taskText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Устанавливаем звук по умолчанию
                .setVibrate(new long[]{1000, 500, 1000})
                .setContentIntent(pendingIntent) // Установка PendingIntent для открытия активности при нажатии на уведомление
                .setAutoCancel(true); // Закрыть уведомление после нажатия

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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

