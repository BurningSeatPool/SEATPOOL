package com.example.seatpool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class AfterAuth extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_auth);

        Intent intent = getIntent();
        String code = intent.getStringExtra("code");
        String state = intent.getStringExtra("state");

        TextView show_text = (TextView) findViewById(R.id.auth_code);
        show_text.setText("code : "+code +"\n"+"state : "+ state);

    }

    long time=0;
    long backTime=0;
    @Override
    public void onBackPressed(){
        long curTime=System.currentTimeMillis();
        long gatTime=curTime-backTime;

        if(0<=gatTime && 2000>=gatTime){
            //super.onBackPressed();
            Intent goMain = new Intent(getApplicationContext(),Signup.class);
            goMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(goMain);
        }else{
            backTime=curTime;
            Toast toast = Toast.makeText(getApplicationContext(),"Press back to go Main",Toast.LENGTH_LONG);
            toast.show();
        }
    }

}