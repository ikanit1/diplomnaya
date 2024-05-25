package com.example.diplomnaya;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShareTask extends AppCompatActivity {

    private EditText editGroupName,  editGroupCode;
    private Button btnCreateGroup, btnJoinGroup;
    private ListView listViewCreatedGroups, listViewJoinedGroups;

    private DatabaseReference databaseReference;
    private String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_task);

        // Инициализация компонентов
        editGroupName = findViewById(R.id.editGroupName);
        editGroupCode = findViewById(R.id.editGroupCode);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        btnJoinGroup = findViewById(R.id.btnJoinGroup);
        listViewCreatedGroups = findViewById(R.id.listViewCreatedGroups);
        listViewJoinedGroups = findViewById(R.id.listViewJoinedGroups);

        // Инициализация базы данных
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Получение текущего пользователя
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            loadGroups(); // Загрузка групп пользователя
        } else {
            showToast("Пожалуйста, войдите в систему");
            finish(); // Закрытие активности
        }

        // Установка обработчиков событий
        btnCreateGroup.setOnClickListener(this::createGroup);
        btnJoinGroup.setOnClickListener(this::joinGroup);
    }

    // Метод для загрузки групп пользователя
    private void loadGroups() {
        List<Group> createdGroups = new ArrayList<>();
        List<Group> joinedGroups = new ArrayList<>();

        DatabaseReference userGroupsRef = databaseReference.child("users").child(currentUserId).child("groups");

        userGroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                createdGroups.clear(); // Очищаем списки перед загрузкой новых данных
                joinedGroups.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String groupCode = snapshot.getKey();
                    loadGroup(groupCode, createdGroups, joinedGroups);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Ошибка загрузки групп: " + databaseError.getMessage());
            }
        });
    }

    // В методе loadGroup загружаем информацию о участниках группы
    private void loadGroup(String groupCode, List<Group> createdGroups, List<Group> joinedGroups) {
        databaseReference.child("groups").child(groupCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                if (group != null) {
                    group.setGroupCode(groupCode);
                    String creatorId = group.getCreatorId();
                    if (creatorId != null && creatorId.equals(currentUserId)) {
                        createdGroups.add(group);
                    } else if (group.getMembers() != null && group.getMembers().containsKey(currentUserId)) {
                        joinedGroups.add(group);
                    }
                    updateGroupsList(createdGroups, joinedGroups);
                } else {
                    showToast("Группа не найдена для кода: " + groupCode);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Ошибка загрузки группы: " + databaseError.getMessage());
            }
        });
    }

    // Обновление списка групп
    private void updateGroupsList(List<Group> createdGroups, List<Group> joinedGroups) {
        GroupAdapter createdGroupsAdapter = new GroupAdapter(ShareTask.this, createdGroups);
        listViewCreatedGroups.setAdapter(createdGroupsAdapter);

        listViewCreatedGroups.setOnItemClickListener((parent, view, position, id) -> {
            Group selectedGroup = createdGroups.get(position);
            handleGroupClick(selectedGroup);
        });

        GroupAdapter joinedGroupsAdapter = new GroupAdapter(ShareTask.this, joinedGroups);
        listViewJoinedGroups.setAdapter(joinedGroupsAdapter);

        listViewJoinedGroups.setOnItemClickListener((parent, view, position, id) -> {
            Group selectedGroup = joinedGroups.get(position);
            handleGroupClick(selectedGroup);
        });
    }

    // Метод для создания новой группы
    private void createGroup(View view) {
        String groupName = editGroupName.getText().toString().trim();

        if (groupName.isEmpty()) {
            showToast("Введите название группы");
            return;
        }

        // Генерация уникального кода группы
        String groupCode = generateUniqueGroupCode();

        // Создание новой группы
        Group newGroup = new Group(groupCode, groupName, currentUserId);
        newGroup.addMember(currentUserId);

        // Сохранение группы в базе данных
        databaseReference.child("groups").child(groupCode).setValue(newGroup)
                .addOnSuccessListener(aVoid -> {
                    showToast("Группа создана с кодом: " + groupCode);
                    // Обновление списка групп после создания новой группы
                    databaseReference.child("users").child(currentUserId).child("groups").child(groupCode).setValue(true);
                })
                .addOnFailureListener(e -> showToast("Ошибка при создании группы: " + e.getMessage()));
    }


    // Метод для присоединения к группе по уникальному коду
    private void joinGroup(View view) {
        String groupCode = editGroupCode.getText().toString().trim();

        if (groupCode.isEmpty()) {
            showToast("Введите код группы");
            return;
        }

        databaseReference.child("groups").child(groupCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    databaseReference.child("groups").child(groupCode).child("members").child(currentUserId).setValue(true)
                            .addOnSuccessListener(aVoid -> {
                                showToast("Успешно присоединились к группе с кодом: " + groupCode);
                                // Обновление списка групп после присоединения к группе
                                databaseReference.child("users").child(currentUserId).child("groups").child(groupCode).setValue(true);
                            })
                            .addOnFailureListener(e -> showToast("Ошибка при присоединении к группе: " + e.getMessage()));
                } else {
                    showToast("Группа с таким кодом не найдена");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Ошибка при присоединении к группе: " + databaseError.getMessage());
            }
        });
    }
    // Метод для создания новой задачи в группе
    private void createTaskForGroup(String groupCode, Task task) {
        DatabaseReference groupTasksRef = databaseReference.child("groups").child(groupCode).child("tasks");
        String taskId = groupTasksRef.push().getKey();
        if (taskId != null) {
            groupTasksRef.child(taskId).setValue(task)
                    .addOnSuccessListener(aVoid -> showToast("Задача создана"))
                    .addOnFailureListener(e -> showToast("Ошибка при создании задачи: " + e.getMessage()));
        }
    }


    // Метод для удаления группы
    private void deleteGroup(String groupId) {
        databaseReference.child("groups").child(groupId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    showToast("Группа успешно удалена");
                    // Обновление списка групп после удаления группы
                    loadGroups();
                })
                .addOnFailureListener(e -> showToast("Ошибка при удалении группы: " + e.getMessage()));
    }

    // Метод для генерации уникального кода группы
    private String generateUniqueGroupCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // Метод для обработки нажатия на элемент списка групп
    private void handleGroupClick(Group group) {
        new AlertDialog.Builder(this)
                .setTitle(group.getGroupName())
                .setPositiveButton("ОК", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Удалить", (dialog, which) -> deleteGroup(group.getGroupCode()))
                .show();
    }

    // Метод для отображения всплывающего сообщения
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
