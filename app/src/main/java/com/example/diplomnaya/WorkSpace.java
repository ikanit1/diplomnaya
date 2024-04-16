package com.example.diplomnaya;
import android.app.Notification;
import android.app.NotificationChannel;
import android.graphics.Paint;
import android.app.NotificationManager;
import android.os.Build;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.widget.RadioButton;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
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
import java.util.Collections;
import java.util.Comparator;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!isNotificationChannelCreated("channel_name")) {
                createNotificationChannel();
            }
        }

        tasksLayout = findViewById(R.id.tasks_layout);
        dbHelper = new TaskDatabaseHelper(this);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        loadTasks();

        findViewById(R.id.button_add_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTasks();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showAddTaskDialog() {
        final Task newTask = new Task();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить задачу");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        final EditText editTitleTask = dialogView.findViewById(R.id.editTitleTask);
        final EditText editTextTask = dialogView.findViewById(R.id.editTextTask);
        final TextView textViewDateTime = dialogView.findViewById(R.id.textViewDateTime);
        ImageButton buttonPickDateTime = dialogView.findViewById(R.id.buttonPickDateTime);
        Switch switchPriority = dialogView.findViewById(R.id.switchPriority);
        Switch switchNotify = dialogView.findViewById(R.id.switch_notify);
        RadioGroup radioGroupTaskType = dialogView.findViewById(R.id.radioGroupTaskType);
        RadioButton radioButtonOneTime = dialogView.findViewById(R.id.radioButtonOneTime);
        RadioButton radioButtonRepeating = dialogView.findViewById(R.id.radioButtonRepeating);

        radioButtonRepeating.setChecked(newTask.isRepeating());

        builder.setView(dialogView);

        switchNotify.setChecked(newTask.isNotify());

        final Calendar calendar = Calendar.getInstance();

        final TextView textSelectDateTime = dialogView.findViewById(R.id.textSelectDateTime); // Добавляем это
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

                                // Проверяем, выбрано ли уже время, и скрываем или показываем соответствующий TextView
                                if (!dateTime.isEmpty()) {
                                    textSelectDateTime.setVisibility(View.GONE); // Скрыть TextView
                                } else {
                                    textSelectDateTime.setVisibility(View.VISIBLE); // Показать TextView
                                }
                            }
                        }, hour, minute, true);
                        timePickerDialog.show();
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        radioButtonRepeating.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Проверяем, выбран ли RadioButton "Повторяющаяся задача"
                if (isChecked) {
                    newTask.setRepeating(true); // Если выбран, устанавливаем repeating в true
                } else {
                    newTask.setRepeating(false); // Иначе устанавливаем repeating в false
                }
            }
        });

        builder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String taskText = editTextTask.getText().toString().trim();
                String taskTitle = editTitleTask.getText().toString().trim();
                if (!taskTitle.isEmpty()) {
                    newTask.setTitle(taskTitle);
                }
                String dateTime = textViewDateTime.getText().toString().trim();
                boolean isImportant = switchPriority.isChecked();
                boolean notify = switchNotify.isChecked();
                newTask.setNotify(notify);

                // Определяем тип задачи в зависимости от выбранного переключателя
                if (radioButtonOneTime.isChecked()) {
                    newTask.setRepeating(false);
                } else if (radioButtonRepeating.isChecked()) {
                    newTask.setRepeating(true);
                }

                if (!taskText.isEmpty()) {
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
                    scheduleNotification(newTask); // Вызовите метод планирования уведомления после добавления задачи
                } else {
                    Toast.makeText(WorkSpace.this, "Пожалуйста, введите текст задачи", Toast.LENGTH_SHORT).show();
                }
                loadTasks();
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

    private boolean isDateTimePassed(String date, String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date taskDateTime = sdf.parse(date + " " + time); // Парсим дату и время задачи
            Date currentDateTime = new Date(); // Получаем текущую дату и время
            // Сравниваем дату и время задачи с текущей датой и временем
            return taskDateTime != null && currentDateTime.after(taskDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateTaskView(View taskView, Task task) {
        TextView taskTextView = taskView.findViewById(R.id.task_text);
        taskTextView.setText(task.getText());

        TextView taskTitleView = taskView.findViewById(R.id.task_title);
        taskTitleView.setText(task.getTitle());

        TextView taskDateTimeView = taskView.findViewById(R.id.task_date_time);
        String dateTime = task.getDateCreated() + " " + task.getTimeCreated();
        taskDateTimeView.setText(dateTime);

        // Проверяем, если время уже прошло, перечеркиваем текст
        if (isDateTimePassed(task.getDateCreated(), task.getTimeCreated())) {
            taskTextView.setPaintFlags(taskTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            taskDateTimeView.setPaintFlags(taskDateTimeView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            // Если время еще не прошло, возвращаем обычный текст
            taskTextView.setPaintFlags(taskTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            taskDateTimeView.setPaintFlags(taskDateTimeView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

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

        TextView taskTitleView = taskView.findViewById(R.id.task_title); // Найдите TextView для заголовка
        taskTitleView.setText(task.getTitle()); // Установите заголовок

        TextView taskDateTimeView = taskView.findViewById(R.id.task_date_time);
        String dateTime = task.getDateCreated() + " " + task.getTimeCreated();
        taskDateTimeView.setText(dateTime);

        TextView taskCreationTimeView = taskView.findViewById(R.id.task_creation_time);

        // Проверяем, установлено ли время создания в объекте задачи
        if (task.getCreationTime() != null && !task.getCreationTime().isEmpty()) {
            taskCreationTimeView.setText("Дата создания: " + task.getCreationTime());
        } else {
            // Если время создания не установлено, берем текущее системное время
            Calendar currentDateTime = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String dateTimeCreated = sdf.format(currentDateTime.getTime());
            taskCreationTimeView.setText("Дата создания: " + dateTimeCreated);

            // Сохраняем время создания в объекте задачи
            task.setCreationTime(dateTimeCreated);
            // Обновляем базу данных с новым временем создания задачи
            dbHelper.updateTask(task);
        }

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
                        loadTasks();
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

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditTaskDialog(taskView, task);
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

        if (tasks.isEmpty()) {
            // Если список задач пуст, делаем TextView видимым
            findViewById(R.id.text_add_task_notification).setVisibility(View.VISIBLE);
        } else {
            // Если в списке есть задачи, скрываем TextView
            findViewById(R.id.text_add_task_notification).setVisibility(View.GONE);
        }

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
        final EditText editTitleTask = editDialogView.findViewById(R.id.editTitleTask);
        final TextView textViewDateTime = editDialogView.findViewById(R.id.textViewDateTime);
        final TextView textSelectDateTime = editDialogView.findViewById(R.id.textSelectDateTime);
        ImageButton buttonPickDateTime = editDialogView.findViewById(R.id.buttonPickDateTime);
        Switch switchPriority = editDialogView.findViewById(R.id.switchPriority);
        Switch switchNotify = editDialogView.findViewById(R.id.switch_notify);
        RadioButton radioButtonRepeating = editDialogView.findViewById(R.id.radioButtonRepeating);

        cancelNotification(task);
        scheduleNotification(task); // Планирование нового уведомления
        // Установка состояния переключателя уведомлений в соответствии с задачей
        switchNotify.setChecked(task.isNotify());

        switchNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Обновляем значение task.isNotify() в соответствии с новым состоянием переключателя
                task.setNotify(isChecked);
            }
        });

        // Установка состояния RadioButton в соответствии с текущим значением задачи
        radioButtonRepeating.setChecked(task.isRepeating());
        editTextTask.setText(task.getText());
        editTitleTask.setText(task.getTitle()); // Установка текста заголовка задачи
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

                                // Проверяем, выбрано ли уже время, и скрываем или показываем соответствующий TextView
                                if (!dateTime.isEmpty()) {
                                    textSelectDateTime.setVisibility(View.GONE); // Скрыть TextView
                                } else {
                                    textSelectDateTime.setVisibility(View.VISIBLE); // Показать TextView
                                }
                            }
                        }, hour, minute, true);
                        timePickerDialog.show();
                    }
                }, year, month, day);
                datePickerDialog.show();
            }

        });

        radioButtonRepeating.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Проверяем, выбран ли RadioButton "Повторяющаяся задача"
                if (isChecked) {
                    task.setRepeating(true); // Устанавливаем repeating в true для конкретной задачи
                } else {
                    task.setRepeating(false); // Устанавливаем repeating в false для конкретной задачи
                }
            }
        });


        editDialogBuilder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String editedTaskText = editTextTask.getText().toString().trim();
                String editedTaskTitle = editTitleTask.getText().toString().trim();
                String editedDateTime = textViewDateTime.getText().toString().trim();

                // Проверка, что текст задачи и дата и время не пустые
                if (editedTaskText.isEmpty() || editedDateTime.isEmpty()) {
                    Toast.makeText(WorkSpace.this, "Пожалуйста, введите задачу и выберите дату и время", Toast.LENGTH_SHORT).show();
                    return; // Прекратить выполнение метода, если одно из условий не выполнено
                }

                // Обновление данных задачи
                task.setText(editedTaskText);
                task.setTitle(editedTaskTitle); // Обновление заголовка задачи
                String[] parts = editedDateTime.split(" ");
                task.setDateCreated(parts[0]);
                task.setTimeCreated(parts[1]);
                task.setImportant(switchPriority.isChecked());
                task.setRepeating(radioButtonRepeating.isChecked());
                dbHelper.updateTask(task);
                task.setNotify(switchNotify.isChecked());
                updateTaskView(taskView, task);

                // Отменяем предыдущее уведомление перед планированием нового
                cancelNotification(task);
                task.setNotify(switchNotify.isChecked());
                // Планирование нового уведомления
                scheduleNotification(task);
                loadTasks();
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

    private void createNotificationChannel() {
        // Проверка версии SDK, так как создание каналов уведомлений требуется только для API 26 и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = "Как будут появляться уведомления";
            int importance = NotificationManager.IMPORTANCE_HIGH; // Повышаем важность уведомлений до HIGH
            NotificationChannel channel = new NotificationChannel("channel_name", name, importance);
            channel.setDescription(description);

            // Включение звука, вибрации и всплывающих уведомлений
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); // Показ уведомлений на экране блокировки

            // Получение менеджера уведомлений и создание канала
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void cancelNotification(Task task) {
        Intent notificationIntent = new Intent(this, NotificationHelper.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, task.getId(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }



    private void scheduleNotification(Task task) {
        if (task.isNotify()) {
            // Получение даты и времени задачи
            String dateTime = task.getDateCreated() + " " + task.getTimeCreated();

            // Проверка наличия времени
            if (task.getTimeCreated().equals("Время не установлено")) {
                Toast.makeText(this, "Пожалуйста, укажите время для задачи", Toast.LENGTH_SHORT).show();
                return; // Если время не указано, выходим из метода без планирования уведомления
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            try {
                Date date = sdf.parse(dateTime);
                long triggerAtMillis = date.getTime();

                Intent notificationIntent = new Intent(this, NotificationHelper.class);
                notificationIntent.putExtra("TASK_TEXT", task.getText());
                notificationIntent.putExtra("TASK_ID", task.getId());
                notificationIntent.putExtra("IS_REPEATING", task.isRepeating()); // Добавляем флаг повторения

                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, task.getId(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                // Установка уведомления с использованием AlarmManager
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                // Используем разные методы для планирования повторяющегося и одноразового уведомлений
                if (task.isRepeating()) {
                    // Планируем повторяющееся уведомление с интервалом в сутки
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_DAY, pendingIntent);
                } else {
                    // Устанавливаем одноразовое уведомление
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Проверка, создан ли канал уведомлений
            if (!isNotificationChannelCreated("channel_name")) {
                createNotificationChannel();
            }
        }
    }



    private boolean isNotificationChannelCreated(String channelId) {
        // Проверяем, создан ли канал уведомлений с заданным идентификатором
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
            return channel != null;
        }
        // Если SDK меньше версии 26, возвращаем true, так как каналы уведомлений не требуются
        return true;
    }
}




