package com.example.seatpool;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FindAddressActivity extends AppCompatActivity {

    // 초기변수설정
    EditText edit_addr_main;
    EditText edit_addr_sub;
    Button btn_register;
    Button btn_cancel;
    String address_main;
    String address_sub;
    // 주소 요청코드 상수 requestCode
    private static final int SEARCH_ADDRESS_ACTIVITY = 10000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_address);
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // UI 요소 연결
        edit_addr_main = findViewById(R.id.edit_addr);
        edit_addr_sub = findViewById(R.id.edit_addr_sub);
        btn_register = findViewById(R.id.findAddressRegisterbtn);
        btn_cancel = findViewById(R.id.findAddressCancelbtn);
        // 주소입력창 클릭
        edit_addr_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("주소설정페이지", "주소입력창 클릭");
                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                if(status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {

                    Log.i("주소설정페이지", "주소입력창 클릭");
                    Intent i = new Intent(getApplicationContext(), AddressWebview.class);
                    // 화면전환 애니메이션 없애기
                    overridePendingTransition(0, 0);
                    // 주소결과
                    startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY);

                }else {
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }


            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object> map_main = new HashMap<>();
                Map<String,Object> map_sub = new HashMap<>();
                address_sub = edit_addr_sub.getText().toString();
                map_main.put("userMainAddress", address_main);
                map_sub.put("userSubAddress",address_sub);
                FirebaseDatabase.getInstance().getReference().child("Users").child(myUid).updateChildren(map_main);
                FirebaseDatabase.getInstance().getReference().child("Users").child(myUid).updateChildren(map_sub);
                startToast("위치정보가 정상적으로 입력되었습니다.");
                myStartActivity(SearchTrain.class);
                finish();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object> map_main = new HashMap<>();
                Map<String,Object> map_sub = new HashMap<>();
                map_main.put("userMainAddress", "아주대학교");
                map_sub.put("userSubAddress","아주대학교");
                FirebaseDatabase.getInstance().getReference().child("Users").child(myUid).updateChildren(map_main);
                FirebaseDatabase.getInstance().getReference().child("Users").child(myUid).updateChildren(map_sub);
                startToast("위치정보를 거부하셨습니다.");
                myStartActivity(SearchTrain.class);
                finish();
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.i("test", "onActivityResult");

        switch (requestCode) {
            case SEARCH_ADDRESS_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    address_main = intent.getExtras().getString("data");
                    if (address_main != null) {
                        Log.i("test", "data:" + address_main);
                        edit_addr_main.setText(address_main);
                    }
                }
                break;
        }
    }

    private void myStartActivity(Class c){
        Intent intent=new Intent(this,c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    private void startToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}