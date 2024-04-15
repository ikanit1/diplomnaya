package com.example.diplomnaya;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.SystemClock;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String taskText = intent.getStringExtra("TASK_TEXT");
        int taskId = intent.getIntExtra("TASK_ID", 0);
        boolean isRepeating = intent.getBooleanExtra("IS_REPEATING", false);

        // Создание намерения для открытия активности WorkSpace
        Intent workspaceIntent = new Intent(context, WorkSpace.class);
        // Если вы хотите передать дополнительные данные в активность, вы можете использовать putExtra()
        // workspaceIntent.putExtra("key", "value");

        // Создание PendingIntent для открытия активности WorkSpace
        PendingIntent pendingIntent = PendingIntent.getActivity(context, taskId, workspaceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Создание уведомления с установленным PendingIntent
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_name")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Напоминание о задаче")
                .setContentText(taskText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(taskText)) // Установка расширенного макета для уведомления
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Устанавливаем звук по умолчанию
                .setVibrate(new long[]{1000, 500, 1000})
                .setContentIntent(pendingIntent) // Установка PendingIntent для открытия активности при нажатии на уведомление
                .setAutoCancel(true); // Закрыть уведомление после нажатия

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(taskId, builder.build());


        if (isRepeating) {
            // Получаем время следующего уведомления
            long triggerAtMillis = SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_DAY;

            // Создаем новое намерение для следующего уведомления
            Intent nextIntent = new Intent(context, NotificationHelper.class);
            nextIntent.putExtra("TASK_TEXT", taskText);
            nextIntent.putExtra("TASK_ID", taskId);
            nextIntent.putExtra("IS_REPEATING", true);

            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, taskId, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Проверяем разрешение на установку будильника
            if (context.checkSelfPermission(Manifest.permission.SET_ALARM) == PackageManager.PERMISSION_GRANTED) {
                // Планируем следующее уведомление
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, nextPendingIntent);
                }
            }
        }
    }
}
