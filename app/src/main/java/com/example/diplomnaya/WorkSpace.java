package com.example.diplomnaya;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ImageView;


import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class WorkSpace extends AppCompatActivity {

    private LinearLayout tasksLayout;
    private TaskDatabaseHelper dbHelper;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workspace_main);

        tasksLayout = findViewById(R.id.tasks_layout);
        dbHelper = new TaskDatabaseHelper(this);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout); // Инициализируйте SwipeRefreshLayout

        loadTasks();

        findViewById(R.id.button_add_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task = new Task();
                showAddTaskDialog();
            }
        });

        // Установите слушатель для обработки события обновления при свайпе вниз
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTasks(); // Вызовите метод загрузки задач при обновлении
                swipeRefreshLayout.setRefreshing(false); // Скрыть индикатор обновления
            }
        });
    }

    private void showAddTaskDialog() {
        task = new Task();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить задачу");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        final EditText editTextTask = dialogView.findViewById(R.id.editTextTask);
        final TextView textViewDateTime = dialogView.findViewById(R.id.textViewDateTime);
        ImageButton buttonPickDateTime = dialogView.findViewById(R.id.buttonPickDateTime);
        Switch switchPriority = dialogView.findViewById(R.id.switchPriority);

        builder.setView(dialogView);

        if (task != null) {
            editTextTask.setText(task.getText());
        }

        final Calendar calendar = Calendar.getInstance();

        buttonPickDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                boolean isImportant = switchPriority.isChecked();

                if (!taskText.isEmpty()) {
                    Task newTask = new Task();
                    newTask.setText(taskText);

                    if (!dateTime.isEmpty()) {
                        String[] parts = dateTime.split(" ");
                        newTask.setDateCreated(parts[0]);

                        if (parts.length > 1) {
                            newTask.setTimeCreated(parts[1]);
                        } else {
                            newTask.setTimeCreated("Время не установлено");
                        }
                    } else {
                        newTask.setDateCreated("Дата");
                        newTask.setTimeCreated("и Время не установлены ");
                    }

                    newTask.setImportant(isImportant);
                    dbHelper.addTask(newTask);
                    addTaskToLayout(newTask);
                } else {
                    Toast.makeText(WorkSpace.this, "Пожалуйста, введите текст задачи", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateTaskView(View taskView, Task task) {
        TextView taskTextView = taskView.findViewById(R.id.task_text);
        taskTextView.setText(task.getText());

        TextView taskDateTimeView = taskView.findViewById(R.id.task_date_time);
        String dateTime = task.getDateCreated() + " " + task.getTimeCreated();
        taskDateTimeView.setText(dateTime);

        ImageView starImageView = taskView.findViewById(R.id.image_star);
        if (starImageView != null) {
            if (task.isImportant()) {
                starImageView.setVisibility(View.VISIBLE);
            } else {
                starImageView.setVisibility(View.GONE);
            }
        }
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

        ImageView starImageView = taskView.findViewById(R.id.image_star);
        if (starImageView != null) {
            if (task.isImportant()) {
                starImageView.setVisibility(View.VISIBLE);
            } else {
                starImageView.setVisibility(View.GONE);
            }
        }

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
            tasksLayout.addView(taskView);
        }




    private void loadTasks() {
        List<Task> tasks = dbHelper.getAllTasks();
        tasksLayout.removeAllViews(); // Очистите макет перед загрузкой задач

        // Сортировка: сначала важные задачи, затем остальные
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return Boolean.compare(o2.isImportant(), o1.isImportant());
            }
        });

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
        Switch switchPriority = editDialogView.findViewById(R.id.switchPriority);

        editTextTask.setText(task.getText());
        textViewDateTime.setText(task.getDateCreated() + " " + task.getTimeCreated());
        switchPriority.setChecked(task.isImportant());

        editDialogBuilder.setView(editDialogView);

        final Calendar calendar = Calendar.getInstance();

        buttonPickDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    task.setImportant(switchPriority.isChecked());
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