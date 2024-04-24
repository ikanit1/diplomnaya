package com.example.diplomnaya;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class Task {
    private String id;
    private String text;
    private String title;
    private String dateCreated;
    private String timeCreated;
    private boolean notify;
    private boolean isRepeating;
    private boolean important;
    private String creationTime;
    private List<Integer> repeatingDays;
    private String repeatingTime;
    // Поле для создателя задачи
    private String creator;

    // Поле для времени создания задачи
    private long createdAt;

    // Ссылка на Firebase Database
    private DatabaseReference databaseReference;

    // Конструктор
    public Task(Context context) {
        // Инициализация Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public Task() {
        // Пустой конструктор
    }

    public List<Integer> getRepeatingDays() {
        return repeatingDays;
    }

    // Сеттер для установки создателя задачи
    public void setCreator(String creator) {
        this.creator = creator;
    }

    // Геттер для получения создателя задачи
    public String getCreator() {
        return creator;
    }

    // Сеттер для установки времени создания задачи
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Геттер для получения времени создания задачи
    public long getCreatedAt() {
        return createdAt;
    }

    // Геттер для времени повторения (в формате HH:mm)
    public String getRepeatingTime() {
        return repeatingTime;
    }
    // Сеттер для установки списка повторяющихся дней недели
    public void setRepeatingDays(List<Integer> days) {
        this.repeatingDays = days;
    }

    // Сеттер для установки времени повторения (в формате HH:mm)
    public void setRepeatingTime(String time) {
        this.repeatingTime = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    public void setRepeating(boolean isRepeating) {
        this.isRepeating = isRepeating;
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    // Функция для добавления задачи в группу
    // Метод для добавления задачи в группу
    public void addTaskToGroup(Context context, String groupId, Task task) {
        DatabaseReference groupTasksRef = databaseReference.child("groups").child(groupId).child("tasks");
        String taskId = groupTasksRef.push().getKey(); // Генерация уникального идентификатора задачи

        task.setId(taskId); // Установите идентификатор задачи в объекте задачи
        groupTasksRef.child(taskId).setValue(task)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Задача добавлена", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Ошибка при добавлении задачи: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
