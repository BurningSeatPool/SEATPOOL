package com.example.seatpool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class GroupListAdapter extends BaseAdapter {

    private final ArrayList<GroupListView> GroupListViewList = new ArrayList<GroupListView>() ;

    public GroupListAdapter() {

    }

    @Override
    public int getCount() {
        return GroupListViewList.size() ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_listview, parent, false);
        }

        TextView groupNumTextView = (TextView)convertView.findViewById(R.id.groupNum);
        GroupListView gridViewItem = GroupListViewList.get(position);
        groupNumTextView.setText(gridViewItem.getGroupNum());

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public Object getItem(int position) {
        return GroupListViewList.get(position) ;
    }

    public void addItem(String groupN) {
        GroupListView item = new GroupListView();

        item.setGroupNum(groupN);

        GroupListViewList.add(item);
    }
}
