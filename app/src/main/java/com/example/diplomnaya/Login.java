package com.example.diplomnaya;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class Login extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegister;
    private TextView textViewRegister;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Инициализация FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Получение ссылок на элементы пользовательского интерфейса
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewRegister = findViewById(R.id.textViewRegister);

        // Проверяем, вошел ли уже пользователь
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Пользователь уже вошел в систему, перенаправляем его на нужный экран
            redirectUser();
            finish(); // Завершаем текущую активность
            return;
        }

        // Установка слушателей для кнопок
        buttonLogin.setOnClickListener(v -> loginUser());
        buttonRegister.setOnClickListener(v -> startActivity(new Intent(Login.this, Register.class)));
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Проверка на пустые поля
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(Login.this, "Введите электронную почту и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        // Вход пользователя
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Вход успешен
                        Toast.makeText(Login.this, "Вход успешен", Toast.LENGTH_SHORT).show();
                        redirectUser();
                        finish(); // Завершаем текущую активность
                    } else {
                        // Вход не удался
                        handleLoginError(task.getException());
                    }
                });
    }

    private void redirectUser() {
        // Получаем настройки пользователя из SharedPreferences или из Firebase, если необходимо
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        boolean goToOwnRoom = sharedPreferences.getBoolean("goToOwnRoom", false);

        Intent intent;
        if (goToOwnRoom) {
            intent = new Intent(Login.this, OwnRoom.class);
        } else {
            intent = new Intent(Login.this, WorkSpace.class);
        }

        // Запускаем соответствующую активность
        startActivity(intent);
    }

    private void handleLoginError(Exception exception) {
        // Вход не удался, обработайте ошибку
        if (exception instanceof FirebaseAuthInvalidUserException) {
            // Пользователь с такой электронной почтой не существует
            Toast.makeText(Login.this, "Пользователь с такой электронной почтой не существует", Toast.LENGTH_SHORT).show();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            // Неправильный пароль
            Toast.makeText(Login.this, "Неправильный пароль", Toast.LENGTH_SHORT).show();
        } else {
            // Общая ошибка входа
            Toast.makeText(Login.this, "Ошибка входа. Проверьте правильность данных", Toast.LENGTH_SHORT).show();
        }
    }

}

