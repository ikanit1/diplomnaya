package com.example.diplomnaya;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class TaskDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "TaskManager";
    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TASK_TEXT = "task_text";
    private static final String COLUMN_TASK_TITLE = "task_title";
    private static final String COLUMN_TASK_CREATION = "task_creation"; // Обновлено: добавлено поле для времени создания задачи
    private static final String COLUMN_DATE_CREATED = "date_created";
    private static final String COLUMN_TIME_CREATED = "time_created";

    private static final String COLUMN_IMPORTANT = "important";
    private static final String COLUMN_NOTIFY = "notify";
    private static final String COLUMN_IS_REPEATING = "is_repeating";
    private DatabaseReference databaseReference;

    public TaskDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        databaseReference = FirebaseDatabase.getInstance().getReference("tasks"); // Ссылка на узел "tasks" в базе данных Firebase
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_TASKS = "CREATE TABLE " + TABLE_TASKS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_TASK_TEXT + " TEXT,"
                + COLUMN_TASK_TITLE + " TEXT,"
                + COLUMN_TASK_CREATION + " TEXT,"
                + COLUMN_DATE_CREATED + " TEXT,"
                + COLUMN_TIME_CREATED + " TEXT,"
                + COLUMN_IMPORTANT + " INTEGER,"
                + COLUMN_NOTIFY + " INTEGER,"
                + COLUMN_IS_REPEATING + " INTEGER"
                + ")";

        db.execSQL(CREATE_TABLE_TASKS);
    }


    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }



    public void addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TEXT, task.getText());
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_DATE_CREATED, task.getDateCreated());
        values.put(COLUMN_TASK_CREATION, task.getCreationTime()); // Обновлено: добавлено время создания задачи
        values.put(COLUMN_TIME_CREATED, task.getTimeCreated());
        values.put(COLUMN_NOTIFY, task.isNotify() ? 1 : 0);
        values.put(COLUMN_IMPORTANT, task.isImportant() ? 1 : 0);
        values.put(COLUMN_IS_REPEATING, task.isRepeating() ? 1 : 0); // Записываем 1 для true и 0 для false

        db.insert(TABLE_TASKS, null, values);
        db.close();
    }


    public void updateTask(Task task) {
        String taskId = String.valueOf(task.getId());
        // Проверяем, что задача существует в базе данных Firebase
        databaseReference.child("tasks").child(taskId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Задача существует, обновляем ее данные
                    databaseReference.child("tasks").child(taskId).setValue(task);
                } else {
                    // Задача не существует, можно создать новую или обработать эту ситуацию по вашему усмотрению
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибки
            }
        });
    }

    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                task.setText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TEXT)));
                task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)));
                task.setDateCreated(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_CREATED)));
                task.setNotify(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFY)) == 1);
                task.setTimeCreated(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME_CREATED)));
                task.setRepeating(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_REPEATING)) == 1);
                task.setImportant(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMPORTANT)) == 1);
                task.setCreationTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_CREATION)));
                taskList.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return taskList;
    }

    public void deleteTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
    }

    // Метод для отправки задачи в Firebase Realtime Database
    public void sendTaskToFirebase(Task task) {
        // Генерируем уникальный ключ для новой задачи в Firebase
        String key = databaseReference.child("tasks").push().getKey();
        if (key != null) {
            // Устанавливаем значение задачи по ключу в Firebase
            databaseReference.child("tasks").child(key).setValue(task);
        }
    }

    public void loadDataFromFirebaseToLocalDatabase() {
        // Получаем ссылку на узел с задачами в базе данных Firebase
        DatabaseReference tasksRef = databaseReference.child("tasks");

        // Слушаем изменения в узле с задачами
        tasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Очищаем локальную базу данных SQLite перед загрузкой новых данных
                clearLocalDatabase();

                // Проходим по всем задачам в базе данных Firebase
                for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                    // Получаем задачу из снимка данных
                    Task task = taskSnapshot.getValue(Task.class);
                    // Добавляем задачу в локальную базу данных SQLite
                    addTaskToLocalDatabase(task);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибки
            }
        });
    }

    public void deleteTaskFromFirebase(Task task) {
        String taskId = String.valueOf(task.getId());
        databaseReference.child("tasks").child(taskId).removeValue();
    }

    private void addTaskToLocalDatabase(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TEXT, task.getText());
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_DATE_CREATED, task.getDateCreated());
        values.put(COLUMN_TASK_CREATION, task.getCreationTime());
        values.put(COLUMN_TIME_CREATED, task.getTimeCreated());
        values.put(COLUMN_NOTIFY, task.isNotify() ? 1 : 0);
        values.put(COLUMN_IMPORTANT, task.isImportant() ? 1 : 0);
        values.put(COLUMN_IS_REPEATING, task.isRepeating() ? 1 : 0);

        db.insert(TABLE_TASKS, null, values);
        db.close();
    }

    private void clearLocalDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, null, null);
        db.close();
    }
}
