package com.example.diplomnaya;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Button themeChangeButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_settings);

        // Находим кнопку смены темы по идентификатору
        themeChangeButton = findViewById(R.id.themeChangeButton);

        // Получаем доступ к файлу настроек
        sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);

        // Устанавливаем слушатель кликов для кнопки
        themeChangeButton.setOnClickListener(view -> {
            // Получаем текущую тему из настроек
            boolean isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false);

            // Инвертируем текущую тему
            isDarkTheme = !isDarkTheme;

            // Сохраняем новую тему в настройках
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isDarkTheme", isDarkTheme);
            editor.apply();

            // Перезапускаем активити для применения новой темы
            recreate();
        });
    }
}
