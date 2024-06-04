package com.example.diplomnaya;

import android.content.ClipboardManager;
import android.content.ClipData;
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
        Button leaveGroupButton = convertView.findViewById(R.id.leaveGroupButton);

        groupName.setText(group.getGroupName());
        groupCode.setText("Код: " + group.getGroupCode());

        // Hide all action buttons initially
        deleteGroupButton.setVisibility(View.GONE);
        leaveGroupButton.setVisibility(View.GONE);

        if (currentUserIsCreator(group)) {
            deleteGroupButton.setVisibility(View.VISIBLE);
            deleteGroupButton.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Удалить группу")
                        .setMessage("Вы уверены, что хотите удалить эту группу?")
                        .setPositiveButton("Да", (dialog, which) -> deleteGroup(group))
                        .setNegativeButton("Нет", null)
                        .show();
            });
        } else {
            leaveGroupButton.setVisibility(View.VISIBLE);
            leaveGroupButton.setOnClickListener(v -> leaveGroup(group));
        }

        viewMembersButton.setOnClickListener(v -> viewMembers(group));

        groupCode.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Group Code", group.getGroupCode());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Код группы скопирован в буфер обмена", Toast.LENGTH_SHORT).show();
        });

        return convertView;
    }

    private boolean currentUserIsCreator(Group group) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null && currentUser.getUid().equals(group.getCreatorId());
    }

    private void deleteGroup(Group group) {
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference()
                .child("groups").child(group.getGroupCode());

        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot memberSnapshot : dataSnapshot.child("members").getChildren()) {
                        String memberId = memberSnapshot.getKey();
                        DatabaseReference userGroupRef = FirebaseDatabase.getInstance().getReference()
                                .child("users").child(memberId).child("groups").child(group.getGroupCode());
                        userGroupRef.removeValue();
                    }
                    // Уменьшение счетчика групп для текущего пользователя
                    decreaseGroupCount();
                    groupRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Группа удалена", Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(context, "Ошибка удаления группы", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Ошибка при удалении группы", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void decreaseGroupCount() {
        DatabaseReference userGroupCountRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("groupCount");
        userGroupCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long groupCount = (long) dataSnapshot.getValue();
                    if (groupCount > 0) {
                        userGroupCountRef.setValue(groupCount - 1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Ошибка при обновлении счетчика групп", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void leaveGroup(Group group) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userGroupRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(currentUser.getUid()).child("groups").child(group.getGroupCode());
            userGroupRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DatabaseReference memberRef = FirebaseDatabase.getInstance().getReference()
                            .child("groups").child(group.getGroupCode()).child("members").child(currentUser.getUid());
                    memberRef.removeValue();
                    Toast.makeText(context, "Вы покинули группу", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "Ошибка при выходе из группы", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void viewMembers(Group group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Участники группы: " + group.getGroupName());

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_view_members, null);
        TextView textViewMemberCount = view.findViewById(R.id.textViewMemberCount);

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
