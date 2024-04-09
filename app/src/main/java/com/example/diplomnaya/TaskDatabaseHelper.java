package com.example.diplomnaya;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class TaskDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "TaskManager";
    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TASK_TEXT = "task_text";
    private static final String COLUMN_DATE_CREATED = "date_created";
    private static final String COLUMN_TIME_CREATED = "time_created";

    public TaskDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_TASK_TEXT + " TEXT,"
                + COLUMN_DATE_CREATED + " TEXT,"
                + COLUMN_TIME_CREATED + " TEXT)";
        db.execSQL(CREATE_TASKS_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Удаляем старую таблицу
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        // Создаем новую таблицу
        onCreate(db);
    }


    public void addTask(Task task) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TASK_TEXT, task.getText());
            values.put(COLUMN_DATE_CREATED, task.getDateCreated());
            values.put(COLUMN_TIME_CREATED, task.getTimeCreated());
            db.insert(TABLE_TASKS, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("Range")
    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS;
        try (SQLiteDatabase db = this.getWritableDatabase();
             Cursor cursor = db.rawQuery(selectQuery, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Task task = new Task();
                    task.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    task.setText(cursor.getString(cursor.getColumnIndex(COLUMN_TASK_TEXT)));
                    task.setDateCreated(cursor.getString(cursor.getColumnIndex(COLUMN_DATE_CREATED)));
                    task.setTimeCreated(cursor.getString(cursor.getColumnIndex(COLUMN_TIME_CREATED)));
                    taskList.add(task);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return taskList;
    }


    public void deleteTask(Task task) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            db.delete(TABLE_TASKS, COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
