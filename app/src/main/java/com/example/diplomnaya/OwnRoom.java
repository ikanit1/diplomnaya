package com.example.diplomnaya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.squareup.picasso.Picasso;

public class OwnRoom extends AppCompatActivity {

    private TextView textViewUserEmail;
    private TextView textViewUserName;
    private ImageView imageViewUserProfile;
    private Button buttonLogout;
    private Button buttonDeleteAccount;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.own_room);

        // Инициализация FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Получение ссылок на элементы пользовательского интерфейса
        textViewUserEmail = findViewById(R.id.textViewUserEmail);
        textViewUserName = findViewById(R.id.textViewUsername);
        imageViewUserProfile = findViewById(R.id.imageViewUserProfile);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);

        // Получение текущего пользователя из Firebase
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Получение адреса электронной почты пользователя
            String userEmail = user.getEmail();
            textViewUserEmail.setText(userEmail);

            // Получение отображаемого имени пользователя
            String userName = user.getDisplayName();
            textViewUserName.setText(userName); // Установка имени пользователя в TextView

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

        // Настройка слушателя для кнопки удаления аккаунта
        buttonDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Вызов метода для удаления аккаунта
                deleteAccount();
            }
        });
    }

    private void deleteAccount() {
        // Получение текущего пользователя
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Проверка провайдера аутентификации
            String providerId = user.getProviderId();
            if (providerId.equals("google.com")) {
                // Выход из учетной записи Google
                mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Удаление аккаунта пользователя
                            deleteFirebaseAccount(user);
                        } else {
                            // Ошибка при выходе из учетной записи Google
                            Toast.makeText(OwnRoom.this, "Ошибка при выходе из учетной записи Google", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                // Удаление аккаунта пользователя
                deleteFirebaseAccount(user);
            }
        }
    }

    private void deleteFirebaseAccount(FirebaseUser user) {
        // Удаление аккаунта Firebase
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Успешное удаление аккаунта
                            // Переход на экран входа в систему
                            startActivity(new Intent(OwnRoom.this, Login.class));
                            finish(); // Закрыть текущую активность
                        } else {
                            // Ошибка при удалении аккаунта Firebase
                            Toast.makeText(OwnRoom.this, "Ошибка при удалении аккаунта", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
