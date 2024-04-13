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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
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
            String description = "Как будут появляться заметки";
            int importance = NotificationManager.IMPORTANCE_HIGH; // Используем IMPORTANCE_HIGH для важных уведомлений

            // Установка вибрации
            long[] vibrationPattern = {1000, 500, 1000}; // Паттерн вибрации (пауза, вибрация, пауза, вибрация)

            // Создание канала уведомлений
            NotificationChannel channel = new NotificationChannel("channel_name", name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setVibrationPattern(vibrationPattern);

            // Установка настройки, чтобы уведомления показывались на экране блокировки
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            // Получение менеджера уведомлений и создание канала
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Планирование уведомления с использованием AlarmManager
        scheduleNotification();

        // Возвращаем START_NOT_STICKY, так как служба не должна быть перезапущена автоматически,
        // если она будет остановлена системой после завершения выполнения
        return START_NOT_STICKY;
    }

    private void scheduleNotification() {
        // Получаем список задач из базы данных
        TaskDatabaseHelper dbHelper = new TaskDatabaseHelper(this);
        for (Task task : dbHelper.getAllTasks()) {
            // Планируем уведомление только для задач, которые требуют уведомления
            if (task.isNotify()) {
                // Получение даты и времени задачи
                String dateTime = task.getDateCreated() + " " + task.getTimeCreated();

                // Проверка наличия времени
                if (task.getTimeCreated().equals("Время не установлено")) {
                    Toast.makeText(this, "Пожалуйста, укажите время для задачи", Toast.LENGTH_SHORT).show();
                    continue; // Если время не указано, переходим к следующей задаче
                }

                // Преобразование строки даты и времени в объект Date
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                try {
                    Date date = sdf.parse(dateTime);
                    long triggerAtMillis = date.getTime();

                    // Создание намерения для вызова BroadcastReceiver, который обрабатывает уведомление
                    Intent notificationIntent = new Intent(this, NotificationHelper.class);
                    notificationIntent.putExtra("TASK_TEXT", task.getText());
                    notificationIntent.putExtra("TASK_ID", task.getId());

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, task.getId(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    // Установка уведомления с использованием AlarmManager
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);

                    // Проверка, создан ли канал уведомлений
                    if (!isNotificationChannelCreated("channel_name")) {
                        createNotificationChannel();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isNotificationChannelCreated(String channelId) {
        // Проверяем, создан ли канал уведомлений с заданным идентификатором
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
            return channel != null;
        }
        // Если SDK меньше версии 26, возвращаем true, так как каналы уведомлений не требуются
        return true;
    }
}
