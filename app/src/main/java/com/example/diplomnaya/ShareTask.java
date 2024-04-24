package com.example.diplomnaya;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import android.util.Log;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class ShareTask extends AppCompatActivity {

    // Поля активности
    private EditText editGroupName, editGroupDescription, editEmails;
    private Button btnCreateGroup;
    private ListView listViewInvitations, listViewGroups;

    private DatabaseReference databaseReference;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_task);

        // Инициализация компонентов
        editGroupName = findViewById(R.id.editGroupName);
        editGroupDescription = findViewById(R.id.editGroupDescription);
        editEmails = findViewById(R.id.editEmails);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        listViewInvitations = findViewById(R.id.listViewInvitations);
        listViewGroups = findViewById(R.id.listViewGroups);

        // Инициализация базы данных
        databaseReference = FirebaseDatabase.getInstance().getReference();


        // Получение текущего пользователя
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            // Загрузка данных (приглашений и групп)
            loadInvitations();
            loadGroups();
        } else {
            Toast.makeText(this, "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
            finish(); // Закрытие активности
        }

        // Установка обработчиков событий
        btnCreateGroup.setOnClickListener(this::createGroup);
    }
    // Добавьте этот метод для отправки уведомлений с использованием FCM
    private String convertEmailToPath(String email) {
        // Замените запрещенные символы в адресе электронной почты на безопасные символы
        return email.replace(".", "_").replace("#", "_").replace("$", "_").replace("[", "_").replace("]", "_");
    }


    // Метод для создания новой группы
    private void createGroup(View view) {
        // Получите ввод данных из EditText
        String groupName = editGroupName.getText().toString().trim();
        String groupDescription = editGroupDescription.getText().toString().trim();
        String[] emails = editEmails.getText().toString().split(",");

        if (groupName.isEmpty()) {
            Toast.makeText(this, "Введите название группы", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создайте новую группу
        String groupId = databaseReference.child("groups").push().getKey();
        Group newGroup = new Group(groupId, groupName, groupDescription);

        // Добавьте текущего пользователя в группу как участника
        newGroup.addMember(currentUserId);

        // Добавьте приглашения для других пользователей
        for (String email : emails) {
            email = email.trim();
            if (!email.isEmpty()) {
                // Отправьте приглашение
                sendInvitation(email, groupId);
            }
        }

        // Сохраните группу в Firebase
        databaseReference.child("groups").child(groupId).setValue(newGroup)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Группа создана", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при создании группы: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendInvitation(String email, String groupId) {
        // Преобразуйте адрес электронной почты в безопасный путь
        String safeEmail = convertEmailToPath(email);
        Log.d("sendInvitation", "Safe email: " + safeEmail);

        // Создайте приглашение для пользователя
        Invitation invitation = new Invitation(groupId, currentUserId, email);

        // Добавьте приглашение в Firebase
        databaseReference.child("invitations").child(safeEmail).push().setValue(invitation)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Приглашение отправлено", Toast.LENGTH_SHORT).show();
                    Log.d("sendInvitation", "Приглашение успешно отправлено: " + invitation.toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка при отправке приглашения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("sendInvitation", "Ошибка при отправке приглашения: " + e.getMessage());
                });
    }




    // Метод для загрузки приглашений
    private void loadInvitations() {
        DatabaseReference invitationsRef = databaseReference.child("invitations").child(currentUserId);

        invitationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Invitation> invitations = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Invitation invitation = snapshot.getValue(Invitation.class);
                    if (invitation != null) {
                        invitations.add(invitation);
                        Log.d("loadInvitations", "Invitation loaded: " + invitation.getGroupId());
                    } else {
                        Log.e("loadInvitations", "Error loading invitation from snapshot: " + snapshot.getKey());
                    }
                }
                InvitationAdapter adapter = new InvitationAdapter(ShareTask.this, invitations);
                listViewInvitations.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ShareTask.this, "Ошибка загрузки приглашений: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Метод для обработки нажатия на приглашение
    private void handleInvitationClick(Invitation invitation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ShareTask.this);
        builder.setTitle("Приглашение в группу")
                .setMessage("Вы были приглашены в группу: " + invitation.getGroupId() + "\n" +
                        "Отправитель: " + invitation.getEmail() + "\n" +
                        "Принять приглашение?")
                .setPositiveButton("Принять", (dialog, which) -> acceptInvitation(invitation))
                .setNegativeButton("Отклонить", (dialog, which) -> declineInvitation(invitation))
                .create()
                .show();
    }

    // Метод для принятия приглашения
    private void acceptInvitation(Invitation invitation) {
        // Обновите статус приглашения на "accepted"
        DatabaseReference invitationRef = databaseReference.child("invitations").child(currentUserId).child(invitation.getId());
        invitationRef.child("status").setValue("accepted");

        // Добавьте пользователя в список участников группы
        DatabaseReference groupMembersRef = databaseReference.child("groups").child(invitation.getGroupId()).child("members");
        groupMembersRef.child(currentUserId).setValue(true);

        Toast.makeText(this, "Приглашение принято", Toast.LENGTH_SHORT).show();
    }

    private void addNoteToGroup(String groupId, String title, String text) {
        String noteId = databaseReference.child("groups").child(groupId).child("notes").push().getKey();
        Task newNote = new Task();
        newNote.setTitle(title);
        newNote.setText(text);
        newNote.setCreator(currentUserId);
        newNote.setCreatedAt(System.currentTimeMillis());

        // Добавьте заметку в Firebase
        databaseReference.child("groups").child(groupId).child("notes").child(noteId).setValue(newNote)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Заметка добавлена", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при добавлении заметки: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }



    // Метод для отклонения приглашения
    private void declineInvitation(Invitation invitation) {
        DatabaseReference invitationRef = databaseReference.child("invitations").child(currentUserId).child(invitation.getId());
        invitationRef.removeValue();
        Toast.makeText(this, "Приглашение отклонено", Toast.LENGTH_SHORT).show();
    }

    // Метод для загрузки групп
    private void loadGroups() {
        DatabaseReference groupsRef = databaseReference.child("groups");

        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Group> groups = new ArrayList<>();

                // Получение списка групп, в которых участвует текущий пользователь
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Group group = snapshot.getValue(Group.class);

                    if (group != null && group.getMembers().contains(currentUserId)) {
                        groups.add(group);
                    }
                }
                // Установка адаптера для списка групп
                GroupAdapter adapter = new GroupAdapter(ShareTask.this, groups);
                listViewGroups.setAdapter(adapter);

                // Обработка нажатий на элементы списка групп
                listViewGroups.setOnItemClickListener((parent, view, position, id) -> {
                    Group selectedGroup = groups.get(position);
                    handleGroupClick(selectedGroup);
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ShareTask.this, "Ошибка загрузки групп: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();



            }
        });
    }

    // Метод для обработки нажатия на группу
    private void handleGroupClick(Group group) {
        // Подтверждение удаления группы
        new AlertDialog.Builder(ShareTask.this)
                .setTitle("Удаление группы")
                .setMessage("Вы уверены, что хотите удалить группу: " + group.getGroupName() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteGroup(group.getGroupId()))
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    // Метод для удаления группы
    private void deleteGroup(String groupId) {
        DatabaseReference groupRef = databaseReference.child("groups").child(groupId);

        groupRef.removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Группа успешно удалена", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при удалении группы: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}
