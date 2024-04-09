package com.example.diplomnaya;

public class Task {
    private int id;
    private String text;
    private String dateCreated;
    private String timeCreated;
    private String imagePath; // Поле для хранения пути к изображению

    public Task() {
        // Пустой конструктор
    }

    public Task(int id, String text, String dateCreated, String timeCreated, String imagePath) {
        this.id = id;
        this.text = text;
        this.dateCreated = dateCreated;
        this.timeCreated = timeCreated;
        this.imagePath = imagePath;
    }

    // Геттеры и сеттеры для поля imagePath
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Геттеры и сеттеры для остальных полей
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
