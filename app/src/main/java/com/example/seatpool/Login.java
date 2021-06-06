package com.example.seatpool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.seatpool.chatFragment.TicketGroupChatActivity;
import com.example.seatpool.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Login extends AppCompatActivity {

    private EditText et_id;
    private EditText et_password;
    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;
    String id;
    String password;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_id = findViewById(R.id.emailEditTExt);
        et_password = findViewById(R.id.passwordEditText);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();

        findViewById(R.id.gotoSignUpButton).setOnClickListener(onClickListener);
        findViewById(R.id.loginButton).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.loginButton:
                    login();

                    break;
                case R.id.gotoSignUpButton:
                    myStartActivity(Signup.class);
                    break;
            }
        }
    };

    //Firebase로 로그인
    private void login() {
        String email = ((EditText) findViewById(R.id.emailEditTExt)).getText().toString();
        String password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString();

        if(email.length() > 0 && password.length() > 0){
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                String myUid = user.getUid();
                                FirebaseDatabase.getInstance().getReference().child("Users").child(myUid)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        UserModel userModel = new UserModel();
                                        userModel = snapshot.getValue(UserModel.class);
                                        if(userModel.userMainAddress == null && userModel.userSubAddress == null){
                                            myStartActivity(FindAddressActivity.class);
                                            finish();
                                        }
                                        else{
                                            startToast("로그인에 성공했습니다.");
                                            myStartActivity(SearchTrain.class);
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            } else {
                                if(task.getException() != null){
                                    startToast(task.getException().toString());
                                }
                            }

                        }
                    });

        }else{
            startToast("이메일 또는 비밀번호를 입력해주세요.");
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
    @Override
    public void onBackPressed() {

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            toast.cancel();
        }
    }
}