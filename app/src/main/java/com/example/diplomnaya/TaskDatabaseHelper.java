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
    private final DatabaseReference databaseReference;
    private final FirebaseAuth mAuth;
    private final Context mContext;
    private String currentGroupId;

    public TaskDatabaseHelper(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mContext = context;
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid())
                    .child("tasks");
            setCurrentGroupId(currentUser.getUid());
        } else {
            showToast("Пожалуйста, войдите в систему");
            databaseReference = null;
        }
    }

    private void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    private void setCurrentGroupId(String userId) {
        DatabaseReference userGroupsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("groups");

        userGroupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                        currentGroupId = groupSnapshot.getKey();
                        break;  // Предполагаем, что текущая группа - первая в списке
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TaskDatabaseHelper", "Ошибка получения идентификатора группы", databaseError.toException());
            }
        });
    }

    public void addTask(Task task) {
        if (databaseReference == null) {
            Log.e("TaskDatabaseHelper", "databaseReference is null. Cannot add task.");
            return;
        }

        if (task.getGroupId() != null) {
            checkGroupExistence(task.getGroupId(), new GroupExistenceCallback() {
                @Override
                public void onGroupExists() {
                    addTaskToDatabase(task, true);  // Добавляем с флагом для распределения задачи
                }

                @Override
                public void onGroupNotExists() {
                    showToast("Группа, в которой была создана задача, больше не существует. Задача не добавлена.");
                }
            });
        } else {
            // Если GroupId не установлен, использовать текущую группу
            task.setGroupId(currentGroupId);
            addTaskToDatabase(task, true);  // Добавляем с флагом для распределения задачи
        }
    }

    private void addTaskToDatabase(Task task, boolean distributeTask) {
        String key = databaseReference.push().getKey();
        if (key != null) {
            task.setId(key);
            if (task.getTitle() == null || task.getTitle().isEmpty()) {
                task.setTitle("Безымянный");
            }
            databaseReference.child(key).setValue(task)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("TaskDatabaseHelper", "Задача успешно добавлена");
                        if (distributeTask && task.getGroupId() != null) {
                            distributeTaskToGroupMembers(task);
                        } else {
                            Log.e("TaskDatabaseHelper", "GroupId is null. Task not distributed.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        handleException(e);
                        Log.e("TaskDatabaseHelper", "Ошибка добавления задачи", e);
                    });
        } else {
            Log.e("TaskDatabaseHelper", "Ошибка создания уникального ключа для задачи");
        }
    }

    private void checkGroupExistence(String groupId, GroupExistenceCallback callback) {
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId);
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    callback.onGroupExists();
                } else {
                    callback.onGroupNotExists();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TaskDatabaseHelper", "Ошибка проверки существования группы", databaseError.toException());
            }
        });
    }

    private void distributeTaskToGroupMembers(Task task) {
        String groupId = task.getGroupId();
        if (groupId == null) {
            Log.e("TaskDatabaseHelper", "GroupId is null. Cannot distribute task.");
            return;
        }
        DatabaseReference groupMembersRef = FirebaseDatabase.getInstance()
                .getReference("groups")
                .child(groupId)
                .child("members");

        groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                    String memberId = memberSnapshot.getKey();
                    if (!memberId.equals(mAuth.getCurrentUser().getUid())) {
                        DatabaseReference memberTasksRef = FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(memberId)
                                .child("tasks");
                        memberTasksRef.child(task.getId()).setValue(task)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("TaskDatabaseHelper", "Task successfully distributed to member: " + memberId);
                                    NotificationHelper.sendNotificationToUser(mContext, memberId, task);
                                })
                                .addOnFailureListener(e -> Log.e("TaskDatabaseHelper", "Error distributing task to member: " + memberId, e));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TaskDatabaseHelper", "Error retrieving group members data", databaseError.toException());
            }
        });
    }

    public void updateTask(Task task) {
        if (databaseReference == null || task.getId() == null) {
            Log.e("TaskDatabaseHelper", "databaseReference or taskId is null. Cannot update task.");
            return;
        }

        databaseReference.child(task.getId()).setValue(task)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TaskDatabaseHelper", "Задача успешно обновлена в Firebase");
                    updateTaskForGroupMembers(task);
                })
                .addOnFailureListener(e -> {
                    handleException(e);
                    Log.e("TaskDatabaseHelper", "Ошибка обновления задачи в Firebase", e);
                });
    }

    public void updateTaskCompletionStatus(Task task) {
        if (databaseReference == null || task.getId() == null) {
            Log.e("TaskDatabaseHelper", "databaseReference or taskId is null. Cannot update task.");
            return;
        }

        databaseReference.child(task.getId()).child("completed").setValue(task.isCompleted())
                .addOnSuccessListener(aVoid -> {
                    Log.d("TaskDatabaseHelper", "Статус выполнения задачи успешно обновлен в Firebase");
                    updateTaskCompletionStatusForGroupMembers(task);
                })
                .addOnFailureListener(e -> {
                    handleException(e);
                    Log.e("TaskDatabaseHelper", "Ошибка обновления статуса выполнения задачи в Firebase", e);
                });
    }

    private void updateTaskCompletionStatusForGroupMembers(Task task) {
        String groupId = task.getGroupId();
        if (groupId == null) {
            Log.e("TaskDatabaseHelper", "GroupId is null. Cannot update task for group members.");
            return;
        }

        DatabaseReference groupMembersRef = FirebaseDatabase.getInstance().getReference("groups")
                .child(groupId)
                .child("members");

        groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                    String memberId = memberSnapshot.getKey();
                    DatabaseReference memberTasksRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(memberId)
                            .child("tasks");

                    memberTasksRef.orderByChild("id").equalTo(task.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                                taskSnapshot.getRef().child("completed").setValue(task.isCompleted())
                                        .addOnSuccessListener(aVoid -> Log.d("TaskDatabaseHelper", "Статус выполнения задачи успешно обновлен для участника: " + memberId))
                                        .addOnFailureListener(e -> Log.e("TaskDatabaseHelper", "Ошибка обновления статуса выполнения задачи для участника: " + memberId, e));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("TaskDatabaseHelper", "Ошибка получения данных о задачах участников", databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TaskDatabaseHelper", "Ошибка получения данных о группах", databaseError.toException());
            }
        });
    }

    private void updateTaskForGroupMembers(Task task) {
        String groupId = task.getGroupId();
        if (groupId == null) {
            Log.e("TaskDatabaseHelper", "GroupId is null. Cannot update task for group members.");
            return;
        }

        DatabaseReference groupMembersRef = FirebaseDatabase.getInstance().getReference("groups")
                .child(groupId)
                .child("members");

        groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
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

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TaskDatabaseHelper", "Ошибка получения данных о группах", databaseError.toException());
            }
        });
    }

    public void deleteTask(Task task) {
        if (databaseReference == null || task.getId() == null) {
            Log.e("TaskDatabaseHelper", "databaseReference or taskId is null. Cannot delete task.");
            return;
        }

        databaseReference.child(task.getId()).removeValue()
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
        String groupId = task.getGroupId();
        if (groupId == null) {
            Log.e("TaskDatabaseHelper", "GroupId is null. Cannot delete task for group members.");
            return;
        }

        DatabaseReference groupMembersRef = FirebaseDatabase.getInstance().getReference("groups")
                .child(groupId)
                .child("members");

        groupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
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

        databaseReference.addValueEventListener(valueEventListener);
    }

    private void handleException(Exception exception) {
        String errorMessage = exception.getMessage();
        Log.e("TaskDatabaseHelper", "Exception: " + errorMessage, exception);
        Toast.makeText(mContext, "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
    }



    private interface GroupExistenceCallback {
        void onGroupExists();
        void onGroupNotExists();
    }
}