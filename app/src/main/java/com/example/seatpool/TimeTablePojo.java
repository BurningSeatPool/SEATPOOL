package com.example.seatpool;

import com.google.gson.annotations.SerializedName;

public class TimeTablePojo {
    @SerializedName("Train_ID")
    String Train_ID;
    @SerializedName("Depart")
    String Depart;
    @SerializedName("Dest")
    String Dest;
    @SerializedName("Depart_Time")
    String Depart_Time;
    @SerializedName("Arrival_Time")
    String Arrival_Time;
    @SerializedName("Amount")
    String Amount;
    @SerializedName("sale")
    String sale;

    public String toString() {
        String s = "";
        s += getTrain_ID() + "/";
        s += getDepart() + "/";
        s += getDest() + "/";
        s += getDepart_Time() + "/";
        s += getArrival_Time() + "/";
        s += getAmount() + "/";
        s += getSale() + "/";
        return s;
    }

    public String getTrain_ID() {
        return Train_ID;
    }

    public void setTrain_ID(String train_ID){
        this.Train_ID = train_ID;
    }

    public String getDepart() {
        return Depart;
    }

    public void setDepart(String depart){
        this.Depart = depart;
    }

    public String getDest() {
        return Dest;
    }

    public void setDest(String dest){
        this.Dest = dest;
    }

    public String getDepart_Time() {
        return Depart_Time;
    }

    public void setDepart_Time(String departTime){
        this.Depart_Time = departTime;
    }

    public String getArrival_Time() {
        return Arrival_Time;
    }

    public void setArrival_Time(String arrivalTime){
        this.Train_ID = arrivalTime;
    }

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount){
        this.Amount = amount;
    }

    public String getSale() {
        return sale;
    }

    public void setSale(String sale){
        this.sale = sale;
    }
}
