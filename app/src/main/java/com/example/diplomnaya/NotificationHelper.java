package com.example.diplomnaya;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class NotificationHelper extends BroadcastReceiver {

    public static void sendNotificationToUser(Context mContext, String memberId, Task task) {
        // Проверяем наличие разрешения на отправку уведомлений
        if (NotificationManagerCompat.from(mContext).areNotificationsEnabled()) {
            // Проверяем, является ли текущий пользователь создателем задачи
            if (!memberId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                // Проверяем, активно ли приложение
                if (!isAppInForeground(mContext)) {
                    // Создание PendingIntent для перехода к активности при нажатии на уведомление
                    Intent intent = new Intent(mContext, MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    // Создание уведомления
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, "channel_name")
                            .setSmallIcon(R.drawable.notification_icon)
                            .setContentTitle("Задача обновлена")
                            .setContentText(task.getTitle())
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_HIGH);

                    // Отправка уведомления
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
                    notificationManager.notify(memberId.hashCode(), builder.build());
                }
            }
        } else {
            // Если разрешение на отправку уведомлений не предоставлено, вы можете предложить пользователю включить уведомления в настройках
            // или выполнить другое действие в зависимости от вашего приложения.
        }
    }

    // Метод для проверки, активно ли приложение
    private static boolean isAppInForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
        if (processes != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
                if (processInfo.processName.equals(context.getPackageName()) && processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                }
            }
        }
        return false;
    }




    @Override
    public void onReceive(Context context, Intent intent) {
        String taskText = intent.getStringExtra("TASK_TEXT");
        int taskId = intent.getIntExtra("TASK_ID", 0);
        boolean isRepeating = intent.getBooleanExtra("IS_REPEATING", false);
        List<String> memberIds = intent.getStringArrayListExtra("MEMBER_IDS");

        // Создание намерения для открытия активности WorkSpace
        Intent workspaceIntent = new Intent(context, WorkSpace.class);

        // Создание PendingIntent для открытия активности WorkSpace
        PendingIntent pendingIntent = PendingIntent.getActivity(context, taskId, workspaceIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

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
            nextIntent.putExtra("MEMBER_IDS", (ArrayList<String>) memberIds);

            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, taskId, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Планируем следующее уведомление
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, nextPendingIntent);
                        } else {
                            Log.e("NotificationHelper", "Cannot schedule exact alarms");
                        }
                    } else {
                        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, nextPendingIntent);
                    }
                } catch (SecurityException e) {
                    Log.e("NotificationHelper", "SecurityException: Cannot schedule exact alarms", e);
                }
            }
        }
    }
}
