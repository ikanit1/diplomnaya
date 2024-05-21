package com.example.diplomnaya;

import android.content.Context;

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
    private DatabaseReference databaseReference;
    private String description;
    private String userId;

    // Конструктор
    public Task(Context context) {
        // Инициализация Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public Task() {
        // Пустой конструктор
    }

    public Task(String title, String description, String currentUserId) {
        this.title = title;
        this.description = description;
        this.userId = currentUserId;
    }

    public List<Integer> getRepeatingDays() {
        return repeatingDays;
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

    public String getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }
}
