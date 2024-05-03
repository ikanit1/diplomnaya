package com.example.diplomnaya;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class TaskDatabaseHelper {

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private Context mContext;

    public TaskDatabaseHelper(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mContext = context;
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Инициализация databaseReference для текущего пользователя
            databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid())
                    .child("tasks");

            if (databaseReference == null) {
                Log.e("TaskDatabaseHelper", "databaseReference is null");
                showToast("Ошибка инициализации базы данных");
            }
        } else {
            // Пользователь не вошел в систему
            showToast("Пожалуйста, войдите в систему");
            return; // Завершаем конструктор, чтобы избежать работы с пустой databaseReference
        }
    }

    private void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    // Метод для добавления задачи в Firebase Realtime Database
    public void addTask(Task task) {
        if (databaseReference == null) {
            Log.e("TaskDatabaseHelper", "databaseReference is null. Cannot add task.");
            return;
        }

        // Генерация уникального ключа для новой задачи
        String key = databaseReference.push().getKey();

        if (key != null) {
            task.setId(key);
            // Установите значение repeatingDays перед добавлением задачи
            List<Integer> repeatingDays = task.getRepeatingDays();
            task.setRepeatingDays(repeatingDays);

            // Добавление задачи в базу данных
            databaseReference.child(key).setValue(task)
                    .addOnSuccessListener(aVoid -> Log.d("TaskDatabaseHelper", "Задача успешно добавлена"))
                    .addOnFailureListener(e -> {
                        handleException(e);
                        Log.e("TaskDatabaseHelper", "Ошибка добавления задачи", e);
                    });
        } else {
            Log.e("TaskDatabaseHelper", "Ошибка создания уникального ключа для задачи");
        }
    }


    // Метод для обновления задачи в Firebase Realtime Database
    public void updateTask(Task task) {
        if (databaseReference == null) {
            Log.e("TaskDatabaseHelper", "databaseReference is null. Cannot update task.");
            return;
        }

        String taskId = task.getId();
        if (taskId == null) {
            Log.e("TaskDatabaseHelper", "taskId is null. Cannot update task.");
            return;
        }

        DatabaseReference taskRef = databaseReference.child(taskId);
        // Установите значение repeatingDays перед обновлением задачи
        List<Integer> repeatingDays = task.getRepeatingDays();
        task.setRepeatingDays(repeatingDays);

        taskRef.setValue(task)
                .addOnSuccessListener(aVoid -> Log.d("TaskDatabaseHelper", "Задача успешно обновлена в Firebase"))
                .addOnFailureListener(e -> {
                    handleException(e);
                    Log.e("TaskDatabaseHelper", "Ошибка обновления задачи в Firebase", e);
                });
    }



    // Метод для удаления задачи из Firebase Realtime Database
    public void deleteTask(Task task) {
        if (databaseReference == null) {
            Log.e("TaskDatabaseHelper", "databaseReference is null. Cannot delete task.");
            return;
        }

        String taskId = task.getId();
        if (taskId == null) {
            Log.e("TaskDatabaseHelper", "taskId is null. Cannot delete task.");
            return;
        }

        databaseReference.child(taskId).removeValue()
                .addOnSuccessListener(aVoid -> Log.d("TaskDatabaseHelper", "Задача успешно удалена из Firebase"))
                .addOnFailureListener(e -> {
                    handleException(e);
                    Log.e("TaskDatabaseHelper", "Ошибка удаления задачи из Firebase", e);
                });
    }

    // Метод для получения всех задач из Firebase Realtime Database
    public void getAllTasks(ValueEventListener valueEventListener) {
        if (databaseReference == null) {
            Log.e("TaskDatabaseHelper", "databaseReference is null. Cannot get tasks.");
            return;
        }

        databaseReference.addListenerForSingleValueEvent(valueEventListener);
    }

    // Метод обработки ошибок базы данных Firebase
    private void handleFirebaseError(DatabaseError error) {
        String errorMessage = error.getMessage();
        Log.e("TaskDatabaseHelper", "Ошибка: " + errorMessage);
        Toast.makeText(mContext, "Ошибка загрузки данных: " + errorMessage, Toast.LENGTH_SHORT).show();
    }

    // Метод для обработки исключений (Exception) в addOnFailureListener
    private void handleException(Exception exception) {
        String errorMessage = exception.getMessage();
        Log.e("TaskDatabaseHelper", "Exception: " + errorMessage, exception);
        Toast.makeText(mContext, "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
    }
}
