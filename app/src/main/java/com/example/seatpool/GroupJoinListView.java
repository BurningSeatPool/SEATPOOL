package com.example.seatpool;

public class GroupJoinListView {
    private int img;
    private String name;
    private String age;
    private String gender;

    public void setImage(int im){
        img = im;
    }

    public void setName(String n){
        name = n;
    }

    public void setAge(String a){
        age = a;
    }

    public void setGender(String g){
        gender = g;
    }

    public int getImg(){
        return this.img;
    }

    public String getName(){
        return this.name;
    }

    public String getAge(){
        return this.age;
    }

    public String getGender(){
        return this.gender;
    }

}
