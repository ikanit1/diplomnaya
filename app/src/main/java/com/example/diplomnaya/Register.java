package com.example.diplomnaya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class Register extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonRegister;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Инициализация FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Получение ссылок на элементы пользовательского интерфейса
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Установка слушателя для кнопки регистрации
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Регистрация нового пользователя
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Регистрация успешна
                        Toast.makeText(Register.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Register.this, WorkSpace.class));
                        finish();
                    } else {
                        // Регистрация не удалась
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            // Пользователь с такой электронной почтой уже зарегистрирован
                            Toast.makeText(Register.this, "Пользователь с такой электронной почтой уже зарегистрирован", Toast.LENGTH_SHORT).show();
                        } else {
                            // Общая ошибка регистрации
                            Toast.makeText(Register.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
