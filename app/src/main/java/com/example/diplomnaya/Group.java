package com.example.diplomnaya;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    public Group(String groupCode, String groupName, String creatorId) {
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

    // Метод для получения имени создателя
    public void getCreatorName(CompletionListener<String> listener) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(creatorId).child("name");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String creatorName = dataSnapshot.getValue(String.class);
                    // Передаем имя создателя через обратный вызов
                    listener.onSuccess(creatorName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок
                listener.onFailure(databaseError.toException());
            }
        });
    }

    // Метод для получения имени пользователя по его идентификатору
    public void getUserName(String userId, UserNameListener listener) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("name");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.getValue(String.class);
                    // Передаем имя пользователя через обратный вызов
                    listener.onUserNameReceived(userName);
                } else {
                    // Если данные не найдены, вызываем метод с пустым именем
                    listener.onUserNameReceived("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок
                listener.onUserNameError(databaseError.toException());
            }
        });
    }

    // Интерфейс обратного вызова для получения результата асинхронной операции
    public interface CompletionListener<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    // Интерфейс для получения имени пользователя
    public interface UserNameListener {
        void onUserNameReceived(String userName);
        void onUserNameError(Exception e);
    }
}
