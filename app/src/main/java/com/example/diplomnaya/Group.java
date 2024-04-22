package com.example.diplomnaya;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String groupId;
    private String groupName;
    private String groupDescription;
    private List<String> members;
    private List<String> invitations;

    public Group(String groupId, String groupName, String groupDescription) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.members = new ArrayList<>();
        this.invitations = new ArrayList<>();
    }

    // Добавление участника
    public void addMember(String memberId) {
        members.add(memberId);
    }

    // Добавление приглашения
    public void addInvitation(String email) {
        invitations.add(email);
    }

    // Геттеры и сеттеры
    // ...
}

