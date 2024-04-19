package com.example.diplomnaya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.content.SharedPreferences;



public class MainActivity extends AppCompatActivity {

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

            // Найдите кнопку "Приступить" в макете
            Button startButton = findViewById(R.id.button_start); // Убедитесь, что `R.id.start_button` — это идентификатор вашей кнопки в макете

            // Установите обработчик события для кнопки "Приступить"
            startButton.setOnClickListener(v -> {
                // Создайте Intent для перехода на активность `Login`
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);

                // Завершаем текущую активность
                finish();
            });
        }
    }

    // Метод для перехода на следующую активность
    public void startNextActivity(View view) {
        // Создаём Intent для перехода на следующую активность
        Intent intent = new Intent(this, WorkSpace.class);
        // Запускаем следующую активность
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        // Завершаем текущую активность
        finish();

        // Сохраняем информацию о том, что страница приветствия была показана
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isWelcomeShown", true);
        editor.apply();
    }
}
