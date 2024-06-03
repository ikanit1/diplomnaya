package com.example.diplomnaya;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NotificationService extends Service {

    private TaskDatabaseHelper dbHelper;
    private BootReceiver bootReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new TaskDatabaseHelper(this);
        createNotificationChannel();
        registerBootReceiver();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = "Как будут появляться уведомления";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel("channel_name", name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            long[] vibrationPattern = {1000, 500, 1000};
            channel.setVibrationPattern(vibrationPattern);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void registerBootReceiver() {
        bootReceiver = new BootReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        registerReceiver(bootReceiver, filter);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Task task = intent.getParcelableExtra("TASK");

        if (task != null) {
            scheduleNotification(task);
        } else {
            dbHelper.getAllTasks(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Task> tasks = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Task t = snapshot.getValue(Task.class);
                        tasks.add(t);
                    }

                    for (Task t : tasks) {
                        scheduleNotification(t);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(NotificationService.this, "Error loading tasks", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return START_NOT_STICKY;
    }

    private void scheduleNotification(Task task) {
        if (task.isNotify()) {
            String[] timeParts = task.getRepeatingTime().split(":");
            int repeatingHour = Integer.parseInt(timeParts[0]);
            int repeatingMinute = Integer.parseInt(timeParts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, repeatingHour);
            calendar.set(Calendar.MINUTE, repeatingMinute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DATE, 1);
            }

            Intent notificationIntent = new Intent(this, NotificationHelper.class);
            notificationIntent.putExtra("TASK_TEXT", task.getText());
            notificationIntent.putExtra("TASK_ID", task.getId());
            notificationIntent.putExtra("IS_REPEATING", task.isRepeating());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, task.getId().hashCode(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            if (task.isRepeating()) {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY * 7, pendingIntent);
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bootReceiver);
    }

    public class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                context.startService(new Intent(context, NotificationService.class));
            }
        }
    }
}
