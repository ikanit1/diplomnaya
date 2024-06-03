package com.example.diplomnaya;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class TaskCheckService extends Service {

    private static final long INTERVAL = 60000; // Период проверки задач (в миллисекундах)
    private final Handler handler;
    private final FirebaseAuth mAuth;
    private final Context mContext;

    public TaskCheckService(Handler handler, Context context, FirebaseAuth auth) {
        this.handler = handler;
        this.mContext = context;
        this.mAuth = auth;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Запускаем проверку задач
        handler.postDelayed(taskChecker, INTERVAL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Останавливаем проверку задач при уничтожении службы
        handler.removeCallbacks(taskChecker);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Runnable для выполнения проверки задач
    private final Runnable taskChecker = new Runnable() {
        @Override
        public void run() {
            // Проверяем наличие новых задач и отправляем уведомления при необходимости
            checkForNewTasksAndSendNotifications();

            // Повторяем проверку через заданный интервал
            handler.postDelayed(this, INTERVAL);
        }
    };

    private void checkForNewTasksAndSendNotifications() {
        TaskDatabaseHelper taskDatabaseHelper = new TaskDatabaseHelper(mContext);

        // Получаем все задачи из базы данных
        taskDatabaseHelper.getAllTasks(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);

                    // Здесь вы можете реализовать логику проверки наличия новых задач
                    // Например, можно сравнить время создания задачи с текущим временем и определить, является ли она новой

                    // После проверки наличия новой задачи, отправляем уведомление, если необходимо
                    if (isNewTask(task)) {
                        // Отправляем уведомление
                        NotificationHelper.sendNotificationToUser(mContext, Objects.requireNonNull(mAuth.getCurrentUser()).getUid(), task);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TaskDatabaseHelper", "Ошибка получения списка задач", databaseError.toException());
            }
        });
    }

    // Метод для проверки, является ли задача новой
    private boolean isNewTask(Task task) {
        // Здесь вы можете реализовать свою логику проверки, например, сравнить время создания задачи с текущим временем
        // В данном примере просто возвращаем true для всех задач
        return true;
    }

}
