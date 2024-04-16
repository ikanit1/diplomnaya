package com.example.diplomnaya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class OwnRoom extends AppCompatActivity {

    private TextView textViewUserEmail;
    private Button buttonLogout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.own_room);

        // Инициализация FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Получение ссылок на элементы пользовательского интерфейса
        textViewUserEmail = findViewById(R.id.textViewUserEmail);
        buttonLogout = findViewById(R.id.buttonLogout);

        // Установка адреса электронной почты пользователя
        String userEmail = mAuth.getCurrentUser().getEmail();
        textViewUserEmail.setText(userEmail);

        // Настройка слушателя для кнопки выхода из аккаунта
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Выход из аккаунта
                mAuth.signOut();
                // Переход на экран входа в систему
                startActivity(new Intent(OwnRoom.this, Login.class));
                finish(); // Закрыть текущую активность
            }
        });
    }
}
