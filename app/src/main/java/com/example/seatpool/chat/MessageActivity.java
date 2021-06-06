package com.example.seatpool.chat;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.seatpool.R;
import com.example.seatpool.model.ChatModel;
import com.example.seatpool.model.NotificationModel;
import com.example.seatpool.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MessageActivity extends AppCompatActivity {

    Map<String,UserModel> users = new HashMap<>();

    private String destinationUid;
    private Button button;
    private EditText editText;

    private String uid;
    private String chatRoomUid;


    private RecyclerView recyclerView;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    private UserModel destinationUserModel;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    int peopleCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 채팅을 요구하는 uid 즉 단말기에 로그인된 uid

        destinationUid = getIntent().getStringExtra("destinationUid"); //채팅을 당하는 uid
        button = findViewById(R.id.messageActivity_button);
        editText = findViewById(R.id.messageActivity_editText);
        recyclerView = findViewById(R.id.messageActivity_recyclerview);


        checkRoom();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatModel chatModel = new ChatModel();
                chatModel.users.put(destinationUid,true);
                chatModel.users.put(uid,true);

                if(chatRoomUid == null) {
                    button.setEnabled(false);
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    checkRoom();
                                }
                            });

                }else{
                    ChatModel.Comment comment = new ChatModel.Comment();
                    comment.c_uid = uid;
                    comment.c_message = editText.getText().toString();
                    //comment.timestamp = ServerValue.TIMESTAMP;

                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //sendGcm();
                            editText.setText("");
                        }
                    });

                }
            }
        });


    }

    /*void sendGcm(){
        Gson gson = new Gson();

        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = destinationUserModel.pushToken;
        notificationModel.notification.title = userName;
        notificationModel.notification.text = editText.getText().toString();
        notificationModel.data.title = userName;
        notificationModel.data.text = editText.getText().toString();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"),gson.toJson(notificationModel));

        Request request = new Request.Builder()
                .header("Content-Type","application/json")
                .addHeader("Authorization","key=AIzaSyCPQZpbGMPsoL-QUzwddlLiRk-q6i5FBPg")
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }*/

    void checkRoom(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid)
                .equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot item :snapshot.getChildren()){
                    ChatModel chatModel = item.getValue(ChatModel.class);
                    if(chatModel.users.containsKey(destinationUid) && chatModel.users.size() == 2 ){
                        chatRoomUid = item.getKey();
                        button.setEnabled(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                        recyclerView.setAdapter(new RecyclerViewAdapter());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


        List<ChatModel.Comment> comments;

        public RecyclerViewAdapter() {
            comments = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference().child("Users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    destinationUserModel = snapshot.getValue(UserModel.class); // 유저에 대한 정보 담김
                    getMessageList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        void getMessageList(){
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments");

            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    comments.clear();
                    Map<String,Object> readUsersMap = new HashMap<>();

                    for(DataSnapshot item : snapshot.getChildren()){
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        //ChatModel.Comment comment_modify = item.getValue(ChatModel.Comment.class);
                        comment_origin.readUsers.put(uid,true); // 서버가 메시지를 읽은 것을 확인

                        readUsersMap.put(key,comment_origin);
                        comments.add(comment_origin);
                    }

                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").updateChildren(readUsersMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    ///메시지가 갱신된다
                                    notifyDataSetChanged();
                                    recyclerView.scrollToPosition(comments.size()-1);
                                }

                            });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } // 메시지를 읽는 코드

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message,parent,false);

            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MessageViewHolder messageViewHolder = ((MessageViewHolder)holder);


            if(comments.get(position).c_uid.equals(uid)){
                messageViewHolder.textView_message.setText(comments.get(position).c_message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.rightbubble);
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);
                //recyclerView.getLayoutManager().scrollToPosition(position); //내가보낸 메시지
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);
                setReadCounter(position,messageViewHolder.textView_readCounter_left);

            }else{
                Glide.with(holder.itemView.getContext())
                        .load(destinationUserModel.profileImageUrl)
                        .apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.imageView_profile);
                messageViewHolder.textView_name.setText(destinationUserModel.UserName);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble);
                messageViewHolder.textView_message.setText(comments.get(position).c_message);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                //recyclerView.getLayoutManager().scrollToPosition(position); //상대방이 보낸 메시지
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);
                setReadCounter(position,messageViewHolder.textView_readCounter_right);
            }
            /*long unixTime = (long) comments.get(position).timestamp;
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);
            messageViewHolder.textView_timestamp.setText(time);*/
        }

        //읽지않은사람 count
        void setReadCounter(int position,TextView textView){

            //uid의 개수를 통해 채팅방에 참여한 사람 수얻어온다.
            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Map<String,Boolean> users = (Map<String, Boolean>) snapshot.getValue();
                    peopleCount = users.size();
                    int count = peopleCount - comments.get(position).readUsers.size(); //읽지않은 사람 수
                    if(count>0){
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(String.valueOf(count));
                    }else{
                        textView.setVisibility(View.INVISIBLE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });



        }


        @Override
        public int getItemCount() {

            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public TextView textView_timestamp;
            public TextView textView_readCounter_left;
            public TextView textView_readCounter_right;

            public MessageViewHolder(View view) {
                super(view);
                textView_message = (TextView) view.findViewById(R.id.messageItem_textView_message);
                textView_name = (TextView) view.findViewById(R.id.messageItem_textView_name);
                imageView_profile = (ImageView) view.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_destination = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_destination);
                linearLayout_main = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_main);
                textView_timestamp = (TextView) view.findViewById(R.id.messageItem_textView_timestamp);
                textView_readCounter_left = (TextView) view.findViewById(R.id.messageItem_textView_readCounter_left);
                textView_readCounter_right = (TextView) view.findViewById(R.id.messageItem_textView_readCounter_right);
            }
        }
    }

    @Override
    public void onBackPressed() {

        if(valueEventListener != null){
            databaseReference.removeEventListener(valueEventListener);
        }
        finish();
        overridePendingTransition(R.anim.fromleft,R.anim.toright);
    }
}