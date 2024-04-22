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
import android.widget.Button;
import android.widget.CheckBox;
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
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.app.AlarmManager;



public class WorkSpace extends AppCompatActivity {
    private LinearLayout tasksLayout;
    private final String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
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
                Intent intent = new Intent(WorkSpace.this, OwnRoom.class);

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

        Task newTask = new Task();

        // Обработчик для выбора опции повторения
        radioGroupTaskType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonRepeating) {
                // Установите, что задача повторяется
                newTask.setRepeating(true);
                // Показ диалогового окна с настройками повторения задачи
                showRepeatingTaskSettingsDialog(newTask);
            } else if (checkedId == R.id.radioButtonOneTime) {
                // Установите, что задача не повторяется
                newTask.setRepeating(false);
                // Показ диалогового окна выбора даты и времени
                showDateTimePickerDialog(textViewDateTime, newTask);
            }
        });

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

            // После выбора повторения или одноразовой задачи обработчик должен установить повторяющиеся дни и время повторения в диалоговом окне настроек повторения.
            dbHelper.addTask(newTask);
            addTaskToLayout(newTask);
            scheduleNotification(newTask);
            loadTasks();
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }



    private void showRepeatingTaskSettingsDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Настройки повторения задачи");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_repeating_task_settings, null);
        builder.setView(dialogView);

        CheckBox[] dayCheckboxes = new CheckBox[7];
        dayCheckboxes[0] = dialogView.findViewById(R.id.checkbox_monday);
        dayCheckboxes[1] = dialogView.findViewById(R.id.checkbox_tuesday);
        dayCheckboxes[2] = dialogView.findViewById(R.id.checkbox_wednesday);
        dayCheckboxes[3] = dialogView.findViewById(R.id.checkbox_thursday);
        dayCheckboxes[4] = dialogView.findViewById(R.id.checkbox_friday);
        dayCheckboxes[5] = dialogView.findViewById(R.id.checkbox_saturday);
        dayCheckboxes[6] = dialogView.findViewById(R.id.checkbox_sunday);

        TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true); // Установите формат времени в 24-часовой

        // Установите начальные значения для CheckBox и TimePicker, если они уже сохранены в задаче
        if (task.getRepeatingDays() != null) {
            for (int i = 0; i < 7; i++) {
                dayCheckboxes[i].setChecked(task.getRepeatingDays().contains(i));
            }
        }
        if (task.getRepeatingTime() != null) {
            // Разделите время на часы и минуты
            String[] timeParts = task.getRepeatingTime().split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            timePicker.setHour(hour);
            timePicker.setMinute(minute);
        }

        // Создание диалогового окна
        AlertDialog dialog = builder.create();

        // Обработчик для кнопки "Применить"
        Button applyButton = dialogView.findViewById(R.id.btn_apply);
        applyButton.setOnClickListener(v -> {
            // Получите выбранные дни недели и сохраните их
            List<Integer> repeatingDays = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                if (dayCheckboxes[i].isChecked()) {
                    repeatingDays.add(i);
                }
            }
            task.setRepeatingDays(repeatingDays);

            // Получите выбранное время и сохраните его
            int selectedHour = timePicker.getHour();
            int selectedMinute = timePicker.getMinute();
            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
            task.setRepeatingTime(selectedTime);

            // Если задача повторяется, установите дату
            if (!repeatingDays.isEmpty()) {
                // Используйте текущую дату для установки даты задачи
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String selectedDate = sdf.format(calendar.getTime());
                task.setDateCreated(selectedDate);
            }

            // Обновите задачу в базе данных
            dbHelper.updateTask(task);

            // Перенастройте уведомление
            scheduleNotification(task);

            // Закройте диалоговое окно
            dialog.dismiss();
        });

        builder.setNegativeButton("Отмена", (dialogInterface, which) -> dialogInterface.dismiss());

        // Покажите диалоговое окно
        dialog.show();
    }


    // Метод для получения сокращения дня недели по индексу
    private String getDayOfWeekAbbreviation(int dayIndex) {
        switch (dayIndex) {
            case 0:
                return "Mon";
            case 1:
                return "Tue";
            case 2:
                return "Wed";
            case 3:
                return "Thu";
            case 4:
                return "Fri";
            case 5:
                return "Sat";
            case 6:
                return "Sun";
            default:
                return "";
        }
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
        // Создание представления задачи из макета task_item
        View taskView = getLayoutInflater().inflate(R.layout.task_item, null);
        tasksLayout.addView(taskView);

        // Обновление представления задачи
        updateTaskView(taskView, task);

        // Получение ссылок на элементы управления в представлении задачи
        ImageButton buttonDeleteTask = taskView.findViewById(R.id.button_delete_task);
        ImageButton buttonEditTask = taskView.findViewById(R.id.button_edit_task);
        TextView taskCreationTimeView = taskView.findViewById(R.id.task_creation_time);
        TextView taskDateTimeView = taskView.findViewById(R.id.task_date_time);

        // Установка времени создания задачи
        if (task.getCreationTime() != null) {
            taskCreationTimeView.setText("Создано: " + task.getCreationTime());
        } else {
            // Если время создания не установлено, используйте текущее время
            Calendar currentDateTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String currentTime = sdf.format(currentDateTime.getTime());
            task.setCreationTime(currentTime);
            taskCreationTimeView.setText("Создано: " + currentTime);
            // Обновление задачи с текущим временем в базе данных
            dbHelper.updateTask(task);
        }

        // Установка времени выполнения задачи в TextView
        if (task.isRepeating()) {
            String days = ""; // Строка для отображения выбранных дней недели
            for (int day : task.getRepeatingDays()) {
                // Добавьте названия дней недели
                days += dayNames[day] + " "; // Используйте массив dayNames
            }
            taskDateTimeView.setText("Повторение: " + days + task.getRepeatingTime());
        } else {
            taskDateTimeView.setText("Выполнить: " + task.getDateCreated() + " " + task.getTimeCreated());
        }

        // Обработчик для кнопки удаления задачи
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

        // Обработчик для кнопки редактирования задачи
        buttonEditTask.setOnClickListener(v -> showEditTaskDialog(taskView, task));


        // Перенастройка уведомлений для задачи после добавления ее в макет
        scheduleNotification(task);
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

        // Получение элементов управления из макета диалогового окна
        EditText editTitleTask = dialogView.findViewById(R.id.editTitleTask);
        EditText editTextTask = dialogView.findViewById(R.id.editTextTask);
        TextView textViewDateTime = dialogView.findViewById(R.id.textViewDateTime);
        Switch switchPriority = dialogView.findViewById(R.id.switchPriority);
        Switch switchNotify = dialogView.findViewById(R.id.switch_notify);
        RadioGroup radioGroupTaskType = dialogView.findViewById(R.id.radioGroupTaskType);
        RadioButton radioButtonOneTime = dialogView.findViewById(R.id.radioButtonOneTime);
        RadioButton radioButtonRepeating = dialogView.findViewById(R.id.radioButtonRepeating);

        // Установка значений в элементы управления из текущей задачи
        editTitleTask.setText(task.getTitle());
        editTextTask.setText(task.getText());
        textViewDateTime.setText(task.getDateCreated() + " " + task.getTimeCreated());
        switchPriority.setChecked(task.isImportant());
        switchNotify.setChecked(task.isNotify());
        radioGroupTaskType.check(task.isRepeating() ? R.id.radioButtonRepeating : R.id.radioButtonOneTime);

        // Обработчик выбора типа задачи (одноразовой или повторяющейся)
        radioGroupTaskType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonOneTime) {
                // Задача одноразовая: показать диалог для выбора даты и времени
                showDateTimePickerDialog(textViewDateTime, task);
            } else if (checkedId == R.id.radioButtonRepeating) {
                // Задача повторяющаяся: показать диалог для выбора дней повторения и времени
                showRepeatingTaskSettingsDialog(task);
            }
        });

        // Обработчик для сохранения изменений в задаче
        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            // Получение значений из формы диалога
            String taskTitle = editTitleTask.getText().toString().trim();
            String taskText = editTextTask.getText().toString().trim();
            String dateTime = textViewDateTime.getText().toString().trim();

            // Проверка на пустые значения заголовка или текста задачи
            if (taskTitle.isEmpty() || taskText.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите заголовок или текст задачи", Toast.LENGTH_SHORT).show();
                return;
            }

            // Установка значений задачи
            task.setTitle(taskTitle);
            task.setText(taskText);
            task.setImportant(switchPriority.isChecked());
            task.setNotify(switchNotify.isChecked());
            task.setRepeating(radioGroupTaskType.getCheckedRadioButtonId() == R.id.radioButtonRepeating);

            // Установка даты и времени задачи, если они не пустые
            if (!dateTime.isEmpty()) {
                String[] dateTimeParts = dateTime.split(" ");
                task.setDateCreated(dateTimeParts[0]);
                task.setTimeCreated(dateTimeParts[1]);
            } else {
                task.setDateCreated(null);
                task.setTimeCreated(null);
            }

            // Обновление задачи в базе данных
            dbHelper.updateTask(task);
            updateTaskView(taskView, task);
            scheduleNotification(task);
            loadTasks();
        });

        // Обработчик для отмены редактирования
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }



    private void scheduleNotification(Task task) {
        if (task.isNotify()) {
            // Получите текущий день недели и время
            Calendar calendar = Calendar.getInstance();
            int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            // Получите список повторяющихся дней недели и время повторения
            List<Integer> repeatingDays = task.getRepeatingDays();
            String repeatingTime = task.getRepeatingTime();

            // Проверьте, совпадает ли текущий день недели с повторяющимися днями
            if (repeatingDays != null && repeatingTime != null && repeatingDays.contains(currentDayOfWeek - 1)) {
                // Разделите время повторения на часы и минуты
                String[] timeParts = repeatingTime.split(":");
                int repeatingHour = Integer.parseInt(timeParts[0]);
                int repeatingMinute = Integer.parseInt(timeParts[1]);

                // Проверьте, совпадает ли текущее время с временем повторения
                if (currentHour == repeatingHour && currentMinute == repeatingMinute) {
                    // Создайте Intent для уведомления
                    Intent notificationIntent = new Intent(this, NotificationHelper.class);
                    notificationIntent.putExtra("TASK_TEXT", task.getText());
                    notificationIntent.putExtra("TASK_ID", task.getId());
                    notificationIntent.putExtra("IS_REPEATING", task.isRepeating());

                    // Создайте PendingIntent для уведомления
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            this, task.getId().hashCode(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    // Отправьте уведомление с помощью AlarmManager
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
        }
    }


}