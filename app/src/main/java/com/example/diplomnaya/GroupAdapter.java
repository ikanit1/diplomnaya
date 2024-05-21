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
        Button viewMembersButton = convertView.findViewById(R.id.viewMembersButton);

        groupName.setText(group.getGroupName());
        viewMembersButton.setOnClickListener(v -> viewMembers(group));

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

        listViewMembers.setOnItemClickListener((parent, view1, position, id) -> {
            String memberId = memberList.get(position);
            new AlertDialog.Builder(context)
                    .setTitle("Удалить участника")
                    .setMessage("Вы уверены, что хотите удалить этого участника?")
                    .setPositiveButton("Да", (dialog, which) -> removeMember(group.getGroupCode(), memberId))
                    .setNegativeButton("Нет", null)
                    .show();
        });

        builder.setView(view);
        builder.setPositiveButton("ОК", null);
        builder.show();
    }


    private void removeMember(String groupCode, String memberId) {
        DatabaseReference memberRef = FirebaseDatabase.getInstance().getReference()
                .child("groups").child(groupCode).child("members").child(memberId);
        memberRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Участник удален", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Ошибка удаления участника", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
