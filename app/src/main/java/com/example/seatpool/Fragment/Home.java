package com.example.seatpool.Fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.seatpool.R;
import com.example.seatpool.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Home extends Fragment {

    ViewGroup view;
    Calendar myCalendar = Calendar.getInstance();
    private TextView userName;

    Date time;

    RecyclerView recyclerView;
    RecyclerVIewAdapter adapter;
    GridLayoutManager layoutManager;
    String[] trainStation;
    TextView depPlace;
    TextView arrPlace;
    Dialog stationList;

    String strDepPlace = null;
    String strArrPlace = null;
    String strDate = null;
    String strYear = null;
    String strMonth = null;
    String strDay = null;
    String strTime = null;
    int stationIdx = 0;
    private LinearLayout homeSlogan;

    String today = null;
    long diffDay;

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
    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;
    ArrayList<String> list = new ArrayList<>();

    public interface onReceivedDataListener{
        public void onReceivedData(String[] info);
    }

    public onReceivedDataListener rListener;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof onReceivedDataListener){
            rListener = (onReceivedDataListener)context;
        }
    }

    @Override
    public void onDetach(){
        super.onDetach();
        rListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        view = (ViewGroup)inflater.inflate(R.layout.fragment_home, container, false);
        userName = view.findViewById(R.id.homeActivityUsername);
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Animation animation = new AlphaAnimation(0,1);
        animation.setDuration(2000);

        homeSlogan = view.findViewById(R.id.homeActivityMainSlogan);
        homeSlogan.setAnimation(animation);
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                    backKeyPressedTime = System.currentTimeMillis();
                    toast = Toast.makeText(getActivity(), "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                    getActivity().finish();
                    toast.cancel();
                }
            }
        });
        FirebaseDatabase.getInstance().getReference().child("Users").child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel usermodel = new UserModel();
                usermodel = snapshot.getValue(UserModel.class);
                Animation animation = new AlphaAnimation(0,1);
                animation.setDuration(1000);
                //userName.setText(usermodel.UserName);
                userName.setVisibility(View.VISIBLE);
                userName.setAnimation(animation);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //arr, dep
        arrPlace = view.findViewById(R.id.arrPlace);
        depPlace = view.findViewById(R.id.depPlace);
        stationList = new Dialog(getActivity());
        stationList.setContentView(R.layout.dialog_station_table);

        arrPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stationIdx = 0;
                showStationDialog();
            }
        });

        depPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stationIdx = 1;
                showStationDialog();
            }
        });

        //Time ComboBox
        Spinner timeSpinner = (Spinner)view.findViewById(R.id.dateTime);

        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.timeList, android.R.layout.simple_spinner_item);

        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        timeSpinner.setAdapter(timeAdapter);
        timeSpinner.setSelection(0);

        //timeSpinner Event
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                strTime = timeSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        //Date
        EditText date = (EditText)view.findViewById(R.id.datePicker);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(), myDatePicker, myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //Search Button Event
        Button searchBtn = (Button)view.findViewById(R.id.searchButton);
        searchBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                strDepPlace = depPlace.getText().toString();
                strArrPlace = arrPlace.getText().toString();

                if(!strDepPlace.equals("선택") && !strArrPlace.equals("선택") && strDate != null){
                    if(diffDay > 2){
                        strYear = strDate.substring(0,4);
                        strMonth = strDate.substring(4,6);
                        strDay = strDate.substring(6,8);

                        String[] tableInfo = {strDepPlace, strArrPlace, strMonth, strDay, strTime};
                        rListener.onReceivedData(tableInfo);
                    }
                    else{
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                        alert.setTitle("알림");
                        alert.setMessage("조회는 오늘로부터 3일 후부터 가능합니다");
                        alert.setPositiveButton("확인", null);
                        alert.create().show();
                    }
                }
                else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                    alert.setTitle("알림");
                    if(strDepPlace.equals("선택")) alert.setMessage("출발역을 선택하세요");
                    else if(strArrPlace.equals("선택")) alert.setMessage("도착역을 선택하세요");
                    else alert.setMessage("시간을 입력하세요.");
                    alert.setPositiveButton("확인", null);

                    alert.create().show();
                }
            }
        });

        return view;
    }

    //DatePickerDialog
    DatePickerDialog.OnDateSetListener myDatePicker = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }
    };

    //Date Label
    private void updateLabel() {
        time = new Date();

        String myFormat = "yyyy년  MM월  dd일";
        String dateFormat = "yyyyMMdd";

        SimpleDateFormat msg = new SimpleDateFormat(myFormat, Locale.KOREA);
        SimpleDateFormat dateMsg = new SimpleDateFormat(dateFormat, Locale.KOREA);

        EditText date = (EditText)view.findViewById(R.id.datePicker);

        date.setText(msg.format(myCalendar.getTime()));
        strDate = dateMsg.format(myCalendar.getTime());
        today = dateMsg.format(time);

        try {
            Date now = dateMsg.parse(today);
            Date expected = dateMsg.parse(strDate);

            diffDay = (expected.getTime() - now.getTime()) / (24*60*60*1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void showStationDialog(){
        recyclerView = stationList.findViewById(R.id.grid_recyclerview);
        adapter = new RecyclerVIewAdapter(getActivity(), list);

        layoutManager = new GridLayoutManager(getActivity(), 2);

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
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.string.setText(list.get(position));

            holder.string.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("log", list.get(position));
                    if(stationIdx == 0) arrPlace.setText(list.get(position));
                    else depPlace.setText(list.get(position));
                    stationList.dismiss();
                }
            });
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_station_table, parent, false);
            return new MyViewHolder(view);
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
}