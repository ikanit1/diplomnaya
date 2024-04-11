package com.example.diplomnaya;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.content.SharedPreferences;



public class MainActivity extends AppCompatActivity {

    private Button settingsButton;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);

        // Проверяем, была ли страница приветствия уже показана
        if (sharedPreferences.getBoolean("isWelcomeShown", false)) {
            // Если да, то переходим сразу на страницу workspace_main
            startNextActivity(null);
        } else {
            // Если нет, то показываем страницу приветствия
            setContentView(R.layout.activity_main);

        }
    }

    // Метод для перехода на следующую активити
    public void startNextActivity(View view) {
        // Создаем Intent для перехода на следующую активити
        Intent intent = new Intent(this, WorkSpace.class);
        // Запускаем следующую активити
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        // Завершаем текущую активити
        finish();

        // Сохраняем информацию о том, что страница приветствия была показана
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isWelcomeShown", true);
        editor.apply();
    }
}
