package com.example.seatpool.chatFragment;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.seatpool.R;
import com.example.seatpool.User;
import com.example.seatpool.chat.GroupMessageActivity;
import com.example.seatpool.model.ChatModel;
import com.example.seatpool.model.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;


public class TicketGroupChatActivity extends AppCompatActivity {
    ChatModel chatModel = new ChatModel();
    private String myUid;
    private String chatName = "";
    private String userMonth = "";
    private String userDay = "";
    private String charge = "";
    private String postId;

    //AlertDialog.Builder oDialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog);
    //AlertDialog.Builder oDialog = new AlertDialog.Builder(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_group_chat);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.ticketGroupChat_recyclerview);
        recyclerView.setAdapter(new SelectFriendRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Button button = findViewById(R.id.ticketGroupChat_button);
        findViewById(R.id.ticketGroupChat_floatingactionbutton).setOnClickListener(onClickListener);



    }
    class SelectFriendRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserModel> userModels;
        private ChatModel localChatModel = new ChatModel();
        private List<String> keys = new ArrayList<>(); // 채팅방에 대한 key값들
        private List<ChatModel> chatModels = new ArrayList<>();
        private ArrayList<String> destinationUsers = new ArrayList<>();

        public SelectFriendRecyclerViewAdapter() {

            userModels = new ArrayList<>();
            myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Intent chatNameIntent = getIntent();
            chatName = chatNameIntent.getStringExtra("chatName");

            FirebaseDatabase.getInstance().getReference().child(chatName+"chatrooms").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //userModels.clear();
                    chatModels.clear();
                    for(DataSnapshot dataSnapshot :snapshot.getChildren()){

                        chatModels.add(dataSnapshot.getValue(ChatModel.class));
                        keys.add(dataSnapshot.getKey()); // 방에대한 key 받아옴

                    }
                    notifyDataSetChanged(); // 새로고침
                }//서버에서 넘어오는 데이터들, 이 부분은 단순히 유저정보들만 리사이클러뷰에 나타내는 부분

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_chat,parent,false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            /*if(chatModels.get(position).testUsers.size() < 1){
                FirebaseDatabase.getInstance().getReference().child(chatName+"chatrooms").child(keys.get(position))
                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            }*/

            String destinationUid = null;

            /*Glide.with(holder.itemView.getContext())
                    .load(userModels.get(position).profileImageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .into(((CustomViewHolder)holder).imageView);*/
            for(String user: chatModels.get(position).users.keySet()){
                if(!user.equals(myUid)){
                    destinationUid = user;
                    destinationUsers.add(destinationUid);
                }
            }

            ((CustomViewHolder)holder).textView.setText(chatModels.get(position).roomname);
            ((CustomViewHolder)holder).textView_numOfPeople.setText(chatModels.get(position).testUsers.size()+" / 4");


            ((CustomViewHolder)holder).itemView.setOnClickListener(new View.OnClickListener() {
                List<ChatModel> list = new ArrayList<>();
                //private ChatModel localChatModel = new ChatModel();

                @Override
                public void onClick(View view) {

                    String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    AlertDialog.Builder builder = new AlertDialog.Builder(TicketGroupChatActivity.this);
                    LayoutInflater layoutInflater = TicketGroupChatActivity.this.getLayoutInflater();
                    View testView = layoutInflater.inflate(R.layout.dialog_enter_room,null);
                    final TextView textView = (TextView) testView.findViewById(R.id.enterRoomDialog_textview);
                    builder.setView(testView).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            FirebaseDatabase.getInstance().getReference().child("Users").child(myUid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        Map<String, Object> map = new HashMap<>();
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            UserModel userModel = snapshot.getValue(UserModel.class);
                                            String[] userRoomUid = userModel.myChatRoom.split("/");

                                            Log.e("tag", chatName + "/" + position +"/"+keys.get(position));

                                            //참여하지 않은 경우
                                            if(userModel.myChatRoom.equals("0")) {
                                                map.put(myUid + "/myChatRoom", chatName + "chatrooms/" + keys.get(position));
                                                FirebaseDatabase.getInstance().getReference().child("Users").updateChildren(map);

                                                localChatModel.users.put(myUid,true);
                                                gotoChatRoom(view, keys.get(position));
                                            }
                                            //참여한 그룹인 경우
                                            else if(userRoomUid[1].equals(keys.get(position))){
                                                gotoChatRoom(view, keys.get(position));
                                            }
                                            //참여한 그룹과 다른 경우
                                            else{
                                                AlertDialog.Builder alert = new AlertDialog.Builder(TicketGroupChatActivity.this);
                                                alert.setTitle("참여불가").setMessage("이미 그룹에 참여하고 있습니다");
                                                alert.setPositiveButton("확인", null);
                                                alert.create().show();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) { }
                                    });
                        }
                    }).setNegativeButton("취소", null);
                    builder.show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return chatModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;
            public TextView textView_numOfPeople;
            public ImageView imageView_logo;

            public CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.item_group_chat_peopleImage);
                textView = view.findViewById(R.id.item_group_chat_title);
                textView_numOfPeople = view.findViewById(R.id.item_group_chat_numberOfpeople);
                imageView_logo = view.findViewById(R.id.item_group_chat_imageView);

            }
        }
    }

    private void startToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.ticketGroupChat_floatingactionbutton:
                    showDialog_createRoom(TicketGroupChatActivity.this);
                    break;
            }
        }
    };

    void showDialog_createRoom(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = TicketGroupChatActivity.this.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_create_chatroom,null);
        final EditText editText = (EditText) view.findViewById(R.id.createDialog_editText);
        Intent getIntent = getIntent();
        userMonth = getIntent.getStringExtra("userMonth");
        userDay = getIntent.getStringExtra("userDay");
        charge = getIntent.getStringExtra("charge");

        builder.setView(view).setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child(chatName+"chatrooms");
                DatabaseReference pushedPostRef = postRef.push();
                postId = pushedPostRef.getKey();

                FirebaseDatabase.getInstance().getReference().child("Users").child(myUid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            Map<String, Object> map = new HashMap<>();
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                UserModel userModel = snapshot.getValue(UserModel.class);

                                if(userModel.myChatRoom.equals("0")){
                                    map.put(myUid + "/myChatRoom", chatName+"chatrooms/"+ postId);
                                    FirebaseDatabase.getInstance().getReference().child("Users").updateChildren(map);

                                    chatModel.testUsers.put(myUid,myUid); // 이 부분이 나의 uid를 put하는 부분이기 때문에 check박스 부분은 필요없을듯?
                                    chatModel.chatRoomMonth = userMonth;
                                    chatModel.chatRoomDay = userDay;
                                    chatModel.chatRoomCharge = charge;
                                    chatModel.roomname = editText.getText().toString();
                                    chatModel.host = myUid;
                                    pushedPostRef.setValue(chatModel); // put한 나의 chatModel을 chatrooms에 넣는다.

                                    bookPushAlarm();
                                    gotoChatRoom(view, postId);
                                }
                                else{
                                    AlertDialog.Builder alert = new AlertDialog.Builder(TicketGroupChatActivity.this);
                                    alert.setTitle("생성불가").setMessage("이미 그룹에 참여하고 있습니다");
                                    alert.setPositiveButton("확인", null);
                                    alert.create().show();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
            }
        }).setNegativeButton("취소", null);
        builder.show();
    } // 방 만들기

    void gotoChatRoom(View v, String key){
        Intent intent = null;

        FirebaseDatabase.getInstance().getReference().child(chatName+"chatrooms").child(key).child("testUsers")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        snapshot.getRef().child(myUid).setValue(myUid);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });

        intent = new Intent(v.getContext(), GroupMessageActivity.class);
        intent.putExtra("destinationRoom", key); // 선택된 방에 대한 key 보내줌
        intent.putExtra("chatName",chatName+"chatrooms");
        ActivityOptions activityOptions
                = ActivityOptions.makeCustomAnimation(v.getContext(), R.anim.fromright, R.anim.toleft);
        startActivity(intent, activityOptions.toBundle());
        finish();
    }

    void bookPushAlarm(){
        RequestBody requestBody = new RequestBody() {

            @Override
            public MediaType contentType() {
                return null;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {

            }
        };

        Request request = new Request.Builder()
                .url("http://115.85.183.12:3000/"+
                        "group/start?"+
                        "roomname="+chatName+"chatrooms"+
                        "&roomid="+postId+
                        "&price="+charge+
                        "&month="+userMonth+
                        "&day="+userDay)
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


    } //방 생성할때 알람 예약 해놓기

    void showDialog_enterRoom(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = TicketGroupChatActivity.this.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_enter_room,null);
        final TextView textView = (TextView) view.findViewById(R.id.enterRoomDialog_textview);
        builder.setView(view).setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.show();
    }
}