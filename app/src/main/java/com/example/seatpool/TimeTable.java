package com.example.seatpool;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.seatpool.chatFragment.TicketGroupChatActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TimeTable extends AppCompatActivity {

    TimeTableListAdapter adapter;
    ListView listview;

    String TAG = null;
    String userDep = null;
    String userArr = null;
    String userMonth = null;
    String userDay = null;
    String userTime = null;
    String count = null;
    String trainNo = null;
    String depName = null;
    String arrName = null;
    String depTime = null;
    String arrTime = null;
    String charge = null;
    String sale = null;
    String table = null;

    Call<String> ktxCountCall;
    Call<String> ktxSearchCall;
    Call<List<TimeTablePojo>> ktxTimeTableCall;

    RetrofitInterface api;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);




        //입력한 열차조회 정보
        Intent intent = getIntent();
        String[] tableInfo = intent.getStringArrayExtra("String[]");
        userDep = tableInfo[0];
        userArr = tableInfo[1];
        userMonth = tableInfo[2];
        userDay = tableInfo[3];
        userTime = tableInfo[4];

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RetrofitInterface.BASEURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();

        api = retrofit.create(RetrofitInterface.class);

        //ktx/get/count interface
        ktxCountCall = api.getCount(userMonth, userDay, userTime, userDep, userArr);
        ktxCountCall.enqueue(new retrofit2.Callback<String>() {

            @Override
            public void onResponse(retrofit2.Call<String> call, retrofit2.Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {

                    progressDialog = new ProgressDialog(TimeTable.this);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("잠시만 기다려 주세요");
                    progressDialog.show();

                    count = response.body();

                    if(count.equals("0")){
                        getKtxSearch();
                    }
                    else getTimeTable();

                    Log.e(TAG, "ktxCount : " + response.body());
                }
                else Log.e(TAG, "ktxCount err");
            }

            @Override
            public void onFailure(retrofit2.Call<String> call, Throwable t) {
                Log.e(TAG, "ktxCount Fail");
            }
        });
    }


    public void getKtxSearch(){
                    //ktx/search interface
        Log.e("ss", userMonth +" "+ userDay);


        ktxSearchCall = api.getKtx(userMonth, userDay, userTime, userDep, userArr);
        ktxSearchCall.enqueue(new retrofit2.Callback<String>() {

            @Override
            public void onResponse(retrofit2.Call<String> call, retrofit2.Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body();
                    Log.e(TAG, jsonResponse);
                    getTimeTable();
                }
                else Log.e(TAG, "ktxSearchErr");
            }

            @Override
            public void onFailure(retrofit2.Call<String> call, Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
            }
        });
    }

    public void getTimeTable(){
        //ktx/get/timetable interface
        ktxTimeTableCall = api.getKtxTimeTable(userMonth, userDay, userTime, userDep, userArr);
        ktxTimeTableCall.enqueue(new retrofit2.Callback<List<TimeTablePojo>>() {

            @Override
            public void onResponse(retrofit2.Call<List<TimeTablePojo>> call, retrofit2.Response<List<TimeTablePojo>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    adapter = new TimeTableListAdapter();;

                    listview = (ListView) findViewById(R.id.tableList);
                    listview.setAdapter(adapter);

                    table = response.body().toString();
                    StringTokenizer tableParse = new StringTokenizer(table, "/,[]");
                    while(tableParse.hasMoreTokens()){
                        trainNo = tableParse.nextToken();
                        depName = tableParse.nextToken();
                        arrName = tableParse.nextToken();
                        depTime = tableParse.nextToken();
                        arrTime = tableParse.nextToken();
                        charge = tableParse.nextToken();
                        sale = tableParse.nextToken();

                        adapter.addItem(depName, arrName, depTime, arrTime, charge, sale);
                    }

                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView parent, View v, int position, long id) {

                            Intent intent = new Intent(TimeTable.this, TicketGroupChatActivity.class);
                            Object vo = (Object)parent.getAdapter().getItem(position);
                            int i = 0;
                            String[] arr = new String[6];

                            try {
                                for (Field field : vo.getClass().getDeclaredFields()) {
                                    field.setAccessible(true);
                                    Object value = field.get(vo);
                                    arr[i] = value.toString();
                                    i++;
                                }

                                String chatName = userMonth + userDay + arr[3] + arr[4]
                                        + arr[0] + arr[1];
                                Log.e("tag", chatName);

                                intent.putExtra("chatName", chatName);
                                intent.putExtra("charge", arr[2]);
                                intent.putExtra("userMonth",userMonth);
                                intent.putExtra("userDay",userDay);
                                startActivity(intent);

                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                            // Intent intent = new Intent(TimeTable.this, GroupList.class);
                           // startActivity(intent);
                        }
                    });
                    progressDialog.dismiss();

                    Log.e(TAG, "ktxTimeTable success");
                }
                else Log.e(TAG, "ktxTimeTable err");
            }

            @Override
            public void onFailure(retrofit2.Call<List<TimeTablePojo>> call, Throwable t) {
                Log.e(TAG, "ktxTimeTable Fail");
            }
        });
    }
}