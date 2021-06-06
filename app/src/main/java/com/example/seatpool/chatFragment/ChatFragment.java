package com.example.seatpool.chatFragment;


import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.seatpool.R;
import com.example.seatpool.chat.GroupMessageActivity;
import com.example.seatpool.chat.MessageActivity;
import com.example.seatpool.model.ChatModel;
import com.example.seatpool.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class ChatFragment extends Fragment {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat,container,false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.chatFragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new ChatRecyclerViewAdapter());

        return view;
    }

    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserModel> userModels;

        private List<String> keys = new ArrayList<>(); // 채팅방에 대한 key값들

        private String uid;
        private List<ChatModel> chatModels = new ArrayList<>();
        private ArrayList<String> destinationUsers = new ArrayList<>();

        public ChatRecyclerViewAdapter() {
            userModels = new ArrayList<>();
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    chatModels.clear();
                    for(DataSnapshot item : snapshot.getChildren()){
                        chatModels.add(item.getValue(ChatModel.class));
                        keys.add(item.getKey()); // 방에대한 key 받아옴
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat,parent,false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            CustomViewHolder customViewHolder = (CustomViewHolder)holder;
            String destinationUid = null;

            //유저 챗방에 있는 유저를 일일이 체크함
            for(String user: chatModels.get(position).users.keySet()){
                if(!user.equals(uid)){
                    destinationUid = user;
                    destinationUsers.add(destinationUid);
                }
            }


            FirebaseDatabase.getInstance().getReference().child("Users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    Glide.with(customViewHolder.itemView.getContext())
                            .load(userModel.profileImageUrl)
                            .apply(new RequestOptions().circleCrop())
                            .into(customViewHolder.imageView);
                    customViewHolder.textView_title.setText(userModel.UserName);
                } //내가 아니라면(상대방이라면) 나타냄

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //메시지를 내림 차순으로 정렬 후 마지막 메시지의 키값을 가져옴
            Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.reverseOrder());
            commentMap.putAll(chatModels.get(position).comments);
            if(commentMap.keySet().toArray().length > 0) {

                String lastMessageKey = (String) commentMap.keySet().toArray()[0];
                customViewHolder.textView_lastmessage.setText(chatModels.get(position).comments.get(lastMessageKey).c_message);

                //timestamp
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                /*long unixTime = (long) chatModels.get(position).comments.get(lastMessageKey).timestamp;
                Date date = new Date(unixTime);
                customViewHolder.textView_timestamp.setText(simpleDateFormat.format(date)); // Timestamp*/
            } // last메시지 표현하기위한 기능 우리는 필요없음

            customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = null;
                    if(chatModels.get(position).users.size() > 2){ //우리는 무조건 단체이기 때문에 이 조건분기절은 할필요없을듯? 무조건 GroupMessageActivity 가도록?
                        intent = new Intent(view.getContext(), GroupMessageActivity.class);
                        intent.putExtra("destinationRoom",keys.get(position)); // 선택된 방에 대한 key 보내줌

                    }else{
                        intent = new Intent(view.getContext(), MessageActivity.class);
                        intent.putExtra("destinationUid", destinationUsers.get(position));
                    }

                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                    startActivity(intent, activityOptions.toBundle());
                }
            }); //각각의 뷰 클릭했을 때 채팅방이동

        }



        @Override
        public int getItemCount() {
            return chatModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView_title;
            public TextView textView_lastmessage;
            public TextView textView_timestamp;

            public CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.chatItem_imageview);
                textView_title = view.findViewById(R.id.chatItem_textview_title);
                textView_lastmessage = view.findViewById(R.id.chatItem_textview_lastmessage);
                textView_timestamp = view.findViewById(R.id.chatItem_textview_timestamp);
            }
        }
    }
}
