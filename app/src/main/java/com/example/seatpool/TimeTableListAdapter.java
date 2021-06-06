package com.example.seatpool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TimeTableListAdapter extends BaseAdapter {

    private final ArrayList<TimeTableListView> TimeTableListViewList = new ArrayList<TimeTableListView>() ;

    public TimeTableListAdapter() {

    }

    @Override
    public int getCount() {
        return TimeTableListViewList.size() ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.time_table_listview, parent, false);
        }

        TextView depStationTextView = (TextView) convertView.findViewById(R.id.depStation);
        TextView arrStationTextView = (TextView) convertView.findViewById(R.id.arrStation);
        TextView depTimeTextView = (TextView) convertView.findViewById(R.id.depTime);
        TextView arrTimeTextView = (TextView) convertView.findViewById(R.id.arrTime);
        TextView chargeTextView = (TextView) convertView.findViewById(R.id.charge);
        TextView saleTextView = (TextView) convertView.findViewById(R.id.sale);


        TimeTableListView listViewItem = TimeTableListViewList.get(position);

        depStationTextView.setText(listViewItem.getDepStation());
        arrStationTextView.setText(listViewItem.getArrStation());
        depTimeTextView.setText(listViewItem.getDepTime());
        arrTimeTextView.setText(listViewItem.getArrTime());
        chargeTextView.setText(listViewItem.getCharge());
        saleTextView.setText(listViewItem.getSale());

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public Object getItem(int position) {
        return TimeTableListViewList.get(position) ;
    }

    public void addItem(String depS, String arrS, String depT, String arrT, String cost, String saleRate) {
        TimeTableListView item = new TimeTableListView();

        item.setDepStation(depS);
        item.setArrStation(arrS);
        item.setDepTime(depT);
        item.setArrTime(arrT);
        item.setCharge(cost);
        item.setSale(saleRate);

        TimeTableListViewList.add(item);
    }
}
