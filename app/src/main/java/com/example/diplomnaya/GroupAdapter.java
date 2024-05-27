package com.example.diplomnaya;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class GroupAdapter extends ArrayAdapter<Group> {
    private Context context;
    private List<Group> groups;

    public GroupAdapter(Context context, List<Group> groups) {
        super(context, 0, groups);
        this.context = context;
        this.groups = groups;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        }

        Group group = groups.get(position);
        TextView groupName = convertView.findViewById(R.id.groupName);
        TextView groupCode = convertView.findViewById(R.id.groupCode);
        Button viewMembersButton = convertView.findViewById(R.id.viewMembersButton);
        Button deleteGroupButton = convertView.findViewById(R.id.deleteGroupButton);

        groupName.setText(group.getGroupName());
        groupCode.setText("Код: " + group.getGroupCode());

        // Получаем имя создателя асинхронно и устанавливаем его после получения


        viewMembersButton.setOnClickListener(v -> viewMembers(group));

        // Обработчик кнопки удаления группы
        deleteGroupButton.setOnClickListener(v -> {
            if (currentUserIsCreator(group)) {
                new AlertDialog.Builder(context)
                        .setTitle("Удалить группу")
                        .setMessage("Вы уверены, что хотите удалить эту группу?")
                        .setPositiveButton("Да", (dialog, which) -> deleteGroup(group))
                        .setNegativeButton("Нет", null)
                        .show();
            } else {
                Toast.makeText(context, "У вас нет прав для удаления этой группы", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    // Метод для проверки, является ли текущий пользователь создателем группы
    private boolean currentUserIsCreator(Group group) {
        // Получаем текущего пользователя из FirebaseAuth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Проверяем, не пуст ли текущий пользователь и совпадает ли его UID с UID создателя группы
        return currentUser != null && currentUser.getUid().equals(group.getCreatorId());
    }

    // В методе deleteGroup после успешного удаления группы
    private void deleteGroup(Group group) {
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference()
                .child("groups").child(group.getGroupCode());
        groupRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Remove the group from the user's groups list
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    DatabaseReference userGroupsRef = FirebaseDatabase.getInstance().getReference()
                            .child("users").child(currentUser.getUid()).child("groups").child(group.getGroupCode());
                    userGroupsRef.removeValue();
                }

                // Удаление участников группы
                deleteGroupMembers(group);

                Toast.makeText(context, "Группа удалена", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged(); // Обновить список после удаления
            } else {
                Toast.makeText(context, "Ошибка удаления группы", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Метод для удаления участников группы из базы данных Firebase
    private void deleteGroupMembers(Group group) {
        DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference()
                .child("groups").child(group.getGroupCode()).child("members");
        membersRef.removeValue();
    }

    private void viewMembers(Group group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Участники группы: " + group.getGroupName());

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_view_members, null);
        TextView textViewMemberCount = view.findViewById(R.id.textViewMemberCount);

        // Получаем количество участников из базы данных Firebase
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference()
                .child("groups").child(group.getGroupCode()).child("members");
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long memberCount = snapshot.getChildrenCount();
                textViewMemberCount.setText("Количество участников: " + memberCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Ошибка получения данных", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(view);
        builder.setPositiveButton("ОК", null);
        builder.show();
    }
}
