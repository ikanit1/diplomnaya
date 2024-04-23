package com.example.diplomnaya;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class GroupAdapter extends BaseAdapter {
    private Context context;
    private List<Group> groups;
    private LayoutInflater inflater;

    public GroupAdapter(Context context, List<Group> groups) {
        this.context = context;
        this.groups = groups;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return groups.size();
    }

    @Override
    public Group getItem(int position) {
        return groups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Создание представления (View) для элемента списка группы
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_group, parent, false);
        }

        // Получение текущей группы из списка
        Group group = getItem(position);

        // Привязка данных группы к элементам представления
        TextView textGroupName = convertView.findViewById(R.id.textGroupName);
        textGroupName.setText(group.getGroupName());

        TextView textGroupDescription = convertView.findViewById(R.id.textGroupDescription);
        textGroupDescription.setText(group.getGroupDescription());

        // Добавьте дополнительные элементы представления и привязку данных, если необходимо

        return convertView;
    }
}
