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

public class MainActivity extends AppCompatActivity {

    private TextView dateTextView;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Настройка вашего пользовательского интерфейса, инициализация переменных и т.д.

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
    }
}
