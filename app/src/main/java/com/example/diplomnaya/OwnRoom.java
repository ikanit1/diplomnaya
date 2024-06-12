package com.example.diplomnaya;

import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.squareup.picasso.Picasso;

public class OwnRoom extends AppCompatActivity {

    private TextView textViewUserEmail;
    private TextView textViewUserName;
    private ImageView imageViewUserProfile;
    private Button buttonLogout;
    private Button buttonDeleteAccount;
    private Button buttonEditProfile;
    private Button buttonChangePassword;
    private Button buttonShareApp;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.own_room);

        mAuth = FirebaseAuth.getInstance();

        textViewUserEmail = findViewById(R.id.textViewUserEmail);
        textViewUserName = findViewById(R.id.textViewUsername);
        imageViewUserProfile = findViewById(R.id.imageViewUserProfile);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonShareApp = findViewById(R.id.buttonShareApp);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            textViewUserEmail.setText(userEmail);

            String userName = user.getDisplayName();
            textViewUserName.setText(userName);

            for (UserInfo profile : user.getProviderData()) {
                if (profile.getProviderId().equals("google.com")) {
                    String photoUrl = profile.getPhotoUrl().toString();
                    Picasso.get().load(photoUrl).into(imageViewUserProfile);
                }
            }
        }

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(OwnRoom.this, Login.class));
                finish();
            }
        });

        buttonDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });

        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OwnRoom.this, ChangePasswordActivity.class));
            }
        });

        buttonShareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareAppLink();
            }
        });
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String providerId = user.getProviderId();
            if (providerId.equals("google.com")) {
                mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            deleteFirebaseAccount(user);
                        } else {
                            Toast.makeText(OwnRoom.this, "Ошибка при выходе из учетной записи Google", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                deleteFirebaseAccount(user);
            }
        }
    }

    private void deleteFirebaseAccount(FirebaseUser user) {
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(OwnRoom.this, Login.class));
                            finish();
                        } else {
                            Toast.makeText(OwnRoom.this, "Ошибка при удалении аккаунта", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void shareAppLink() {
        String appUrl = "https://diplomnaya-e24de.web.app";
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Привет! приглашаю тебя попробовать приложение для заметок в группах. Скачай приложение тут -> " + appUrl);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            Picasso.get().load(imageUri).into(imageViewUserProfile);
            updateProfilePhoto(imageUri);
        }
    }

    private void updateProfilePhoto(Uri photoUri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(photoUri)
                    .build();
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(OwnRoom.this, "Фото профиля обновлено", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(OwnRoom.this, "Ошибка при обновлении фото профиля", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
