package com.example.seatpool;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.util.StringTokenizer;

public class Result {
    private String success;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public static String getParamValFromUrlString(String url, String paramKey){

        Log.d("## url", url);
        String[] urlParamPair = url.split("\\?");
        if(urlParamPair.length < 2){
            Log.d("##", "파라미터가 존재하지 않는 URL 입니다.");
            return "";
        }
        String queryString = urlParamPair[1];
        Log.d("## queryString", queryString);
        StringTokenizer st = new StringTokenizer(queryString, "&");
        while(st.hasMoreTokens()){
            String pair = st.nextToken();
            Log.d("## pair", pair);
            String[] pairArr = pair.split("=");
            if(paramKey.equals(pairArr[0])){
                return pairArr[1]; // 찾았을 경우
            }
        }
        return "";
    }
}
