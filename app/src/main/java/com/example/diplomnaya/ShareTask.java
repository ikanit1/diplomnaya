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

    private EditText editGroupName, editGroupDescription, editGroupCode;
    private Button btnCreateGroup, btnJoinGroup;
    private ListView listViewGroups;

    private DatabaseReference databaseReference;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_task);

        // Инициализация компонентов
        editGroupName = findViewById(R.id.editGroupName);
        editGroupDescription = findViewById(R.id.editGroupDescription);
        editGroupCode = findViewById(R.id.editGroupCode);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        btnJoinGroup = findViewById(R.id.btnJoinGroup);
        listViewGroups = findViewById(R.id.listViewGroups);

        // Инициализация базы данных
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Получение текущего пользователя
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            loadGroups(); // Загрузка групп пользователя
        } else {
            Toast.makeText(this, "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
            finish(); // Закрытие активности
        }

        // Установка обработчиков событий
        btnCreateGroup.setOnClickListener(this::createGroup);
        btnJoinGroup.setOnClickListener(this::joinGroup);
    }

    // Метод для генерации уникального кода группы
    private String generateUniqueGroupCode() {
        return UUID.randomUUID().toString().substring(0, 8); // Короткий UUID
    }

    // Метод для создания новой группы
    private void createGroup(View view) {
        String groupName = editGroupName.getText().toString().trim();
        String groupDescription = editGroupDescription.getText().toString().trim();

        if (groupName.isEmpty()) {
            showToast("Введите название группы");
            return;
        }

        // Генерация уникального кода группы
        String groupCode = generateUniqueGroupCode();

        // Создание новой группы
        Group newGroup = new Group(groupCode, groupName, groupDescription);
        newGroup.addMember(currentUserId);

        // Сохранение группы в базе данных
        databaseReference.child("groups").child(groupCode).setValue(newGroup)
                .addOnSuccessListener(aVoid -> showToast("Группа создана с кодом: " + groupCode))
                .addOnFailureListener(e -> showToast("Ошибка при создании группы: " + e.getMessage()));
    }

    // Метод для присоединения к группе по уникальному коду
    private void joinGroup(View view) {
        String groupCode = editGroupCode.getText().toString().trim();

        if (groupCode.isEmpty()) {
            showToast("Введите код группы");
            return;
        }

        // Добавляем текущего пользователя в члены группы
        databaseReference.child("groups").child(groupCode).child("members").child(currentUserId).setValue(true)
                .addOnSuccessListener(aVoid -> showToast("Успешно присоединились к группе с кодом: " + groupCode))
                .addOnFailureListener(e -> showToast("Ошибка при присоединении к группе: " + e.getMessage()));
    }

    // Метод для загрузки групп пользователя
    private void loadGroups() {
        DatabaseReference userGroupsRef = databaseReference.child("users").child(currentUserId).child("groups");

        userGroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Group> groups = new ArrayList<>();

                // Получение списка групп, в которых состоит пользователь
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String groupCode = snapshot.getKey();
                    loadGroup(groupCode, groups);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Ошибка загрузки групп: " + databaseError.getMessage());
            }
        });
    }

    // Загрузка данных группы
    private void loadGroup(String groupCode, List<Group> groups) {
        databaseReference.child("groups").child(groupCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                if (group != null) {
                    groups.add(group);
                    updateGroupsList(groups);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Ошибка загрузки группы: " + databaseError.getMessage());
            }
        });
    }

    // Обновление списка групп
    private void updateGroupsList(List<Group> groups) {
        GroupAdapter adapter = new GroupAdapter(ShareTask.this, groups);
        listViewGroups.setAdapter(adapter);
        listViewGroups.setOnItemClickListener((parent, view, position, id) -> {
            Group selectedGroup = groups.get(position);
            handleGroupClick(selectedGroup);
        });
    }

    // Метод для обработки нажатия на группу
    private void handleGroupClick(Group group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Удаление группы")
                .setMessage("Вы уверены, что хотите удалить группу: " + group.getGroupName() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteGroup(group.getGroupId()))
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    // Метод для удаления группы
    private void deleteGroup(String groupId) {
        databaseReference.child("groups").child(groupId).removeValue()
                .addOnSuccessListener(aVoid -> showToast("Группа успешно удалена"))
                .addOnFailureListener(e -> showToast("Ошибка при удалении группы: " + e.getMessage()));
    }

    // Метод для вывода сообщения
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
