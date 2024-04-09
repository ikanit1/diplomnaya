package com.example.diplomnaya;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.Toast;


public class WorkSpace extends AppCompatActivity {

    private LinearLayout tasksLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workspace_main);

        tasksLayout = findViewById(R.id.tasks_layout);

        // Нажатие на кнопку "Добавить задачу"
        findViewById(R.id.button_add_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });
    }

    // Показать диалог для добавления задачи
    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить задачу");

        // Надуть макет для диалога
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        final EditText editTextTask = dialogView.findViewById(R.id.editTextTask);
        builder.setView(dialogView);

        // Настройка кнопок диалога
        builder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String taskText = editTextTask.getText().toString().trim(); // Убираем пробелы в начале и конце строки
                if (!taskText.isEmpty()) { // Проверка на пустое значение
                    addTask(taskText);
                } else {
                    Toast.makeText(WorkSpace.this, "Пожалуйста, введите задачу", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Показать диалог
        builder.show();
    }

    // Добавить задачу на экран

    private void addTask(String taskText) {
        LayoutInflater inflater = getLayoutInflater();
        final View taskView = inflater.inflate(R.layout.task_item, null);

        TextView taskTextView = taskView.findViewById(R.id.task_text);
        taskTextView.setText(taskText);

        // Добавление кнопки удаления
        ImageButton deleteButton = taskView.findViewById(R.id.button_delete_task);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Показать диалог подтверждения перед удалением задачи
                AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(WorkSpace.this);
                deleteDialogBuilder.setTitle("Удалить задачу");
                deleteDialogBuilder.setMessage("Вы уверены, что хотите удалить эту задачу?");
                deleteDialogBuilder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Удаление задачи при подтверждении
                        tasksLayout.removeView(taskView);
                        dialog.dismiss();
                    }
                });
                deleteDialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                deleteDialogBuilder.show();
            }
        });

        tasksLayout.addView(taskView, 0);
    }

}
