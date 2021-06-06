package com.example.seatpool;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class Webviewtest extends AppCompatActivity {
    private String msg;
    private String state;
    private WebView webView;

    String name;
    String birth;
    String id;
    String password;
    String check_password;
    String account;
    String complete = "본인인증 완료";
    int count;

    private long pressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;

    //Toast toastmsg = Toast.makeText(this.getApplicationContext(), "Error", Toast.LENGTH_LONG);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webviewtest);
        webView = (WebView) findViewById(R.id.webtest);

        Intent intent = getIntent();
        msg = intent.getStringExtra("location");
        state = intent.getStringExtra("state");

        Toast toastmsg = Toast.makeText(this.getApplicationContext(), msg, Toast.LENGTH_LONG);
        //toastmsg.show();

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(msg);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClientClass());
    }




    private class WebViewClientClass extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){

            //String callbackUrl = "http%3A%2F%2Flocalhost%3A3000%2F";
            String callbackUrl = "http://localhost:3000/";
            if(url.startsWith(callbackUrl)){
                String code = Result.getParamValFromUrlString(url,"code");
                String returnState = Result.getParamValFromUrlString(url,"state");
                if(!returnState.equals(state)){
                    Intent gotoerror = new Intent(getApplicationContext(), ErrorPage.class);
                    startActivity(gotoerror);
                    //toastmsg.show();
                    return true;
                }

                onBackPressed();

            }

            view.loadUrl(url);
            return true;
        }
    }

}