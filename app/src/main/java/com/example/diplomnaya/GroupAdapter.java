package com.example.diplomnaya;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
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

    private void viewMembers(Group group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Участники группы: " + group.getGroupName());

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_view_members, null);
        ListView listViewMembers = view.findViewById(R.id.listViewMembers);
        List<String> memberList = new ArrayList<>(group.getMembers().keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, memberList);
        listViewMembers.setAdapter(adapter);

        // Проверяем, является ли текущий пользователь создателем группы
        if (currentUserIsCreator(group)) {
            listViewMembers.setOnItemClickListener((parent, view1, position, id) -> {
                String memberId = memberList.get(position);
                new AlertDialog.Builder(context)
                        .setTitle("Удалить участника")
                        .setMessage("Вы уверены, что хотите удалить этого участника?")
                        .setPositiveButton("Да", (dialog, which) -> removeMember(group.getGroupCode(), memberId))
                        .setNegativeButton("Нет", null)
                        .show();
            });
        }

        builder.setView(view);
        builder.setPositiveButton("ОК", null);
        builder.show();
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
                Toast.makeText(context, "Группа удалена", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged(); // Обновить список после удаления
            } else {
                Toast.makeText(context, "Ошибка удаления группы", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeMember(String groupCode, String memberId) {
        DatabaseReference memberRef = FirebaseDatabase.getInstance().getReference()
                .child("groups").child(groupCode).child("members").child(memberId);
        memberRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Участник удален", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged(); // Обновить список после удаления
            } else {
                Toast.makeText(context, "Ошибка удаления участника", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
