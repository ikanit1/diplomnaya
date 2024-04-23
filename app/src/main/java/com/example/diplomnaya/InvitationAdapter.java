package com.example.diplomnaya;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class InvitationAdapter extends BaseAdapter {
    private Context context;
    private List<Invitation> invitations;
    private LayoutInflater inflater;

    public InvitationAdapter(Context context, List<Invitation> invitations) {
        this.context = context;
        this.invitations = invitations;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return invitations.size();
    }

    @Override
    public Invitation getItem(int position) {
        return invitations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_invitation, parent, false);
        }

        // Получите текущее приглашение
        Invitation invitation = getItem(position);

        // Заполните элементы интерфейса данными приглашения
        TextView textGroupName = convertView.findViewById(R.id.textGroupName);
        TextView textEmail = convertView.findViewById(R.id.textEmail);
        Button btnAccept = convertView.findViewById(R.id.btnAccept);
        Button btnReject = convertView.findViewById(R.id.btnReject);

        textGroupName.setText(invitation.getGroupId()); // Или получите имя группы из базы данных
        textEmail.setText(invitation.getEmail());

        // Установка обработчиков нажатия для кнопок
        btnAccept.setOnClickListener(view -> {
            // Обработка принятия приглашения
            invitation.setAccepted(true);
            // Обновите статус приглашения в базе данных
            updateInvitationStatus(invitation);
        });

        btnReject.setOnClickListener(view -> {
            // Обработка отклонения приглашения
            invitation.setAccepted(false);
            // Удалите приглашение из базы данных
            deleteInvitation(invitation);
        });

        return convertView;
    }

    // Метод для обновления статуса приглашения в базе данных
    private void updateInvitationStatus(Invitation invitation) {
        // Реализуйте логику обновления статуса приглашения в базе данных
        Toast.makeText(context, "Приглашение принято", Toast.LENGTH_SHORT).show();
    }

    // Метод для удаления приглашения из базы данных
    private void deleteInvitation(Invitation invitation) {
        // Реализуйте логику удаления приглашения из базы данных
        Toast.makeText(context, "Приглашение отклонено", Toast.LENGTH_SHORT).show();
    }
}
