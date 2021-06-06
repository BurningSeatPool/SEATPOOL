package com.example.seatpool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.seatpool.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Signup extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM = 10;
    int REQUEST_EXTERNAL_STORAGE_PERMISSION = 100;  private TextView text;
    private final String TAG = getClass().getSimpleName();
    private String myResponse;
    private String header;
    private String location;
    private EditText et_name;
    private EditText et_birth;
    private EditText et_id;
    private EditText et_password;
    private EditText et_checkPass;
    private EditText et_account;
    private TextView backtoLoginText;
    private Button genderSelect_male;
    private Button genderSelect_female;
    private String gender;
    Button auth_button;
    Button signUp_button;
    private ImageView profile;
    private Uri imageUri;
    Spinner bankSpinner;
    CheckBox checkBox;
    Button terms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        profile = (ImageView) findViewById(R.id.signUp_imageView_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, PICK_FROM_ALBUM);

            }
        });

        auth_button = (Button) findViewById(R.id.authButton);
        signUp_button = findViewById(R.id.signUpButton);
        //backToLogin_button = findViewById(R.id.backButton);
        backtoLoginText = findViewById(R.id.signUpActivityBacktoLogin);
        et_id = findViewById(R.id.idEditText);
        et_name = findViewById(R.id.nameEditText);
        et_password = findViewById(R.id.passwordEditText);
        et_checkPass = findViewById(R.id.passwordCheckEditText);
        et_account = findViewById(R.id.accountEditText);
        et_birth = findViewById(R.id.birthEditText);
        checkBox = findViewById(R.id.checkBox);
        terms = findViewById(R.id.terms);
        genderSelect_male = findViewById(R.id.genderSelectButton_male);
        genderSelect_female = findViewById(R.id.genderSelectButton_female);

        AlertDialog.Builder alert = new AlertDialog.Builder(Signup.this);

        bankSpinner = (Spinner)findViewById(R.id.bankSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.bankList, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bankSpinner.setAdapter(adapter);
        bankSpinner.setSelection(0);

        final OkHttpClient client = new OkHttpClient();
        String baseurl = "http://developers.kftc.or.kr/proxy/oauth/2.0/authorize";
        String response_type="code";
        String client_id="d5674974-d492-4abd-9671-83a4c8d14ce7";
        String redirect_uri="http%3A%2F%2Flocalhost%3A3000%2F";
        String scope = "login%20inquiry%20transfer";
        String state="b80BLsfigm9OokPTjy03elbJqRHOfGSY";
        String auth_type="0";

        String url = baseurl+"?response_type=code&client_id="
                +client_id+"&redirect_uri="
                +redirect_uri+"&scope=login%20inquiry%20transfer&state=b80BLsfigm9OokPTjy03elbJqRHOfGSY&auth_type=0";

        final Request request = new Request.Builder()
                .url(url)
                .build();

        //남자 선택시 event
        genderSelect_male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genderSelect_male.setBackgroundColor(Color.rgb(98,180,211));
                genderSelect_female.setBackgroundColor(Color.rgb(230,229,229));
                gender = genderSelect_male.getText().toString();
            }
        });

        //여자 선택시 event
        genderSelect_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genderSelect_female.setBackgroundColor(Color.rgb(98,180,211));
                genderSelect_male.setBackgroundColor(Color.rgb(230,229,229));
                gender = genderSelect_female.getText().toString();
            }
        });

        //본인인증 버튼 event
        auth_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //비밀번호 확인
                if(!et_password.getText().toString().equals(et_checkPass.getText().toString())){
                    alert.setTitle("비밀번호 오류");
                    alert.setMessage("비밀번호가 다릅니다.");
                    alert.setPositiveButton("확인", null);
                    alert.create().show();
                }

                else {
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful()) {
                                myResponse = response.body().string();
                                header = response.headers().toString();
                                location = header.substring(header.lastIndexOf("Location") + 10);

                                Signup.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() { }
                                });

                                Intent webview = new Intent(getApplicationContext(), Webviewtest.class);
                                webview.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                webview.putExtra("location", location);
                                webview.putExtra("state", "b80BLsfigm9OokPTjy03elbJqRHOfGSY");
                                startActivity(webview);
                             }
                        }

                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });

        //이용약관 버튼
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog termsDialog = new Dialog(Signup.this);
                termsDialog.setContentView(R.layout.dialog_terms);

                Button termsOk = termsDialog.findViewById(R.id.termsOk);
                ScrollView test = termsDialog.findViewById(R.id.test);

                termsDialog.show();

                termsOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        termsDialog.dismiss();
                    }
                });
            }
        });

        //회원가입 버튼 event
        signUp_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               signUp();
            }
        });

        //뒤로가기 버튼 event
        backtoLoginText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void signUp(){
        if(!checkBox.isChecked()){
            startToast("약관에 동의해주세요");
            return;
        }
        else if(et_id.getText().toString() == null || et_birth.getText().toString() == null || et_password.getText().toString() == null
                || et_account.getText().toString() == null || imageUri == null){
            startToast("작성되지 않은 칸이 있습니다");
            return;
        }
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(et_id.getText().toString(),et_password.getText().toString())
                .addOnCompleteListener(Signup.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        final String uid = task.getResult().getUser().getUid();
                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(et_name.getText().toString())
                                .build();
                        task.getResult().getUser().updateProfile(userProfileChangeRequest);

                        FirebaseStorage.getInstance().getReference().child("userProfiles").child(uid).putFile(imageUri)
                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                        Task<Uri> imageUrl = task.getResult().getStorage().getDownloadUrl();
                                        while(!imageUrl.isComplete());

                                        UserModel userModel = new UserModel();
                                        userModel.UserName = et_name.getText().toString();
                                        userModel.Birth = Long.parseLong(et_birth.getText().toString());
                                        userModel.Account = et_account.getText().toString();
                                        userModel.profileImageUrl = imageUrl.getResult().toString();
                                        userModel.userGender = gender;
                                        userModel.uid = uid;
                                        userModel.paid = "0";
                                        userModel.money = 0;
                                        userModel.userMainAddress = null;
                                        userModel.userSubAddress = null;
                                        userModel.myChatRoom = "0";
                                        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).setValue(userModel);
                                    }
                                });
                        startToast("회원가입 완료");
                        finish();
                    }
                });
    }

    private void startToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData(); // 이미지 경로 원본
            profile.setImageURI(imageUri); //이미지 바꿈
        }
    }
}





