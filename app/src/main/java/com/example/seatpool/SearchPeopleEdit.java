package com.example.seatpool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.seatpool.Fragment.Home;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SearchPeopleEdit extends AppCompatActivity {
    String title;
    String depart;
    String content;
    String arrival;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    EditText sp_et_title;
    EditText sp_et_content;
    TextView sp_et_depart;
    TextView sp_et_arrival;
    public String timestamp;
    String myUid;
    String now;
    int commentCount;
    int reportCount;
    int num;
    int stationIdx;

    RecyclerView recyclerView;
    RecyclerVIewAdapter adapter;
    GridLayoutManager layoutManager;
    String[] trainStation;
    Dialog stationList;

    TextView primary;
    TextView ga;
    TextView na;
    TextView da;
    TextView ra;
    TextView ma;
    TextView ba;
    TextView sa;
    TextView aa;
    TextView za;
    TextView cha;
    TextView ka;
    TextView ta;
    TextView pa;
    TextView ha;

    ArrayList<String> list = new ArrayList<>();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    Date time = new Date();

    String image = "https://firebasestorage.googleapis.com/v0/b/seat-34957.appspot.com/o/boardimage400400.jpg?alt=media&token=6cc88ba8-f219-4ddf-b351-a37551b7fdd5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_people_edit);
        findViewById(R.id.sp_btn_cancle).setOnClickListener(onClickListener);
        findViewById(R.id.sp_btn_confirm).setOnClickListener(onClickListener);
        sp_et_title = (EditText) findViewById(R.id.sp_et_title);
        sp_et_content = (EditText) findViewById(R.id.sp_et_content);
        sp_et_depart = (TextView) findViewById(R.id.sp_et_depart);
        sp_et_arrival = (TextView) findViewById(R.id.sp_et_arrival);
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Post");

        stationList = new Dialog(SearchPeopleEdit.this);
        stationList.setContentView(R.layout.dialog_station_table);

        sp_et_arrival.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stationIdx = 0;
                showStationDialog();
            }
        });

        sp_et_depart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stationIdx = 1;
                showStationDialog();
            }
        });
    }

    public void showStationDialog(){
        recyclerView = stationList.findViewById(R.id.grid_recyclerview);
        adapter = new RecyclerVIewAdapter(SearchPeopleEdit.this, list);

        layoutManager = new GridLayoutManager(SearchPeopleEdit.this, 2);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        trainStation = getResources().getStringArray(R.array.initial);
        getStation();
        stationList.show();

        primary = stationList.findViewById(R.id.primary);
        ga = stationList.findViewById(R.id.ga);
        na = stationList.findViewById(R.id.na);
        da = stationList.findViewById(R.id.da);
        ra = stationList.findViewById(R.id.ra);
        ma = stationList.findViewById(R.id.ma);
        ba = stationList.findViewById(R.id.ba);
        sa = stationList.findViewById(R.id.sa);
        aa = stationList.findViewById(R.id.aa);
        za = stationList.findViewById(R.id.za);
        cha = stationList.findViewById(R.id.cha);
        ka = stationList.findViewById(R.id.ka);
        ta = stationList.findViewById(R.id.ta);
        pa = stationList.findViewById(R.id.pa);
        ha = stationList.findViewById(R.id.ha);


        primary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trainStation = getResources().getStringArray(R.array.primary_station);
                getStation();
            }
        });

        ga.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                trainStation = getResources().getStringArray(R.array.ga_station);
                getStation();
            }
        });

        na.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                trainStation = getResources().getStringArray(R.array.na_station);
                getStation();
            }
        });

        da.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                trainStation = getResources().getStringArray(R.array.da_station);
                getStation();
            }
        });

        ra.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                trainStation = getResources().getStringArray(R.array.ra_station);
                getStation();
            }
        });

        ma.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                trainStation = getResources().getStringArray(R.array.ma_station);
                getStation();
            }
        });

        ba.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                trainStation = getResources().getStringArray(R.array.ba_station);
                getStation();
            }
        });

        sa.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                trainStation = getResources().getStringArray(R.array.sa_station);
                getStation();
            }
        });

        aa.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                trainStation = getResources().getStringArray(R.array.aa_station);
                getStation();
            }
        });

        za.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                trainStation = getResources().getStringArray(R.array.za_station);
                getStation();
            }
        });

        cha.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                trainStation = getResources().getStringArray(R.array.cha_station);
                getStation();
            }
        });

        ka.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                trainStation = getResources().getStringArray(R.array.ka_station);
                getStation();
            }
        });

        ta.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                trainStation = getResources().getStringArray(R.array.ta_station);
                getStation();
            }
        });

        pa.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                trainStation = getResources().getStringArray(R.array.pa_station);
                getStation();
            }
        });

        ha.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                trainStation = getResources().getStringArray(R.array.ha_station);
                getStation();
            }
        });
    }

    public void getStation(){
        list.clear();
        list.addAll(Arrays.asList(trainStation));
        if(list.size() == 0){
            trainStation = getResources().getStringArray(R.array.initial);
            list.addAll(Arrays.asList(trainStation));
        }
        adapter.notifyDataSetChanged();
    }

    public class RecyclerVIewAdapter extends RecyclerView.Adapter<RecyclerVIewAdapter.MyViewHolder>{

        Context context;
        ArrayList<String> list;

        public RecyclerVIewAdapter(Context context, ArrayList<String> list) {
            super();
            this.context = context;
            this.list = list;
        }

        @Override
        public void onBindViewHolder(RecyclerVIewAdapter.MyViewHolder holder, int position) {
            holder.string.setText(list.get(position));

            holder.string.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("log", list.get(position));
                    if(stationIdx == 0) sp_et_arrival.setText(list.get(position));
                    else sp_et_depart.setText(list.get(position));
                    stationList.dismiss();
                }
            });
        }

        @Override
        public RecyclerVIewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_station_table, parent, false);
            return new RecyclerVIewAdapter.MyViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{
            TextView string;

            public MyViewHolder(View itemView) {
                super(itemView);
                string = itemView.findViewById(R.id.recylcerview_station);
            }
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.sp_btn_confirm:
                    now = simpleDateFormat.format(time);
                    spUpload();

                    break;
                case R.id.sp_btn_cancle:

                    finish();
                    break;
            }
        }
    };

    private void spUpload(){

        ArrayList<Post> arrayList = new ArrayList<>();
        title = sp_et_title.getText().toString();
        depart = sp_et_depart.getText().toString();
        content = sp_et_content.getText().toString();
        arrival = sp_et_arrival.getText().toString();
        timestamp = now;
        commentCount = 0;
        reportCount = 0;
        num = 0;

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    num++;
                }
                Post post = new Post(image,title,depart,arrival,content,timestamp,myUid, num, commentCount, reportCount);

                database = FirebaseDatabase.getInstance(); // 파이어베이스 db 연동
                databaseReference = database.getReference("Post"); // db 테이블 연결

                DatabaseReference infoRef = databaseReference;
                infoRef.push().setValue(post);

                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

    }
}