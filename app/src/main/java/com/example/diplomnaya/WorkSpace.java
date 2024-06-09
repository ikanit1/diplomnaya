package com.example.diplomnaya;

import static android.graphics.Color.WHITE;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.CompoundButtonCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.Response;


public class WorkSpace extends AppCompatActivity {
    private LinearLayout tasksLayout;
    private final String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
    private TaskDatabaseHelper dbHelper;
    private String currentUserId;
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
        findViewById(R.id.btnShare).setOnClickListener(v -> showAddTaskDialog());

            // Добавьте обработчик нажатия для кнопки btnShare
        findViewById(R.id.btnShare).setOnClickListener(v -> {
            // Создайте Intent для перехода на класс ShareTask
            Intent intent = new Intent(WorkSpace.this, ShareTask.class);

            // Передайте дополнительные данные, если это необходимо
            // Например, можно передать текущий идентификатор пользователя или другие данные
            // intent.putExtra("key", "value");

            // Запустите активность ShareTask
            startActivity(intent);
        });


        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadTasks();
            swipeRefreshLayout.setRefreshing(false);
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Handle focus or reinitialization if needed
        loadTasks(); // Reload tasks when the activity resumes
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Release resources or save state if needed
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Уведомления заметок";
            String description = "Как будут появлятся заметки";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("channel_name", name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
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

        radioGroupTaskType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonRepeating) {
                newTask.setRepeating(true);
                showRepeatingTaskSettingsDialog(newTask);
            } else if (checkedId == R.id.radioButtonOneTime) {
                newTask.setRepeating(false);
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
                newTask.setDateCreated(null);
                newTask.setTimeCreated(null);
            }

            dbHelper.addTask(newTask);
            addTaskToLayout(newTask);
            scheduleNotification(newTask);
            loadTasks();
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Установка фокуса на поле ввода заголовка задачи и открытие клавиатуры
        editTitleTask.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }




//    private void showAIInputDialog(EditText editTitleTask, EditText editTextTask) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Сгенерировать задачу");
//
//        LayoutInflater inflater = getLayoutInflater();
//        View dialogView = inflater.inflate(R.layout.dialog_ai_input, null);
//        builder.setView(dialogView);
//
//        EditText editPrompt = dialogView.findViewById(R.id.editPrompt);
//        Button buttonSubmit = dialogView.findViewById(R.id.button_submit);
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
//
//        buttonSubmit.setOnClickListener(v -> {
//            String prompt = editPrompt.getText().toString().trim();
//            if (!prompt.isEmpty()) {
//                // Укажите API-ключ здесь
//                String apiKey = "ключ";
//
//                if (apiKey.isEmpty()) {
//                    Toast.makeText(WorkSpace.this, "API-ключ не указан", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                try {
//                    OpenAIHelper.generateTaskContent(apiKey, prompt, new Callback() {
//                        @Override
//                        public void onFailure(Call call, IOException e) {
//                            Log.e("OpenAI", "Ошибка запроса", e); // Лог ошибки
//                            runOnUiThread(() -> Toast.makeText(WorkSpace.this, "Ошибка генерации задачи", Toast.LENGTH_SHORT).show());
//                        }
//
//                        @Override
//                        public void onResponse(Call call, Response response) throws IOException {
//                            if (response.isSuccessful()) {
//                                String responseBody = response.body().string();
//                                try {
//                                    JSONObject jsonObject = new JSONObject(responseBody);
//                                    String generatedText = jsonObject.getJSONArray("choices").getJSONObject(0).getString("text").trim();
//                                    runOnUiThread(() -> {
//                                        String[] parts = generatedText.split("\\n", 2);
//                                        if (parts.length == 2) {
//                                            editTitleTask.setText(parts[0]);
//                                            editTextTask.setText(parts[1]);
//                                        } else {
//                                            editTextTask.setText(generatedText);
//                                        }
//                                        dialog.dismiss();
//                                    });
//                                } catch (JSONException e) {
//                                    Log.e("OpenAI", "Ошибка обработки JSON", e); // Лог ошибки JSON
//                                    runOnUiThread(() -> Toast.makeText(WorkSpace.this, "Ошибка обработки JSON ответа", Toast.LENGTH_SHORT).show());
//                                }
//                            } else {
//                                Log.e("OpenAI", "Ошибка ответа: " + response.code()); // Лог кода ответа
//                                runOnUiThread(() -> Toast.makeText(WorkSpace.this, "Ошибка генерации задачи", Toast.LENGTH_SHORT).show());
//                            }
//                        }
//                    });
//                } catch (JSONException e) {
//                    Log.e("OpenAI", "Ошибка создания JSON запроса", e); // Лог ошибки JSON
//                    Toast.makeText(WorkSpace.this, "Ошибка создания JSON запроса", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Toast.makeText(this, "Пожалуйста, введите запрос", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

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
        if (task.getRepeatingTime() != null && task.getRepeatingTime().matches("\\d{2}:\\d{2}")) {
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
        CheckBox checkboxCompleted = taskView.findViewById(R.id.checkbox_completed);
        checkboxCompleted.setTextColor(getResources().getColor(android.R.color.black));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkboxCompleted.setButtonTintList(ColorStateList.valueOf(WHITE));
        } else {
            CompoundButtonCompat.setButtonTintList(checkboxCompleted, ColorStateList.valueOf(Color.BLACK));
        }
        checkboxCompleted.setChecked(task.isCompleted());
        checkboxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            dbHelper.updateTask(task); // Update the task in the local database
            updateTaskCompletionStatusInFirebase(task); // Update the task in Firebase
        });



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
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Удалить задачу");
            builder.setMessage("Вы уверены, что хотите удалить эту задачу?");
            builder.setPositiveButton("Да", (dialog, which) -> {
                dbHelper.deleteTask(task);
                tasksLayout.removeView(taskView);
                dialog.dismiss();
            });
            builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.setOnShowListener(d -> {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                positiveButton.setTextColor(getResources().getColor(R.color.white));
                negativeButton.setTextColor(getResources().getColor(R.color.white));
            });
        });


        // Обработчик для кнопки редактирования задачи
        buttonEditTask.setOnClickListener(v -> showEditTaskDialog(taskView, task));

        // Перенастройка уведомлений для задачи после добавления ее в макет
        scheduleNotification(task);
    }
    private void updateTaskCompletionStatusInFirebase(Task task) {
        DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference()
                .child("tasks")
                .child(task.getId());
        taskRef.child("completed").setValue(task.isCompleted())
                .addOnSuccessListener(aVoid -> showToast("Статус выполнения задачи обновлен"))
                .addOnFailureListener(e -> showToast("Не удалось обновить статус выполнения задачи: " + e.getMessage()));
    }



    // Метод для деления задачи с группой
    private void shareTaskWithGroup(String groupId, Task task) {
        task.setShared(true);
        task.setGroupId(groupId);
        DatabaseReference groupTasksRef = FirebaseDatabase.getInstance().getReference().child("groups").child(groupId).child("tasks");
        String taskId = groupTasksRef.push().getKey();
        if (taskId != null) {
            task.setId(taskId); // Установите новый ID задачи
            groupTasksRef.child(taskId).setValue(task)
                    .addOnSuccessListener(aVoid -> showToast("Задача поделена с группой"))
                    .addOnFailureListener(e -> showToast("Ошибка при делении задачи: " + e.getMessage()));
        }
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    // Метод для показа диалога выбора группы
    private void showGroupSelectionDialog(String currentUserId, Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите группу");

        // Получение списка групп пользователя из Firebase
        DatabaseReference userGroupsRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(currentUserId)
                .child("groups");

        userGroupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Group> userGroups = new ArrayList<>();
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    Group group = groupSnapshot.getValue(Group.class);
                    if (group != null) {
                        userGroups.add(group);
                    }
                }

                // Создание массива имен групп для диалога
                String[] groupNames = new String[userGroups.size()];
                for (int i = 0; i < userGroups.size(); i++) {
                    groupNames[i] = userGroups.get(i).getGroupName();
                }

                // Установка элементов списка групп в диалог и добавление обработчика нажатия
                builder.setItems(groupNames, (dialog, which) -> {
                    String selectedGroupId = userGroups.get(which).getGroupId();
                    shareTaskWithGroup(selectedGroupId, task);
                });

                // Добавление кнопки отмены
                builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
                // Показ диалога после подготовки данных
                builder.show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок при чтении данных из базы данных
                showToast("Ошибка при получении списка групп: " + databaseError.getMessage());
            }
        });
    }


    // Метод для получения списка групп пользователя
    private List<Group> getUserGroups() {
        List<Group> userGroups = new ArrayList<>();

        // Получение ссылки на узел в базе данных, где хранятся группы пользователя
        DatabaseReference userGroupsRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("groups");

        // Добавление слушателя для получения данных о группах пользователя из базы данных
        userGroupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userGroups.clear(); // Очистка списка перед загрузкой новых данных

                // Обход всех дочерних узлов (групп) в базе данных
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    // Преобразование данных из снимка в объект Group
                    Group group = groupSnapshot.getValue(Group.class);
                    if (group != null) {
                        // Добавление группы в список
                        userGroups.add(group);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок при чтении данных из базы данных
                showToast("Ошибка при получении списка групп: " + databaseError.getMessage());
            }
        });

        return userGroups;
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
        AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(getResources().getColor(R.color.black));
        negativeButton.setTextColor(getResources().getColor(R.color.black));
    }





    @SuppressLint("ScheduleExactAlarm")
    private void scheduleNotification(Task task) {
        if (task.isNotify()) {
            // Проверяем, что время повторения не равно null перед вызовом split
            if (task.getRepeatingTime() != null) {
                // Разделите время на часы и минуты
                String[] timeParts = task.getRepeatingTime().split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);

                // Создайте Calendar для установки времени
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                // Если время уже прошло, добавьте один день для одноразовой задачи
                if (!task.isRepeating() && calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    calendar.add(Calendar.DATE, 1);
                }

                // Если задача повторяется, установите повторяющийся будильник на каждый день повторения
                if (task.isRepeating()) {
                    List<Integer> repeatingDays = task.getRepeatingDays();
                    for (Integer day : repeatingDays) {
                        Calendar repeatingCalendar = (Calendar) calendar.clone();
                        repeatingCalendar.set(Calendar.DAY_OF_WEEK, day + 1);

                        // Если день уже прошел, добавьте неделю для следующего раза
                        if (repeatingCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
                            repeatingCalendar.add(Calendar.DATE, 7);
                        }

                        // Создайте Intent и PendingIntent для повторяющихся уведомлений
                        Intent notificationIntent = new Intent(this, NotificationHelper.class);
                        notificationIntent.putExtra("TASK_TEXT", task.getText());
                        notificationIntent.putExtra("TASK_ID", task.getId());
                        notificationIntent.putExtra("IS_REPEATING", task.isRepeating());

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                this, task.getId().hashCode() + day, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                        // Установите повторяющийся будильник
                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                repeatingCalendar.getTimeInMillis(),
                                pendingIntent
                        );
                    }
                } else {
                    // Для одноразовых задач установите будильник только один раз
                    Intent notificationIntent = new Intent(this, NotificationHelper.class);
                    notificationIntent.putExtra("TASK_TEXT", task.getText());
                    notificationIntent.putExtra("TASK_ID", task.getId());
                    notificationIntent.putExtra("IS_REPEATING", task.isRepeating());

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            this, task.getId().hashCode(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    // Установите одноразовый будильник
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }
            }
        }
    }

}