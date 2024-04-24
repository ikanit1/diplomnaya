package com.example.diplomnaya;

public class Invitation {
    private String groupId;
    private String id; // Уникальный идентификатор приглашения
    private String sender;
    private String email;
    private boolean accepted; // Поле для хранения статуса принятия

    // Метод для установки статуса принятия приглашения
    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    // Метод для получения статуса принятия приглашения
    public boolean isAccepted() {
        return accepted;
    }

    public Invitation(String groupId, String sender, String email) {
        this.groupId = groupId;
        this.sender = sender;
        this.email = email;
    }

    // Геттеры и сеттеры для всех полей
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    // Метод для получения идентификатора приглашения
    public String getId() {
        return id;
    }

    // Метод для установки идентификатора приглашения
    public void setId(String id) {
        this.id = id;
    }
}
