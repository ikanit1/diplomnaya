package com.example.diplomnaya;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
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
            databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid())
                    .child("tasks");

            if (databaseReference == null) {
                Log.e("TaskDatabaseHelper", "databaseReference is null");
                showToast("Ошибка инициализации базы данных");
            }
        } else {
            showToast("Пожалуйста, войдите в систему");
            return;
        }
    }

    private void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public void addTask(Task task) {
        if (databaseReference == null) {
            Log.e("TaskDatabaseHelper", "databaseReference is null. Cannot add task.");
            return;
        }

        // Проверяем, существует ли группа, в которой создана задача
        if (task.getGroupId() != null) {
            checkGroupExistence(task.getGroupId(), task);
        }

        String key = databaseReference.push().getKey();

        if (key != null) {
            String taskTitle = task.getTitle();
            if (taskTitle == null || taskTitle.isEmpty()) {
                taskTitle = "Безымянный"; // Установка значения "Безымянный", если заголовок пустой
            }

            // Проверяем, указано ли время в задаче
            String taskTime = task.getRepeatingTime();

            task.setId(key);
            task.setTitle(taskTitle);
            task.setRepeatingTime(taskTime); // Устанавливаем время задачи

            databaseReference.child(key).setValue(task)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("TaskDatabaseHelper", "Задача успешно добавлена");
                        distributeTaskToGroupMembers(task);
                    })
                    .addOnFailureListener(e -> {
                        handleException(e);
                        Log.e("TaskDatabaseHelper", "Ошибка добавления задачи", e);
                    });
        } else {
            Log.e("TaskDatabaseHelper", "Ошибка создания уникального ключа для задачи");
        }
    }

    // Метод для проверки существования группы по её идентификатору
    private void checkGroupExistence(String groupId, Task task) {
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId);
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Группа не существует, удаляем задачу
                    deleteTask(task);
                    showToast("Группа, в которой была создана задача, больше не существует. Задача удалена.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TaskDatabaseHelper", "Ошибка проверки существования группы", databaseError.toException());
            }
        });
    }

    private void distributeTaskToGroupMembers(Task task) {
        DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("groups");

        groupsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    if (groupSnapshot.child("creatorId").getValue(String.class).equals(mAuth.getCurrentUser().getUid())) {
                        String groupCreatorId = mAuth.getCurrentUser().getUid(); // Идентификатор создателя группы
                        for (DataSnapshot memberSnapshot : groupSnapshot.child("members").getChildren()) {
                            String memberId = memberSnapshot.getKey();
                            if (!memberId.equals(groupCreatorId)) { // Избегаем дублирования у создателя
                                DatabaseReference memberTasksRef = FirebaseDatabase.getInstance()
                                        .getReference("users")
                                        .child(memberId)
                                        .child("tasks");

                                String memberTaskKey = memberTasksRef.push().getKey();
                                if (memberTaskKey != null) {
                                    memberTasksRef.child(memberTaskKey).setValue(task)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("TaskDatabaseHelper", "Задача успешно распределена участнику: " + memberId);
                                                sendNotificationToUser(mContext, memberId, task, groupCreatorId); // Отправка уведомления всем участникам группы
                                            })
                                            .addOnFailureListener(e -> Log.e("TaskDatabaseHelper", "Ошибка распределения задачи участнику: " + memberId, e));
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TaskDatabaseHelper", "Ошибка получения данных о группах", databaseError.toException());
            }
        });
    }

    private void sendNotificationToUser(Context mContext, String groupId, Task task, String groupCreatorId) {
        DatabaseReference groupMembersRef = FirebaseDatabase.getInstance()
                .getReference("groups")
                .child(groupId)
                .child("members");

        groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                    String memberId = memberSnapshot.getKey();
                    // Отправить уведомление каждому участнику группы, кроме создателя задачи
                    if (!memberId.equals(mAuth.getCurrentUser().getUid())) {
                        // Вызываем метод sendNotificationToUser из класса NotificationHelper для отправки уведомления
                        NotificationHelper.sendNotificationToUser(TaskDatabaseHelper.this.mContext, memberId, task);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TaskDatabaseHelper", "Ошибка получения данных о членах группы", databaseError.toException());
            }
        });
    }



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
        List<Integer> repeatingDays = task.getRepeatingDays();
        task.setRepeatingDays(repeatingDays);

        taskRef.setValue(task)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TaskDatabaseHelper", "Задача успешно обновлена в Firebase");
                    updateTaskForGroupMembers(task);
                })
                .addOnFailureListener(e -> {
                    handleException(e);
                    Log.e("TaskDatabaseHelper", "Ошибка обновления задачи в Firebase", e);
                });
    }

    private void updateTaskForGroupMembers(Task task) {
        DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("groups");

        groupsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    if (groupSnapshot.child("creatorId").getValue(String.class).equals(mAuth.getCurrentUser().getUid())) {
                        for (DataSnapshot memberSnapshot : groupSnapshot.child("members").getChildren()) {
                            String memberId = memberSnapshot.getKey();
                            DatabaseReference memberTasksRef = FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(memberId)
                                    .child("tasks");

                            memberTasksRef.orderByChild("id").equalTo(task.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                                        taskSnapshot.getRef().setValue(task)
                                                .addOnSuccessListener(aVoid -> Log.d("TaskDatabaseHelper", "Задача успешно обновлена для участника: " + memberId))
                                                .addOnFailureListener(e -> Log.e("TaskDatabaseHelper", "Ошибка обновления задачи для участника: " + memberId, e));
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("TaskDatabaseHelper", "Ошибка получения данных о задачах участников", databaseError.toException());
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TaskDatabaseHelper", "Ошибка получения данных о группах", databaseError.toException());
            }
        });
    }

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
                .addOnSuccessListener(aVoid -> {
                    Log.d("TaskDatabaseHelper", "Задача успешно удалена из Firebase");
                    deleteTaskForGroupMembers(task);
                })
                .addOnFailureListener(e -> {
                    handleException(e);
                    Log.e("TaskDatabaseHelper", "Ошибка удаления задачи из Firebase", e);
                });
    }

    private void deleteTaskForGroupMembers(Task task) {
        DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("groups");

        groupsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    if (groupSnapshot.child("creatorId").getValue(String.class).equals(mAuth.getCurrentUser().getUid())) {
                        for (DataSnapshot memberSnapshot : groupSnapshot.child("members").getChildren()) {
                            String memberId = memberSnapshot.getKey();
                            DatabaseReference memberTasksRef = FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(memberId)
                                    .child("tasks");

                            memberTasksRef.orderByChild("id").equalTo(task.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                                        taskSnapshot.getRef().removeValue()
                                                .addOnSuccessListener(aVoid -> Log.d("TaskDatabaseHelper", "Задача успешно удалена для участника: " + memberId))
                                                .addOnFailureListener(e -> Log.e("TaskDatabaseHelper", "Ошибка удаления задачи для участника: " + memberId, e));
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("TaskDatabaseHelper", "Ошибка получения данных о задачах участников", databaseError.toException());
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TaskDatabaseHelper", "Ошибка получения данных о группах", databaseError.toException());
            }
        });
    }

    public void getAllTasks(ValueEventListener valueEventListener) {
        if (databaseReference == null) {
            Log.e("TaskDatabaseHelper", "databaseReference is null. Cannot get tasks.");
            return;
        }

        // Используем addValueEventListener для отслеживания изменений в реальном времени
        databaseReference.addValueEventListener(valueEventListener);
    }


    private void handleException(Exception exception) {
        String errorMessage = exception.getMessage();
        Log.e("TaskDatabaseHelper", "Exception: " + errorMessage, exception);
        Toast.makeText(mContext, "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
    }
}
