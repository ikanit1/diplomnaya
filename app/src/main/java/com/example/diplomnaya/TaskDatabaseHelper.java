package com.example.diplomnaya;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

    public TaskDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TEXT, task.getText());
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_DATE_CREATED, task.getDateCreated());
        values.put(COLUMN_TASK_CREATION, task.getCreationTime()); // Обновлено: добавлено время создания задачи
        values.put(COLUMN_TIME_CREATED, task.getTimeCreated());
        values.put(COLUMN_NOTIFY, task.isNotify() ? 1 : 0);
        values.put(COLUMN_IMPORTANT, task.isImportant() ? 1 : 0);
        values.put(COLUMN_IS_REPEATING, task.isRepeating() ? 1 : 0);

        db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
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

//    @SuppressLint("Range")
//    public Task getTaskById(int id) {
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor cursor = db.query(TABLE_TASKS,
//                new String[]{COLUMN_ID, COLUMN_TASK_TEXT, COLUMN_TASK_TITLE, COLUMN_DATE_CREATED, COLUMN_TIME_CREATED, COLUMN_IMPORTANT, COLUMN_NOTIFY, COLUMN_TASK_CREATION},
//                COLUMN_ID + "=?",
//                new String[]{String.valueOf(id)},
//                null,
//                null,
//                null,
//                null);
//
//        if (cursor != null && cursor.moveToFirst()) {
//            Task task = new Task();
//            task.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
//            task.setText(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TEXT)));
//            task.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TITLE)));
//            task.setCreationTime(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_CREATION))); // Исправлено
//            task.setDateCreated(cursor.getString(cursor.getColumnIndex(COLUMN_DATE_CREATED)));
//            task.setTimeCreated(cursor.getString(cursor.getColumnIndex(COLUMN_TIME_CREATED)));
//            task.setImportant(cursor.getInt(cursor.getColumnIndex(COLUMN_IMPORTANT)) == 1);
//            task.setNotify(cursor.getInt(cursor.getColumnIndex(COLUMN_NOTIFY)) == 1);
//            task.setRepeating(cursor.getInt(cursor.getColumnIndex(COLUMN_IS_REPEATING)) == 1);
//
//            cursor.close();
//
//            return task;
//        } else {
//            return null;
//        }
//    }

    public void deleteTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
    }
}
