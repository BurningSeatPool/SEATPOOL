package com.example.seatpool;

public class TimeTableListView {
    private String depStation;
    private String arrStation;
    private String depTime;
    private String arrTime;
    private String charge;
    private String sale;

    public void setDepStation(String depS){
        depStation = depS;
    }

    public void setArrStation(String arrS){
        arrStation = arrS;
    }

    public void setDepTime(String depT){
        depTime = depT;
    }

    public void setArrTime(String arrT){
        arrTime = arrT;
    }

    public void setCharge(String cost){
        charge = cost;
    }

    public void setSale(String saleRate){
        sale = saleRate;
    }

    public String getDepStation(){
        return this.depStation;
    }

    public String getArrStation(){
        return this.arrStation;
    }

    public String getDepTime(){
        return this.depTime;
    }

    public String getArrTime(){
        return this.arrTime;
    }

    public String getCharge(){
        return this.charge;
    }

    public String getSale() {
        return this.sale;
    }

}
