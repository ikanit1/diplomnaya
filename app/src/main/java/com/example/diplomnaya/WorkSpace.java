package com.example.diplomnaya;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.Calendar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.content.Intent;
import android.net.Uri;

public class WorkSpace extends AppCompatActivity {

    private LinearLayout tasksLayout;
    private TaskDatabaseHelper dbHelper;

    private Task task;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workspace_main);

        tasksLayout = findViewById(R.id.tasks_layout);
        dbHelper = new TaskDatabaseHelper(this);

        loadTasks();

        findViewById(R.id.button_add_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            // Обработка полученного изображения, например, сохранение его пути в базе данных
        }
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить задачу");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        final EditText editTextTask = dialogView.findViewById(R.id.editTextTask);
        final TextView textViewDateTime = dialogView.findViewById(R.id.textViewDateTime);
        ImageButton buttonPickDateTime = dialogView.findViewById(R.id.buttonPickDateTime);

        // Установка текста задачи, если он есть
        if (task != null) {
            editTextTask.setText(task.getText());
        }

        builder.setView(dialogView);

        // Обработчик для выбора даты и времени
        buttonPickDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                DatePickerDialog datePickerDialog = new DatePickerDialog(WorkSpace.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(WorkSpace.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                String dateTime = sdf.format(calendar.getTime());
                                textViewDateTime.setText(dateTime);
                            }
                        }, hour, minute, true);
                        timePickerDialog.show();
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        builder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String taskText = editTextTask.getText().toString().trim();
                String dateTime = textViewDateTime.getText().toString().trim();
                if (!taskText.isEmpty() && !dateTime.isEmpty()) {
                    Task task = new Task();
                    task.setText(taskText);
                    String[] parts = dateTime.split(" ");
                    task.setDateCreated(parts[0]);

                    // Проверяем, было ли выбрано время
                    if (parts.length > 1) {
                        task.setTimeCreated(parts[1]);
                    } else {
                        // Время не было указано, устанавливаем пустое значение
                        task.setTimeCreated("");
                    }

                    dbHelper.addTask(task);
                    addTaskToLayout(task);
                } else {
                    Toast.makeText(WorkSpace.this, "Пожалуйста, введите задачу и выберите дату и время", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();

        ImageButton buttonPickImage = dialogView.findViewById(R.id.buttonPickImage);
        buttonPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickImageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickImageIntent, PICK_IMAGE_REQUEST);
            }
        });
    }


    private void updateTaskView(View taskView, Task task) {
        TextView taskTextView = taskView.findViewById(R.id.task_text);
        taskTextView.setText(task.getText());

        TextView taskDateTimeView = taskView.findViewById(R.id.task_date_time);
        String dateTime = task.getDateCreated() + " " + task.getTimeCreated();
        taskDateTimeView.setText(dateTime);
    }

    private void addTaskToLayout(Task task) {
        LayoutInflater inflater = getLayoutInflater();
        final View taskView = inflater.inflate(R.layout.task_item, null);

        TextView taskTextView = taskView.findViewById(R.id.task_text);
        taskTextView.setText(task.getText());

        TextView taskDateTimeView = taskView.findViewById(R.id.task_date_time);
        String dateTime = task.getDateCreated() + " " + task.getTimeCreated();
        taskDateTimeView.setText(dateTime);

        ImageButton deleteButton = taskView.findViewById(R.id.button_delete_task);

        ImageButton editButton = taskView.findViewById(R.id.button_edit_task);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditTaskDialog(taskView, task);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(WorkSpace.this);
                deleteDialogBuilder.setTitle("Удалить задачу");
                deleteDialogBuilder.setMessage("Вы уверены, что хотите удалить эту задачу?");
                deleteDialogBuilder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteTask(task);
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

    private void loadTasks() {
        List<Task> tasks = dbHelper.getAllTasks();
        for (Task task : tasks) {
            addTaskToLayout(task);
        }
    }

    private void showEditTaskDialog(View taskView, Task task) {
        AlertDialog.Builder editDialogBuilder = new AlertDialog.Builder(WorkSpace.this);
        editDialogBuilder.setTitle("Редактировать задачу");

        LayoutInflater inflater = getLayoutInflater();
        View editDialogView = inflater.inflate(R.layout.dialog_edit_task, null);
        final EditText editTextTask = editDialogView.findViewById(R.id.editTextTask);
        final TextView textViewDateTime = editDialogView.findViewById(R.id.textViewDateTime);
        ImageButton buttonPickDateTime = editDialogView.findViewById(R.id.buttonPickDateTime);

        // Устанавливаем текущий текст и время задачи в поля диалогового окна
        editTextTask.setText(task.getText());
        textViewDateTime.setText(task.getDateCreated() + " " + task.getTimeCreated());

        editDialogBuilder.setView(editDialogView);

        buttonPickDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                DatePickerDialog datePickerDialog = new DatePickerDialog(WorkSpace.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(WorkSpace.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                String dateTime = sdf.format(calendar.getTime());
                                textViewDateTime.setText(dateTime);
                            }
                        }, hour, minute, true);
                        timePickerDialog.show();
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        editDialogBuilder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String editedTaskText = editTextTask.getText().toString().trim();
                String editedDateTime = textViewDateTime.getText().toString().trim();

                if (!editedTaskText.isEmpty() && !editedDateTime.isEmpty()) {
                    task.setText(editedTaskText);
                    String[] parts = editedDateTime.split(" ");
                    task.setDateCreated(parts[0]);
                    task.setTimeCreated(parts[1]);
                    dbHelper.updateTask(task);
                    updateTaskView(taskView, task);
                } else {
                    Toast.makeText(WorkSpace.this, "Пожалуйста, введите задачу и выберите дату и время", Toast.LENGTH_SHORT).show();
                }
            }
        });

        editDialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        editDialogBuilder.show();
    }

}
