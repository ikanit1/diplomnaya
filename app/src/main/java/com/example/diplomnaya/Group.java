package com.example.diplomnaya;

import java.util.HashMap;
import java.util.Map;

public class Group {
    private String groupCode;
    private String groupName;
    private String groupId;
    private String creatorId;
    private Map<String, Boolean> members = new HashMap<>();

    public Group() {
        // Default constructor required for calls to DataSnapshot.getValue(Group.class)
    }

    public Group(String groupCode, String groupName,  String creatorId) {
        this.groupCode = groupCode;
        this.groupName = groupName;
        this.creatorId = creatorId;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public Map<String, Boolean> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Boolean> members) {
        this.members = members;
    }

    public void addMember(String memberId) {
        this.members.put(memberId, true);
    }

    public String getGroupId() {
        return groupId;
    }
}
