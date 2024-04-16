package com.example.diplomnaya;

public class Task {
    private int id;
    private String text;
    private String title;
    private String dateCreated;
    private String timeCreated;
    private boolean notify;
    private boolean is_repeating;
    private boolean important;

    // Добавляем поле для хранения времени создания задачи
    private String creationTime;

    public Task() {
        // Пустой конструктор
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public boolean isRepeating() {
        return is_repeating;
    }

    public void setRepeating(boolean is_repeating) {
        this.is_repeating = is_repeating;
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    // Метод для получения времени создания задачи
    public String getCreationTime() {
        return creationTime;
    }

    // Метод для установки времени создания задачи
    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }
}
