package com.example.diplomnaya;

import android.content.Context;
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
    private String description;
    private String userId;
    private boolean isShared; // Новое поле для отметки, что задача поделена
    private String groupId; // Новое поле для хранения идентификатора группы

    // Конструктор
    public Task(Context context) {
        // Инициализация Firebase Database
    }

    public Task() {
        // Пустой конструктор
    }

    public Task(String title, String description, String currentUserId) {
        this.title = title;
        this.description = description;
        this.userId = currentUserId;
    }

    // Геттеры и сеттеры
    public List<Integer> getRepeatingDays() {
        return repeatingDays;
    }

    public String getRepeatingTime() {
        if (repeatingTime == null || repeatingTime.isEmpty()) {
            return "Даты нет"; // Возвращаем "Даты нет", если время не указано
        } else {
            return repeatingTime;
        }
    }

    public void setRepeatingDays(List<Integer> days) {
        this.repeatingDays = days;
    }

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

    public void setShared(boolean isShared) {
        this.isShared = isShared;
    }


    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
