package com.example.diplomnaya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.squareup.picasso.Picasso;

public class OwnRoom extends AppCompatActivity {

    private TextView textViewUserEmail;
    private ImageView imageViewUserProfile;
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
        imageViewUserProfile = findViewById(R.id.imageViewUserProfile);
        buttonLogout = findViewById(R.id.buttonLogout);

        // Получение текущего пользователя
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Получение адреса электронной почты пользователя
            String userEmail = user.getEmail();
            textViewUserEmail.setText(userEmail);

            // Получение информации о провайдере аутентификации
            for (UserInfo profile : user.getProviderData()) {
                // Если пользователь вошел через Google
                if (profile.getProviderId().equals("google.com")) {
                    // Получение URL фото профиля Google
                    String photoUrl = profile.getPhotoUrl().toString();
                    // Загрузка фото профиля с использованием Picasso (или любой другой библиотеки)
                    Picasso.get().load(photoUrl).into(imageViewUserProfile);
                }
            }
        }

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
