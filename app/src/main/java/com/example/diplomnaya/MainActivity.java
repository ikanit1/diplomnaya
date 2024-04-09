package com.example.diplomnaya;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    private Button settingsButton;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsButton = findViewById(R.id.button_settings);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingsActivity(); // Вызываем метод для открытия экрана настроек
            }
        });
    }

    public void openSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class); // Создаем Intent для перехода на экран настроек
        startActivity(intent); // Запускаем экран настроек
    }
        // Настройка вашего пользовательского интерфейса, инициализация переменных и т.д.




    // Метод для перехода на следующую активити
    public void startNextActivity(View view) {
        // Создаем Intent для перехода на следующую активити
        Intent intent = new Intent(this, WorkSpace.class);
        // Запускаем следующую активити
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        // Завершаем текущую активити
        finish();
    }
}
