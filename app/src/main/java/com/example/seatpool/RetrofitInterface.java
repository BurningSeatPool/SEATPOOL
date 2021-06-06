package com.example.seatpool;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitInterface {

    String BASEURL = "http://115.85.183.12:3000/";
    //String BASEURL = "http://192.168.0.103:3000/";

    @GET("register")
    Call<String> getRegister(@Query("id") String id,
                             @Query("name") String name,
                             @Query("pw") String pw,
                             @Query("accountBank") String accountBank,
                             @Query("account") String account,
                             @Query("birth") String birth);

    //Search Train Time Table
    @GET("ktx/get/count")
    Call<String> getCount(@Query("month") String month,
                          @Query("day") String day,
                          @Query("Depart_Time") String Depart_Time,
                          @Query("Depart") String Depart,
                          @Query("Dest") String Dest);

    @GET("ktx/search")
    Call<String> getKtx(@Query("month") String month,
                        @Query("day") String day,
                        @Query("Depart_Time") String Depart_Time,
                        @Query("Depart") String Depart,
                        @Query("Dest") String Dest);

    @GET("ktx/get/timetable")
    Call<List<TimeTablePojo>> getKtxTimeTable(@Query("month") String month,
                                       @Query("day") String day,
                                       @Query("Depart_Time") String Depart_Time,
                                       @Query("Depart") String Depart,
                                       @Query("Dest") String Dest);


}
