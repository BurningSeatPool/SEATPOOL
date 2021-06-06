package com.example.seatpool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class GroupJoinListAdapter extends BaseAdapter {
    private final ArrayList<GroupJoinListView> GroupJoinListViewList = new ArrayList<GroupJoinListView>() ;

    public GroupJoinListAdapter() {

    }

    @Override
    public int getCount() {
        return GroupJoinListViewList.size() ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.join_dialog_listview, parent, false);
        }

        ImageView imgImageView= (ImageView)convertView.findViewById(R.id.img);
        TextView nameTextView = (TextView)convertView.findViewById(R.id.name);
        TextView ageTextView = (TextView)convertView.findViewById(R.id.age);
        TextView genderTextView = (TextView)convertView.findViewById(R.id.gender);

        GroupJoinListView listViewItem = GroupJoinListViewList.get(position);

        imgImageView.setImageResource(listViewItem.getImg());
        nameTextView.setText(listViewItem.getName());
        ageTextView.setText(listViewItem.getAge());
        genderTextView.setText(listViewItem.getGender());

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public Object getItem(int position) {
        return GroupJoinListViewList.get(position) ;
    }

    public void addItem(int img, String name, String age, String gender) {
        GroupJoinListView item = new GroupJoinListView();

        item.setImage(img);
        item.setName(name);
        item.setAge(age);
        item.setGender(gender);

        GroupJoinListViewList.add(item);
    }
}
