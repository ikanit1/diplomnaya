package com.example.diplomnaya;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String groupId;
    private String groupName;
    private String groupDescription;
    private List<String> members;
    private List<String> invitations;

    // Публичный конструктор по умолчанию
    public Group() {
        this.members = new ArrayList<>();
        this.invitations = new ArrayList<>();
    }

    // Конструктор с параметрами
    public Group(String groupId, String groupName, String groupDescription) {
        this();
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
    }

    // Геттеры и сеттеры
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public List<String> getInvitations() {
        return invitations;
    }

    public void setInvitations(List<String> invitations) {
        this.invitations = invitations;
    }

    // Методы для добавления членов и приглашений
    public void addMember(String member) {
        // Проверка, есть ли уже такой член в списке
        if (!members.contains(member)) {
            members.add(member);
        } else {
            // Опционально: Вы можете добавить логирование или вывод сообщения,
            // если член уже существует в списке
            Log.d("Group", "Член с именем " + member + " уже существует в группе.");
        }
    }


    public void addInvitation(String invitation) {
        invitations.add(invitation);
    }
}
