package com.example.diplomnaya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Register extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextName;
    private Button buttonRegister, buttonGoogleSignIn;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Инициализация FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Получение ссылок на элементы пользовательского интерфейса
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextName = findViewById(R.id.editTextName);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn);

        // Установка слушателя для кнопки регистрации
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Настройка Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("428014765321-9e319i0u0t0li7n28b4p03eglqa5nfpj.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Установка слушателя для кнопки входа через Google
        buttonGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String name = editTextName.getText().toString().trim();

        // Проверяем, что поля не пустые
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(Register.this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Регистрация нового пользователя
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Регистрация успешна
                            Toast.makeText(Register.this, "Регистрация успешна. Пожалуйста, проверьте свою почту для подтверждения", Toast.LENGTH_SHORT).show();
                            // Сохраняем имя пользователя в Firebase
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                                user.updateProfile(profileUpdate)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (!task.isSuccessful()) {
                                                    // Ошибка обновления имени пользователя
                                                    Toast.makeText(Register.this, "Ошибка обновления имени пользователя", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                sendEmailVerification(); // Отправка письма с подтверждением
                            }
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
                    }
                });
    }

    private void sendEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Письмо с подтверждением успешно отправлено
                                Toast.makeText(Register.this, "Пожалуйста, проверьте свою почту для подтверждения", Toast.LENGTH_SHORT).show();
                                // Перенаправляем пользователя на экран входа
                                startActivity(new Intent(Register.this, Login.class));
                                finish(); // Завершаем текущую активность
                            } else {
                                // Ошибка отправки письма с подтверждением
                                Toast.makeText(Register.this, "Ошибка отправки письма с подтверждением", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Обработка результата аутентификации с помощью Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In успешен, аутентифицируем с Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In не удался, обновляем UI
                Toast.makeText(Register.this, "Google Sign In не удался", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Вход успешен
                            Toast.makeText(Register.this, "Вход через Google успешен", Toast.LENGTH_SHORT).show();
                            String displayName = account.getDisplayName();
                            // Используем отображаемое имя Google аккаунта, если доступно
                            if (displayName != null && !displayName.isEmpty()) {
                                editTextName.setText(displayName);
                            }
                            startActivity(new Intent(Register.this, WorkSpace.class));
                            finish(); // Завершаем текущую активность
                        } else {
                            // Если вход не удался, отображаем сообщение пользователю.
                            Toast.makeText(Register.this, "Ошибка входа через Google", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
