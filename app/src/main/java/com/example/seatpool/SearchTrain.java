package com.example.seatpool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.seatpool.Fragment.Board;
import com.example.seatpool.Fragment.Home;
import com.example.seatpool.Fragment.My;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class SearchTrain extends AppCompatActivity implements Home.onReceivedDataListener {

    BottomNavigationView bottomNavigationView;
    Home home;
    Board board;
    My my;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_train);

        bottomNavigationView = findViewById(R.id.bottomNav);

        home = new Home();
        board = new Board();
        my = new My();

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, home).commitAllowingStateLoss();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.navigation_home:{
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, home).commitAllowingStateLoss();

                        return true;
                    }
                    case R.id.navigation_board:{
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, board).commitAllowingStateLoss();

                        return true;
                    }
                    case R.id.navigation_my:{
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, my).commitAllowingStateLoss();

                        return true;
                    }
                    default: return false;
                }
            }
        });

        passPushTokenToServer();/*
        getFragmentManager().beginTransaction().replace(R.id.main_activity_framelayout,fragment).commit();*/
    }

    @Override
    public void onReceivedData(String[] tableInfo){
        Intent intent = new Intent(SearchTrain.this, TimeTable.class);
        intent.putExtra("String[]", (Serializable) tableInfo);
        startActivity(intent);
    }

    void passPushTokenToServer(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();

        Map<String,Object> map = new HashMap<>();
        map.put("pushToken", token);

        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).updateChildren(map);

    }

}