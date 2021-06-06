package com.example.seatpool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.textclassifier.TextLinks;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.seatpool.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class ViewActivity extends AppCompatActivity {

    private String charge;
    private String userEmail;
    private String userName;
    private String myUid;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Intent chargeIntent = getIntent();
        charge = chargeIntent.getStringExtra("paymentCharge");

        FirebaseDatabase.getInstance().getReference().child("Users").child(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = new UserModel();
                        userModel = snapshot.getValue(UserModel.class);
                        userName = userModel.UserName;
                        String amount = charge; //Viewintent.getStringExtra("price");     //표 가격
                        //String amount = "100"; //Viewintent.getStringExtra("price");     //표 가격
                        String email = myUid; //Viewintent.getStringExtra("email");      //사용자 이메일 주소
                        String name = userName; //Viewintent.getStringExtra("name");        //사용자 이름        실제로 적용할때는 다르게

                        WebView webview = (WebView) findViewById(R.id.webView);

                        webview.getSettings().setJavaScriptEnabled(true);              //아임포트 결제창 웹뷰에서 보여주기
                        webview.loadUrl("file:///android_asset/paymentPage.html");
                        //webview.loadUrl("javascript:payment('"+amount+"')");
                        webview.setWebChromeClient(new WebChromeClient());
                        webview.setWebViewClient(new WebViewClientClass(){
                            public void onPageFinished(WebView webview, String parameter){
                                webview.loadUrl("javascript:payment('"+amount+'/'+email+'/'+name+"')");
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private class WebViewClientClass extends WebViewClient{
        /*
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){
            view.loadUrl(String.valueOf(request.getUrl()));
            return true;
        }
        */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String callback ="http://paymentend";
            if(url.startsWith(callback)){
                String m_uid = Result.getParamValFromUrlString(url,"merchant_uid");


                Map<String,Object> map = new HashMap<>();
                map.put("merchant_uid", m_uid);
                FirebaseDatabase.getInstance().getReference().child("Users").child(myUid).updateChildren(map);

                RequestBody requestBody = new RequestBody() {

                    @Override
                    public MediaType contentType() {
                        return null;
                    }

                    @Override
                    public void writeTo(BufferedSink sink) throws IOException {

                    }
                };

                Request request = new Request.Builder()
                        .url("http://115.85.183.12:3000/payment/verify?" +
                                "muid="+ m_uid +
                                "&fbid="+ myUid)
                        .post(requestBody)
                        .build();

                System.out.println("================테스트입니다=========================");
                System.out.println("http://115.85.183.12:3000/payment/verify?" +
                        "muid="+m_uid +
                        "&fbid="+myUid);
                System.out.println("================테스트입니다=========================");

                OkHttpClient okHttpClient = new OkHttpClient();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                    }
                });

                Toast msg = Toast.makeText(getApplicationContext(),"결제완료", Toast.LENGTH_LONG);
                msg.show();
                Log.d("IAMPORT",m_uid);
                onBackPressed();
            }

            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:")) {
                Intent intent = null;

                try {
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME); //IntentURI처리
                    Uri uri = Uri.parse(intent.getDataString());

                    startActivity(new Intent(Intent.ACTION_VIEW, uri)); //해당되는 Activity 실행
                    return true;
                } catch (URISyntaxException ex) {
                    return false;
                } catch (ActivityNotFoundException e) {
                    if ( intent == null )   return false;

                    //if ( handleNotFoundPaymentScheme(intent.getScheme()) )  return true; //설치되지 않은 앱에 대해 사전 처리(Google Play이동 등 필요한 처리)

                    String packageName = intent.getPackage();
                    if (packageName != null) { //packageName이 있는 경우에는 Google Play에서 검색을 기본
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                        return true;
                    }

                    return false;
                }
            }

            return false;
        }
    }
}