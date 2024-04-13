package com.example.diplomnaya;

public class Task {
    private int id;
    private String text;
    private String dateCreated;
    private String timeCreated;
    private boolean notify;
    private boolean is_repeating;
    private boolean important;

    public Task() {
        // Пустой конструктор
    }

    public Task(int id, String text, String dateCreated, String timeCreated) {
        this.id = id;
        this.text = text;
        this.dateCreated = dateCreated;
        this.timeCreated = timeCreated;
    }

    public Task(int id, String text, String dateCreated, String timeCreated, boolean important) {
        this.id = id;
        this.text = text;
        this.dateCreated = dateCreated;
        this.timeCreated = timeCreated;
        this.important = important;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    // Геттер и сеттер для repeating
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
}
