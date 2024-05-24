package com.example.diplomnaya;

import java.util.HashMap;
import java.util.Map;

public class Group {
    private String groupCode;
    private String groupName;
    private String groupId;
    private String groupDescription;
    private String creatorId;
    private Map<String, Boolean> members = new HashMap<>();

    public Group() {
        // Default constructor required for calls to DataSnapshot.getValue(Group.class)
    }

    public Group(String groupCode, String groupName, String groupDescription, String creatorId) {
        this.groupCode = groupCode;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
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

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
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
