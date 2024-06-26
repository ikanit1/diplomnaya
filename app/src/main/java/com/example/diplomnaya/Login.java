package com.example.diplomnaya;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegister, buttonGoogleSignIn;
    private TextView textViewRegister, textViewForgotPassword;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

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
        buttonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn);
        textViewRegister = findViewById(R.id.textViewRegister);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);

        // Проверяем, вошел ли уже пользователь
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            // Пользователь уже вошел в систему и его почта подтверждена, перенаправляем его на нужный экран
            redirectUser();
            finish(); // Завершаем текущую активность
            return;
        }

        // Установка слушателей для кнопок
        buttonLogin.setOnClickListener(v -> loginUser());
        buttonRegister.setOnClickListener(v -> startActivity(new Intent(Login.this, Register.class)));
        buttonGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        textViewForgotPassword.setOnClickListener(v -> resetPassword());

        // Настройка Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("428014765321-9e319i0u0t0li7n28b4p03eglqa5nfpj.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
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
                        // Вход успешен, проверяем подтверждена ли почта
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(Login.this, "Вход успешен", Toast.LENGTH_SHORT).show();
                            getTokenAndRedirect();
                            finish(); // Завершаем текущую активность
                        } else {
                            Toast.makeText(Login.this, "Пожалуйста, подтвердите свою почту", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Вход не удался
                        handleLoginError(task.getException());
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Результат, возвращаемый запуском Intent из GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In успешен, аутентифицируем с Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In не удался, обновляем UI
                Toast.makeText(Login.this, "Google Sign In не удался", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Вход успешен
                        Toast.makeText(Login.this, "Вход через Google успешен", Toast.LENGTH_SHORT).show();
                        getTokenAndRedirect();
                        finish(); // Завершаем текущую активность
                    } else {
                        // Если вход не удался, отображаем сообщение пользователю.
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

    private void getTokenAndRedirect() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    // Save token to Firebase Realtime Database
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("notificationToken");
                        tokenRef.setValue(token);
                    }



                    // Redirect user after saving the token
                    redirectUser();
                });
    }


    private void resetPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите Электронную Почту");

        // Установка EditText для ввода электронной почты
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        // Установка кнопок "Отправить" и "Отмена"
        builder.setPositiveButton("Отправить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = input.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(Login.this, "Введите электронную почту для восстановления пароля", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(Login.this, "Инструкция по восстановлению пароля отправлена на вашу почту", Toast.LENGTH_SHORT).show();
                            } else {
                                if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                    Toast.makeText(Login.this, "Пользователь с такой электронной почтой не существует", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Login.this, "Ошибка при отправке инструкции по восстановлению пароля", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

}
