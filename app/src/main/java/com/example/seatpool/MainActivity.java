package com.example.seatpool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.seatpool.chatFragment.AccountFragment;
import com.example.seatpool.chatFragment.ChatFragment;
import com.example.seatpool.chatFragment.PeopleFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Fragment fragment = new PeopleFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.main_activity_bottom_navigation_view);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_people:
                        getFragmentManager().beginTransaction().replace(R.id.main_activity_framelayout,new PeopleFragment()).commit();
                        return true;
                    case R.id.action_chat:
                        getFragmentManager().beginTransaction().replace(R.id.main_activity_framelayout,new ChatFragment()).commit();
                        return true;
                    case R.id.action_account:
                        getFragmentManager().beginTransaction().replace(R.id.main_activity_framelayout,new AccountFragment()).commit();
                        return true;
                }
                return false;
            }
        });

        passPushTokenToServer();
        getFragmentManager().beginTransaction().replace(R.id.main_activity_framelayout,fragment).commit();
    }
    void passPushTokenToServer(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();

        Map<String,Object> map = new HashMap<>();
        map.put("pushToken", token);

        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).updateChildren(map);




    }
}