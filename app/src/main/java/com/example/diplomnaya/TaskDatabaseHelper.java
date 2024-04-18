package com.example.diplomnaya;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;



public class TaskDatabaseHelper {

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private Context mContext;

    public TaskDatabaseHelper(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mContext = context;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Пользователь вошел в систему, создаем ссылку на базу данных Firebase для пользователя
            databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid())
                    .child("tasks");
            if (databaseReference == null) {
                Log.e("TaskDatabaseHelper", "databaseReference is null");
            }
        } else {
            // Пользователь не вошел в систему, показываем предупреждение
            Toast.makeText(context, "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
            databaseReference = null;
        }
    }


    // Метод для добавления задачи в Firebase Realtime Database
    // Метод для добавления задачи в Firebase Realtime Database
    public void addTask(Task task) {
        if (databaseReference == null) {
            Log.e("TaskDatabaseHelper", "databaseReference is null. Cannot add task.");
            return;
        }
        // Генерируем уникальный ключ для новой задачи в Firebase
        String key = databaseReference.push().getKey();
        if (key != null) {
            // Устанавливаем id задачи в объекте задачи
            task.setId(key);
            // Устанавливаем задачу по ключу в Firebase
            databaseReference.child(key).setValue(task)
                    .addOnSuccessListener(aVoid -> Log.d("TaskDatabaseHelper", "Задача успешно добавлена"))
                    .addOnFailureListener(e -> Log.e("TaskDatabaseHelper", "Ошибка добавления задачи", e));
        }
    }

    // Метод для обновления задачи в Firebase Realtime Database
    // Метод для обновления задачи в Firebase Realtime Database
    public void updateTask(Task task) {
        if (databaseReference == null) {
            Log.e("TaskDatabaseHelper", "databaseReference is null. Cannot update task.");
            return;
        }
        // Используем id задачи для обновления соответствующей записи в Firebase
        String taskId = task.getId();
        DatabaseReference taskRef = databaseReference.child(taskId);
        taskRef.setValue(task)
                .addOnSuccessListener(aVoid -> Log.d("TaskDatabaseHelper", "Задача успешно обновлена в Firebase"))
                .addOnFailureListener(e -> Log.e("TaskDatabaseHelper", "Ошибка обновления задачи в Firebase", e));
    }

    // Метод для удаления задачи из Firebase Realtime Database
    // Метод для удаления задачи из Firebase Realtime Database
    public void deleteTask(Task task) {
        if (databaseReference == null) {
            Log.e("TaskDatabaseHelper", "databaseReference is null. Cannot delete task.");
            return;
        }
        // Используем id задачи для удаления соответствующей записи из Firebase
        String taskId = task.getId();
        databaseReference.child(taskId).removeValue()
                .addOnSuccessListener(aVoid -> Log.d("TaskDatabaseHelper", "Задача успешно удалена из Firebase"))
                .addOnFailureListener(e -> Log.e("TaskDatabaseHelper", "Ошибка удаления задачи из Firebase", e));
    }

    // Метод для получения всех задач из Firebase Realtime Database
    public void getAllTasks(ValueEventListener valueEventListener) {
        if (databaseReference == null) {
            Log.e("TaskDatabaseHelper", "databaseReference is null. Cannot get tasks.");
            return;
        }
        databaseReference.addListenerForSingleValueEvent(valueEventListener);
    }

    // Обработка ошибок базы данных Firebase
    private void handleFirebaseError(DatabaseError databaseError) {
        String errorMessage = databaseError.getMessage();
        Log.e("FirebaseDatabase", "Ошибка: " + errorMessage);
        Toast.makeText(mContext, "Ошибка загрузки данных: " + errorMessage, Toast.LENGTH_SHORT).show();
    }
}
