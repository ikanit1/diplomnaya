package com.example.diplomnaya;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ShareTask extends AppCompatActivity {
    private EditText editGroupName;
    private EditText editGroupDescription;
    private EditText editEmails;
    private Button btnCreateGroup;
    private ListView listViewInvitations;

    private DatabaseReference database;
    private String currentUserId; // Идентификатор текущего пользователя (нужно получить из контекста или другого класса)

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

        // Инициализация ссылки на базу данных Firebase
        database = FirebaseDatabase.getInstance().getReference();

        // Установка обработчика для кнопки создания группы
        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });

        // Загрузка приглашений для текущего пользователя
        loadInvitations();
    }

    // Метод для создания группы
    private void createGroup() {
        String groupName = editGroupName.getText().toString().trim();
        String groupDescription = editGroupDescription.getText().toString().trim();
        String[] emails = editEmails.getText().toString().split(",");

        if (groupName.isEmpty()) {
            Toast.makeText(this, "Введите название группы", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создание новой записи о группе
        String groupId = database.child("groups").push().getKey();
        Group newGroup = new Group(groupId, groupName, groupDescription);

        // Добавление текущего пользователя в список участников
        newGroup.addMember(currentUserId);

        // Добавление приглашений
        List<String> emailList = new ArrayList<>();
        for (String email : emails) {
            email = email.trim();
            if (!email.isEmpty()) {
                emailList.add(email);
                newGroup.addInvitation(email);
            }
        }

        // Сохранение группы в базе данных
        database.child("groups").child(groupId).setValue(newGroup);

        // Отправка уведомлений приглашенным пользователям (необходимо реализовать отправку уведомлений)

        Toast.makeText(this, "Группа создана", Toast.LENGTH_SHORT).show();
        finish(); // Закрытие активности
    }

    // Метод для загрузки приглашений для текущего пользователя
    private void loadInvitations() {
        // Здесь вы должны загрузить список приглашений из базы данных
        // Для текущего пользователя и отобразить их в listViewInvitations

        // После загрузки приглашений установите обработчик нажатия на элементы списка
        // listViewInvitations.setOnItemClickListener((adapterView, view, position, id) -> {
        //     Invitation invitation = (Invitation) adapterView.getItemAtPosition(position);
        //     // Обработка принятия или отклонения приглашения
        // });
    }
}
