package com.example.diplomnaya;

public class Invitation {
    private String id;
    private String groupId; // Идентификатор группы
    private String email;
    private boolean accepted;

    // Конструктор по умолчанию
    public Invitation() {
        // Пустой конструктор
    }

    // Конструктор с параметрами
    public Invitation(String id, String groupId, String email, boolean accepted) {
        this.id = id;
        this.groupId = groupId;
        this.email = email;
        this.accepted = accepted;
    }

    // Геттеры и сеттеры для полей
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}




