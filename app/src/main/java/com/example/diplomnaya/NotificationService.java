package com.example.diplomnaya;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationService extends Service {

    private TaskDatabaseHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        // Инициализация базы данных
        dbHelper = new TaskDatabaseHelper(this);
        // Создание канала уведомлений
        createNotificationChannel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        // Проверка версии SDK, так как создание каналов уведомлений требуется только для API 26 и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = "Как будут появляться уведомления";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            // Создание канала уведомлений
            NotificationChannel channel = new NotificationChannel("channel_name", name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            long[] vibrationPattern = {1000, 500, 1000};
            channel.setVibrationPattern(vibrationPattern);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            // Получение менеджера уведомлений и создание канала
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Запуск планирования уведомлений
        scheduleNotification();

        // Возвращаем START_NOT_STICKY, так как служба не должна быть перезапущена автоматически,
        // если она будет остановлена системой
        return START_NOT_STICKY;
    }

    private void scheduleNotification() {

        long intervalWeek = 7 * 24 * 60 * 60 * 1000L; // неделя в миллисекундах

        dbHelper.getAllTasks(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null && task.isNotify()) {
                        // Combine date and time
                        String dateTime = task.getDateCreated() + " " + task.getTimeCreated();

                        // Check if time is specified
                        if (task.getTimeCreated() == null || task.getTimeCreated().isEmpty()) {
                            Toast.makeText(NotificationService.this, "Please specify the time for the task", Toast.LENGTH_SHORT).show();
                            continue; // Skip the current task if time is not specified
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        try {
                            Date date = sdf.parse(dateTime);
                            long triggerAtMillis = date.getTime();

                            // Create intent for NotificationHelper
                            Intent notificationIntent = new Intent(NotificationService.this, NotificationHelper.class);
                            notificationIntent.putExtra("TASK_TEXT", task.getText());
                            notificationIntent.putExtra("TASK_ID", task.getId());
                            notificationIntent.putExtra("IS_REPEATING", task.isRepeating());
                            notificationIntent.putExtra("REPEATING_TIME", task.getRepeatingTime());
                            ArrayList<Integer> repeatingDays = new ArrayList<>(task.getRepeatingDays());
                            notificationIntent.putExtra("REPEATING_DAYS", repeatingDays);


                            // Convert task ID to an integer request code
                            int requestCode = task.getId().hashCode();

                            // Create PendingIntent
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(NotificationService.this, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                            // Get AlarmManager
                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                            if (task.isRepeating()) {
                                // Plan repeating tasks
                                for (int dayOfWeek : task.getRepeatingDays()) {
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(date);
                                    calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek + 1); // Sunday is 1, Saturday is 7

                                    // Split time into hour and minute
                                    String[] timeParts = task.getRepeatingTime().split(":");
                                    int hour = Integer.parseInt(timeParts[0]);
                                    int minute = Integer.parseInt(timeParts[1]);
                                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                                    calendar.set(Calendar.MINUTE, minute);

                                    // Create a unique request code for each day of the week
                                    int requestCodeForDay = requestCode + dayOfWeek;

                                    // Create a new PendingIntent for each day of the week
                                    PendingIntent repeatingPendingIntent = PendingIntent.getBroadcast(
                                            NotificationService.this, requestCodeForDay, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                    // Set the repeating alarm using AlarmManager
                                    alarmManager.setRepeating(
                                            AlarmManager.RTC_WAKEUP,
                                            calendar.getTimeInMillis(),
                                            intervalWeek,
                                            repeatingPendingIntent
                                    );

                                }
                            } else {
                                // Plan non-repeating tasks
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                            }

                            // Ensure the notification channel is created
                            if (!isNotificationChannelCreated("channel_name")) {
                                createNotificationChannel();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(NotificationService.this, "Error fetching data from Firebase: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private boolean isNotificationChannelCreated(String channelId) {
        // Проверяем, создан ли канал уведомлений с данным идентификатором
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
            return channel != null;
        }
        // Если версия SDK ниже 26, считаем, что каналы уведомлений не требуются
        return true;
    }
}
