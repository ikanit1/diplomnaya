package com.example.diplomnaya;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


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
        // Получите объект задачи из Intent (обратите внимание, что ключ должен совпадать с ключом, используемым при передаче данных в Intent)
        Task task = intent.getParcelableExtra("TASK");

        if (task != null) {
            // Запуск планирования уведомлений для этой задачи
            scheduleNotification(task);
        } else {
            // Используйте ValueEventListener для запроса списка задач из базы данных
            dbHelper.getAllTasks(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Task> tasks = new ArrayList<>();

                    // Преобразование данных из DataSnapshot в список задач
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Task t = snapshot.getValue(Task.class);
                        tasks.add(t);
                    }

                    // Планируем уведомления для каждой задачи
                    for (Task t : tasks) {
                        scheduleNotification(t);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Обработка ошибок при получении данных из базы данных
                    Toast.makeText(NotificationService.this, "Error loading tasks", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Возвращаем START_NOT_STICKY, так как служба не должна быть перезапущена автоматически,
        // если она будет остановлена системой
        return START_NOT_STICKY;
    }



    // Обновите scheduleNotification(Task task)
    private void scheduleNotification(Task task) {
        if (task.isNotify()) {
            // Разделите время на часы и минуты
            String[] timeParts = task.getRepeatingTime().split(":");
            int repeatingHour = Integer.parseInt(timeParts[0]);
            int repeatingMinute = Integer.parseInt(timeParts[1]);

            // Создайте Calendar для повторяющегося времени
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, repeatingHour);
            calendar.set(Calendar.MINUTE, repeatingMinute);
            calendar.set(Calendar.SECOND, 0);

            // Убедитесь, что время будильника устанавливается в будущее
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DATE, 1);
            }

            // Создайте намерение для уведомления
            Intent notificationIntent = new Intent(this, NotificationHelper.class);
            notificationIntent.putExtra("TASK_TEXT", task.getText());
            notificationIntent.putExtra("TASK_ID", task.getId());
            notificationIntent.putExtra("IS_REPEATING", task.isRepeating());

            // Создайте PendingIntent для уведомления
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, task.getId().hashCode(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Получите AlarmManager и установите будильник
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            // Если задача повторяющаяся, используйте setRepeating
            if (task.isRepeating()) {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY * 7, pendingIntent);
            } else {
                // Для одноразовых задач используйте setExactAndAllowWhileIdle
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    public class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                // Запланируйте уведомления снова после перезапуска устройства
                context.startService(new Intent(context, NotificationService.class));
            }
        }
    }
}
