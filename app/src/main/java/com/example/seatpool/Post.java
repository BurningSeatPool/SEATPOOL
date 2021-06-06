package com.example.seatpool;

public class Post {
    public String logo;
    public String title;
    public String depart;
    public String arrival;
    public String content;
    public String timestamp;
    public String sp_uid;
    public int num;
    public int commentCount;
    public int reportCount;

    public Post(String logo, String title, String depart, String arrival, String content,
                String timestamp,String sp_uid, int num, int commentCount, int reportCount) {
        this.logo = logo;
        this.title = title;
        this.depart = depart;
        this.arrival = arrival;
        this.content = content;
        this.timestamp = timestamp;
        this.sp_uid = sp_uid;
        this.num = num;
        this.commentCount = commentCount;
        this.reportCount = reportCount;
    }

    /*public Post(String logo, String title, String depart, String arrival, String content) {
        this.logo = logo;
        this.title = title;
        this.depart = depart;
        this.arrival = arrival;
        this.content = content;
    }*/

    public Post(){}
}
