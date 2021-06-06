package com.example.seatpool;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;

import java.sql.Time;


public class GroupList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        GridView gridView;
        GroupListAdapter gridAdapter;

        gridAdapter = new GroupListAdapter();

        gridView = (GridView)findViewById(R.id.groupList);
        gridView.setAdapter(gridAdapter);

        //addButton
        Button addBtn = (Button)findViewById(R.id.addButton);

        addBtn.setOnClickListener(new View.OnClickListener(){
            //test
            String[] a = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
            final int per = 10;
            int i = 0;

            @Override
            public void onClick(View v){

                AlertDialog.Builder alert = new AlertDialog.Builder(GroupList.this);

                alert.setTitle("그룹 생성");
                alert.setMessage("그룹을 생성하시겠습니까?");
                alert.setNegativeButton("취소", null);
                alert.setPositiveButton("생성", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        if(i < per){
                            gridAdapter.addItem(a[i]);
                            gridView.setAdapter(gridAdapter);
                            i++;
                            //생성한 그룹으로 이동
                            Intent intent = new Intent(GroupList.this, ChatRoom.class);
                            startActivity(intent);
                        }
                        else{
                            alert.setTitle("그룹 생성 불가");
                            alert.setMessage("그룹 개수가 초과했습니다");
                            alert.setNegativeButton(null, null);
                            alert.setPositiveButton("확인", null);
                            alert.create().show();
                        }
                    }
                });

                alert.create().show();
            }
        });


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {

                ListView listview;
                GroupJoinListAdapter listAdapter;

                listAdapter = new GroupJoinListAdapter();

                LayoutInflater inflater=getLayoutInflater();

                final View dialogView= inflater.inflate(R.layout.join_dialog, null);
                AlertDialog.Builder memAlert= new AlertDialog.Builder(GroupList.this);

                listview = (ListView) dialogView.findViewById(R.id.memberList);
                listview.setAdapter(listAdapter);

                listAdapter.addItem(R.drawable.ic_baseline_face_24, "A", "20", "female");
                listAdapter.addItem(R.drawable.ic_baseline_face_24, "B", "29", "male");
                listAdapter.addItem(R.drawable.ic_baseline_face_24, "C", "22", "female");
                listview.setAdapter(listAdapter);

                memAlert.setTitle("Member Information");
                memAlert.setView(dialogView);
                memAlert.setNegativeButton("취소", null);
                memAlert.setPositiveButton("참여", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        //선택한 그룹으로 이동
                        Intent intent = new Intent(GroupList.this, ChatRoom.class);
                        startActivity(intent);
                    }
                });

                memAlert.create().show();
            }
        }) ;

    }
}