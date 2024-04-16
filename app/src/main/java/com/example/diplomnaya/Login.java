package com.example.diplomnaya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

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
            // Пользователь уже вошел в систему, перенаправляем его в личный кабинет
            startActivity(new Intent(Login.this, WorkSpace.class));
            finish(); // Закрываем текущую активити, чтобы пользователь не мог вернуться назад
        }

        // Установка слушателей для кнопок
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Вход пользователя
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Вход успешен
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Дополнительные действия после успешного входа, если нужны
                        Toast.makeText(Login.this, "Вход успешен", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Login.this, WorkSpace.class));
                        finish();
                    } else {
                        // Вход не удался
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            // Пользователь с такой электронной почтой не существует
                            Toast.makeText(Login.this, "Пользователь с такой электронной почтой не существует", Toast.LENGTH_SHORT).show();
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // Неверный пароль
                            Toast.makeText(Login.this, "Неверный пароль", Toast.LENGTH_SHORT).show();
                        } else {
                            // Общая ошибка входа
                            Toast.makeText(Login.this, "Ошибка входа", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
