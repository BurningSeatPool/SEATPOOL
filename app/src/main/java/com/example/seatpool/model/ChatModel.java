package com.example.seatpool.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


//채팅방의 전체적인 구조, 이 안에 comment와 user들 뭐 그리고 읽은사람들, 타임스탬프 찍히는것

public class ChatModel {

    public Map<String, Boolean> users = new HashMap<>(); // 채팅방 유저들
    public Map<String,Comment> comments = new HashMap<>(); //채팅방의 내용
    public String roomname;
    public String host;
    public String chatRoomDay;
    public String chatRoomMonth;
    public String chatRoomCharge;

    public Object roomCreationTime;

    public Map<String,String> testUsers = new HashMap<>();

    public static class Comment{

        public String c_uid;
        public String c_message;
        //public Object timestamp;
        public Map<String, Object> readUsers = new HashMap<>();

    }

    public static boolean isEmpty(Object s) {
        if (s == null) {
            return true;
        }

        if ((s instanceof String) && (((String)s).trim().length() == 0)) {
            return true;
        }

        if (s instanceof Map) {
            return ((Map<?, ?>)s).isEmpty();
        }

        if (s instanceof List) {
            return ((List<?>)s).isEmpty();
        }

        if (s instanceof Object[]) {
            return (((Object[])s).length == 0);
        }
        return false;

    }


}
