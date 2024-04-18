package com.example.diplomnaya;
import android.app.NotificationChannel;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import com.google.firebase.database.DatabaseError;
import android.app.NotificationManager;
import android.os.Build;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.app.AlarmManager;
import java.text.ParseException;
import java.util.Date;



public class WorkSpace extends AppCompatActivity {
    private LinearLayout tasksLayout;
    private TaskDatabaseHelper dbHelper;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workspace_main);
        tasksLayout = findViewById(R.id.tasks_layout);
        dbHelper = new TaskDatabaseHelper(this);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        createNotificationChannel();

        loadTasks();

        ImageButton buttonGoto = findViewById(R.id.button_goto);
        buttonGoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем Intent для перехода на activity_login.xml
                Intent intent = new Intent(WorkSpace.this, Login.class);

                // Запускаем активность
                startActivity(intent);
            }
        });

        findViewById(R.id.button_add_task).setOnClickListener(v -> showAddTaskDialog());

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadTasks();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    "channel_name",
                    getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(getString(R.string.channel_name));
            manager.createNotificationChannel(channel);
        }
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить задачу");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText editTitleTask = dialogView.findViewById(R.id.editTitleTask);
        EditText editTextTask = dialogView.findViewById(R.id.editTextTask);
        TextView textViewDateTime = dialogView.findViewById(R.id.textViewDateTime);
        Switch switchPriority = dialogView.findViewById(R.id.switchPriority);
        Switch switchNotify = dialogView.findViewById(R.id.switch_notify);
        RadioGroup radioGroupTaskType = dialogView.findViewById(R.id.radioGroupTaskType);
        RadioButton radioButtonOneTime = dialogView.findViewById(R.id.radioButtonOneTime);
        RadioButton radioButtonRepeating = dialogView.findViewById(R.id.radioButtonRepeating);
        ImageButton buttonPickDateTime = dialogView.findViewById(R.id.buttonPickDateTime);

        Task newTask = new Task();
        radioGroupTaskType.setOnCheckedChangeListener((group, checkedId) -> {
            newTask.setRepeating(checkedId == R.id.radioButtonRepeating);
        });

        buttonPickDateTime.setOnClickListener(v -> showDateTimePickerDialog(textViewDateTime, newTask));

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String taskText = editTextTask.getText().toString().trim();
            String taskTitle = editTitleTask.getText().toString().trim();
            String dateTime = textViewDateTime.getText().toString().trim();

            if (taskText.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите текст задачи", Toast.LENGTH_SHORT).show();
                return;
            }

            newTask.setText(taskText);
            newTask.setTitle(taskTitle);
            newTask.setImportant(switchPriority.isChecked());
            newTask.setNotify(switchNotify.isChecked());

            if (!dateTime.isEmpty()) {
                String[] dateTimeParts = dateTime.split(" ");
                newTask.setDateCreated(dateTimeParts[0]);
                newTask.setTimeCreated(dateTimeParts[1]);
            } else {
                // Если дата и время не были указаны, установите их как null
                newTask.setDateCreated(null);
                newTask.setTimeCreated(null);
            }

            dbHelper.addTask(newTask);
            addTaskToLayout(newTask);
            scheduleNotification(newTask);
            loadTasks();
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showDateTimePickerDialog(TextView textViewDateTime, Task task) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(year, month, day);
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String dateTimeString = sdf.format(calendar.getTime());
                textViewDateTime.setText(dateTimeString);
                String[] parts = dateTimeString.split(" ");
                task.setDateCreated(parts[0]);
                task.setTimeCreated(parts[1]);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void loadTasks() {
        dbHelper.getAllTasks(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Task> tasks = new ArrayList<>();
                for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    tasks.add(task);
                }
                tasksLayout.removeAllViews();
                tasks.sort((t1, t2) -> Boolean.compare(t2.isImportant(), t1.isImportant()));
                if (tasks.isEmpty()) {
                    findViewById(R.id.text_add_task_notification).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.text_add_task_notification).setVisibility(View.GONE);
                    for (Task task : tasks) {
                        addTaskToLayout(task);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WorkSpace.this, "Ошибка загрузки задач: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTaskToLayout(Task task) {
        View taskView = getLayoutInflater().inflate(R.layout.task_item, null);
        tasksLayout.addView(taskView);

        updateTaskView(taskView, task);

        ImageButton buttonDeleteTask = taskView.findViewById(R.id.button_delete_task);
        buttonDeleteTask.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(WorkSpace.this);
            builder.setTitle("Удалить задачу");
            builder.setMessage("Вы уверены, что хотите удалить эту задачу?");
            builder.setPositiveButton("Да", (dialog, which) -> {
                dbHelper.deleteTask(task);
                tasksLayout.removeView(taskView);
                dialog.dismiss();
            });
            builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        ImageButton buttonEditTask = taskView.findViewById(R.id.button_edit_task);
        buttonEditTask.setOnClickListener(v -> showEditTaskDialog(taskView, task));

        TextView taskCreationTimeView = taskView.findViewById(R.id.task_creation_time);

        // Установка времени создания задачи в поле "Создано"
        if (task.getCreationTime() != null) {
            taskCreationTimeView.setText("Создано: " + task.getCreationTime());
        } else {
            // Если время создания не установлено, берем текущее время
            Calendar currentDateTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String currentTime = sdf.format(currentDateTime.getTime());
            task.setCreationTime(currentTime);
            taskCreationTimeView.setText("Создано: " + currentTime);

            // Обновление задачи с текущим временем в базе данных
            dbHelper.updateTask(task);
        }

        ImageButton buttonShareTask = taskView.findViewById(R.id.btnShare);
        buttonShareTask.setOnClickListener(v -> {
            Intent intent = new Intent(WorkSpace.this, ShareTask.class);
            intent.putExtra("TASK_ID", task.getId());
            startActivity(intent);
        });
    }


    private void updateTaskView(View taskView, Task task) {
        TextView taskText = taskView.findViewById(R.id.task_text);
        taskText.setText(task.getText());

        TextView taskTitle = taskView.findViewById(R.id.task_title);
        taskTitle.setText(task.getTitle());

        TextView taskDateTime = taskView.findViewById(R.id.task_date_time);
        taskDateTime.setText(task.getDateCreated() + " " + task.getTimeCreated());

        ImageView imageStar = taskView.findViewById(R.id.image_star);
        if (task.isImportant()) {
            imageStar.setVisibility(View.VISIBLE);
        } else {
            imageStar.setVisibility(View.GONE);
        }
    }

    private void showEditTaskDialog(View taskView, Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Редактировать задачу");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_task, null);
        builder.setView(dialogView);

        EditText editTitleTask = dialogView.findViewById(R.id.editTitleTask);
        EditText editTextTask = dialogView.findViewById(R.id.editTextTask);
        TextView textViewDateTime = dialogView.findViewById(R.id.textViewDateTime);
        Switch switchPriority = dialogView.findViewById(R.id.switchPriority);
        Switch switchNotify = dialogView.findViewById(R.id.switch_notify);
        RadioGroup radioGroupTaskType = dialogView.findViewById(R.id.radioGroupTaskType);
        RadioButton radioButtonOneTime = dialogView.findViewById(R.id.radioButtonOneTime);
        RadioButton radioButtonRepeating = dialogView.findViewById(R.id.radioButtonRepeating);
        ImageButton buttonPickDateTime = dialogView.findViewById(R.id.buttonPickDateTime);

        editTitleTask.setText(task.getTitle());
        editTextTask.setText(task.getText());
        textViewDateTime.setText(task.getDateCreated() + " " + task.getTimeCreated());
        switchPriority.setChecked(task.isImportant());
        switchNotify.setChecked(task.isNotify());
        radioGroupTaskType.check(task.isRepeating() ? R.id.radioButtonRepeating : R.id.radioButtonOneTime);

        buttonPickDateTime.setOnClickListener(v -> showDateTimePickerDialog(textViewDateTime, task));

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String taskTitle = editTitleTask.getText().toString().trim();
            String taskText = editTextTask.getText().toString().trim();
            String dateTime = textViewDateTime.getText().toString().trim();

            if (taskTitle.isEmpty() || taskText.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите заголовок или текст задачи", Toast.LENGTH_SHORT).show();
                return;
            }

            task.setTitle(taskTitle);
            task.setText(taskText);

            if (!dateTime.isEmpty()) {
                String[] dateTimeParts = dateTime.split(" ");
                task.setDateCreated(dateTimeParts[0]);
                task.setTimeCreated(dateTimeParts[1]);
            } else {
                // Установите время и дату как null, если они не были указаны
                task.setDateCreated(null);
                task.setTimeCreated(null);
            }

            task.setImportant(switchPriority.isChecked());
            task.setNotify(switchNotify.isChecked());
            task.setRepeating(radioGroupTaskType.getCheckedRadioButtonId() == R.id.radioButtonRepeating);

            dbHelper.updateTask(task);
            updateTaskView(taskView, task);
            scheduleNotification(task);
            loadTasks();
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void scheduleNotification(Task task) {
        if (task.isNotify()) {
            String timeCreated = task.getTimeCreated();
            String dateCreated = task.getDateCreated();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            try {
                String dateTimeString = dateCreated + " " + timeCreated;
                Date date = sdf.parse(dateTimeString);
                long triggerAtMillis = date.getTime();

                Intent notificationIntent = new Intent(this, NotificationHelper.class);
                notificationIntent.putExtra("TASK_TEXT", task.getText());
                notificationIntent.putExtra("TASK_ID", task.getId());
                notificationIntent.putExtra("IS_REPEATING", task.isRepeating());

                // Convert the task ID to an integer for use as the request code
                int requestCode = task.getId().hashCode(); // Using hashCode() to generate a unique integer

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (task.isRepeating()) {
                    alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_DAY, pendingIntent);
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean isNotificationChannelCreated(String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
            return channel != null;
        }
        return true; // Каналы уведомлений не требуются для более старых версий SDK
    }
}