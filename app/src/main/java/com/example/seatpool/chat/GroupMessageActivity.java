package com.example.seatpool.chat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.seatpool.Login;
import com.example.seatpool.R;
import com.example.seatpool.SearchTrain;
import com.example.seatpool.User;
import com.example.seatpool.ViewActivity;
import com.example.seatpool.model.ChatModel;
import com.example.seatpool.model.NotificationModel;
import com.example.seatpool.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import java.text.ParseException;
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
import okio.BufferedSink;


public class GroupMessageActivity extends AppCompatActivity {
    ChatModel exitChatModel = new ChatModel();
    private DrawerLayout drawerLayout;
    private View drawerView;
    private ImageView ivMenu;
    private Toolbar toolbar;
    private String chatName;
    private ImageView toolbarMenu;
    private String notificationKey;
    private ImageView exitButton;
    private String hostUid;
    private String day;
    private String month;
    //유저들의 정보를 받을 Map
    Map<String,UserModel> users = new HashMap<>();
    String destinationRoom;
    String uid;
    EditText editText;
    ChatModel timeChatModel = new ChatModel();

    private TextView hostNameTextView;

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    private RecyclerView recyclerView;
    private RecyclerView recyclerView_side;

    List<ChatModel.Comment> comments = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);
        exitButton = findViewById(R.id.groupChatSideNavi_exitButton);
        destinationRoom = getIntent().getStringExtra("destinationRoom");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        editText = findViewById(R.id.groupMessageActivity_editText);
        hostNameTextView = findViewById(R.id.groupChatSideNavi_hostName);

        drawerLayout = (DrawerLayout)findViewById(R.id.groupMessageActivity_drawerlayout);
        drawerView = (View)findViewById(R.id.groupChatSideNavi_linearLayout);
        recyclerView_side = findViewById(R.id.groupChatSideNavi_recyclerview);
        ImageView my_profile_image = findViewById(R.id.groupChatSideNavi_profileImage);
        TextView my_username = findViewById(R.id.groupChatSideNavi_myName);
        Button payment_btn = findViewById(R.id.groupChatSideNavi_payButton);
        toolbarMenu = findViewById(R.id.groupMessageActivity_toolbarBtn);

        Intent getchatNameIntent = getIntent();
        chatName = getchatNameIntent.getStringExtra("chatName");

        System.out.println("chatName = " + chatName);
        System.out.println("Destinationroomid = " + destinationRoom);

        recyclerView_side.setAdapter(new SidebarCustomAdapter());
        recyclerView_side.setLayoutManager(new LinearLayoutManager(GroupMessageActivity.this));

        payment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().child(chatName).child(destinationRoom)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ChatModel refundChatModel = new ChatModel();
                        refundChatModel = snapshot.getValue(ChatModel.class);
                        Intent intent = new Intent(getApplicationContext(),ViewActivity.class);
                        intent.putExtra("paymentCharge",refundChatModel.chatRoomCharge);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });


        FirebaseDatabase.getInstance().getReference().child(chatName).child(destinationRoom)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        timeChatModel = snapshot.getValue(ChatModel.class);
                        month = timeChatModel.chatRoomMonth;
                        day = timeChatModel.chatRoomDay;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }); // 날짜 얻기위한 reference

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkRefundState(timeChatModel)){
                    startToast("출발 2일전 미만이라 환불 및 방나가기가 불가능합니다");
                }
                else{
                    exitChatroom();
                    changeMyChatRoomState();
                }

            }
        }); // 채팅방 나가는 버튼 onclickListener


        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel myUsermodel = new UserModel();
                myUsermodel = snapshot.getValue(UserModel.class);
                my_username.setText(myUsermodel.UserName);
                Glide.with(drawerView).load(myUsermodel.profileImageUrl).apply(new RequestOptions().circleCrop()).into(my_profile_image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //사이드바에 내 유저정보 뿌려주는곳

        FirebaseDatabase.getInstance().getReference().child(chatName).child(destinationRoom).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ChatModel hostChatModel = new ChatModel();
                hostChatModel = snapshot.getValue(ChatModel.class);
                hostUid = hostChatModel.host;
                FirebaseDatabase.getInstance().getReference().child("Users").child(hostUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel hostUserModel = new UserModel();
                        hostUserModel = snapshot.getValue(UserModel.class);
                        hostNameTextView.setText(hostUserModel.UserName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } //LEADER정보 보여주는곳

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Host정보 얻어오는곳


        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot item : snapshot.getChildren()){
                    users.put(item.getKey(),item.getValue(UserModel.class));
                }
                init();
                recyclerView = findViewById(R.id.groupMessageActivity_recyclerview);
                recyclerView.setAdapter(new GroupMessageRecyclerViewAdapter());
                recyclerView.setLayoutManager(new LinearLayoutManager(GroupMessageActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //메시지 작성하고 init하는곳

        toolbarMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerView);
            }
        });

        drawerLayout.setDrawerListener(listener);

        drawerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

    }

    DrawerLayout.DrawerListener listener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };

    void sendGcm(){
        Gson gson = new Gson();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = notificationKey;
        notificationModel.data.title = "결제마감 알림";
        notificationModel.data.message = "결제를 해주세요 제발";

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"),gson.toJson(notificationModel));

        Request request = new Request.Builder()
                .header("Content-Type","application/json")
                .addHeader("Authorization","key=AAAAY351QXg:APA91bHAkN5FMVjoVPDS8yQ5qMpb6LkgeONZDaxf_a1AGReyFOlo39dmg2x-xF-laD7-e9jaBoukYk4J6pV3GzRxSQ-B6CvdZnlX-0y9yzof-BO1FBMcLe0zRyl8uUkzF_QSXbWXixFh")
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
    } // fcm보내는테스트

    void init(){
        Button button = findViewById(R.id.groupMessageActivity_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatModel.Comment comment = new ChatModel.Comment();
                comment.c_uid = uid;
                comment.c_message = editText.getText().toString();

                //comment.timestamp = ServerValue.TIMESTAMP;
                FirebaseDatabase.getInstance().getReference().child(chatName).child(destinationRoom).child("comments").push().setValue(comment)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                editText.setText("");
                            }
                        });
            }
        });
    } // 메시지 보내는 함수

    class GroupMessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public GroupMessageRecyclerViewAdapter(){
            getMessageList();
        }

        void getMessageList(){
            databaseReference = FirebaseDatabase.getInstance().getReference().child(chatName).child(destinationRoom).child("comments");

            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    comments.clear(); // comments = 메시지담는부분
                    Map<String,Object> readUsersMap = new HashMap<>();

                    for(DataSnapshot item : snapshot.getChildren()){
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        //ChatModel.Comment comment_modify = item.getValue(ChatModel.Comment.class);
                        //comment_origin.readUsers.put(uid,true); // 서버가 메시지를 읽은 것을 확인

                        readUsersMap.put(key,comment_origin);
                        comments.add(comment_origin);
                    }

                    FirebaseDatabase.getInstance().getReference().child(chatName).child(destinationRoom).child("comments").updateChildren(readUsersMap)
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
            return new GroupMessageViewHolder(view);
        } // 뷰 그려주기

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            GroupMessageViewHolder messageViewHolder = (GroupMessageViewHolder) holder;

            if(comments.get(position).c_uid.equals(uid)){
                messageViewHolder.textView_message.setText(comments.get(position).c_message);
                messageViewHolder.textView_message.setTextColor(Color.WHITE);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.theme_chatroom_bubble_me_01_image);
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);

                //recyclerView.getLayoutManager().scrollToPosition(position); //내가보낸 메시지
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);
                //setReadCounter(position,messageViewHolder.textView_readCounter_left);

            }else{
                Glide.with(holder.itemView.getContext())
                        .load(users.get(comments.get(position).c_uid).profileImageUrl)
                        .apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.imageView_profile);
                messageViewHolder.textView_name.setText(users.get(comments.get(position).c_uid).UserName);
                messageViewHolder.textView_message.setTextColor(Color.BLACK);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.theme_chatroom_bubble_you_01_image);
                messageViewHolder.textView_message.setText(comments.get(position).c_message);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                //recyclerView.getLayoutManager().scrollToPosition(position); //상대방이 보낸 메시지
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);
                //setReadCounter(position,messageViewHolder.textView_readCounter_right);
            }
            /*long unixTime = (long) comments.get(position).timestamp;
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);
            messageViewHolder.textView_timestamp.setText(time);*/
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }


        private class GroupMessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public TextView textView_timestamp;
            public TextView textView_readCounter_left;
            public TextView textView_readCounter_right;
            public GroupMessageViewHolder(View view) {
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
    } // 메시지 주고받기 어댑터

    class SidebarCustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<String> userIds;


        public SidebarCustomAdapter() {
            String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            userIds = new ArrayList<String>();
            FirebaseDatabase.getInstance().getReference().child(chatName).child(destinationRoom).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userIds.clear();
                    ChatModel chatModel = new ChatModel();
                    chatModel = snapshot.getValue(ChatModel.class);

                    if(!ChatModel.isEmpty(chatModel)){ //객체가 null값이 아닐때만 시행됨
                        for(String test : chatModel.testUsers.keySet()){ //저장된 key값 확인
                            System.out.println("[Key]:" + test + " [Value]:" + chatModel.testUsers.get(test));
                            userIds.add(chatModel.testUsers.get(test));
                        }
                        System.out.println("===================테스트입니다=========================");
                        for(int i = 0;i<userIds.size();i++){
                            System.out.println(userIds.get(i));
                        }
                        System.out.println("===================테스트입니다=========================");
                        notifyDataSetChanged(); // 새로고침
                    }


                }//서버에서 넘어오는 데이터들

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend,parent,false);
            return new SidebarCustomAdapter.CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


            FirebaseDatabase.getInstance().getReference().child("Users").child(userIds.get(position)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserModel sideBarUserModel = new UserModel();
                    sideBarUserModel = snapshot.getValue(UserModel.class);
                    
                    /*Glide.with(holder.itemView.getContext())
                    .load(sideBarUserModel.profileImageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .into(((SidebarCustomAdapter.CustomViewHolder)holder).imageView);*/
                    if(sideBarUserModel.userGender.equals("남자")){
                        ((SidebarCustomAdapter.CustomViewHolder)holder).imageView.setImageResource(R.drawable.ic_man);
                    }
                    else{
                        ((SidebarCustomAdapter.CustomViewHolder)holder).imageView.setImageResource(R.drawable.ic_woman);
                    }
                    if(sideBarUserModel.paid.equals("1")){
                        ((CustomViewHolder)holder).textView_comment.setText("결제완료");
                    }
                    ((SidebarCustomAdapter.CustomViewHolder)holder).textView.setText(sideBarUserModel.UserName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            /*Glide.with(holder.itemView.getContext())
                    .load(userModels.get(position).profileImageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .into(((SidebarCustomAdapter.CustomViewHolder)holder).imageView);*/
            //((SidebarCustomAdapter.CustomViewHolder)holder).textView.setText(userIds.get(position));


        }

        @Override
        public int getItemCount() {
            return userIds.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;
            public TextView textView_comment;

            public CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.friend_item_image_view);
                textView = view.findViewById(R.id.friend_item_text_view);
                textView_comment = view.findViewById(R.id.friend_item_textView_comment);
            }
        }
    } //사이드바 참여유저정보 어댑터

    private void myStartActivity(Class c){
        Intent intent=new Intent(this,c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    } // 화면 전환

    void exitChatroom(){

        String exitRoomUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(chatName).child(destinationRoom);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ChatModel chatModel = new ChatModel();
                chatModel = snapshot.getValue(ChatModel.class);
                if(chatModel.testUsers.size() <= 1){
                    snapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            startToast("방이 폭파되었어요.\n결제를 한 사람에게 환불이 완료되었습니다.");
                            refundMoney();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
                }

                else {
                    startToast("방에서 나가셨습니다.\n결제를 한 사람에게 환불이 완료되었습니다.");
                    refundMoney();
                    snapshot.getRef().child("testUsers").child(exitRoomUserUid).removeValue();
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    } // 채팅방 나가는 함수

    private void refundMoney(){

        String refundUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("Users").child(refundUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel refundUserModel = new UserModel();
                        refundUserModel = snapshot.getValue(UserModel.class);
                        String merchant_uid = refundUserModel.merchant_uid;

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
                                .url("http://115.85.183.12:3000/payment/refund?" +
                                        "muid="+ merchant_uid +
                                        "&fbid="+ refundUid)
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

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




    } // 환불 함수

    private boolean checkRefundState(ChatModel chatModel){

        SimpleDateFormat dateFormat = new  SimpleDateFormat("yyyy-MM-dd");
        long now = System.currentTimeMillis();
        //java.util.Locale.getDefault();

        Date currentDate = new Date(now); //현재 날짜
        Date departDate = new Date();
        String strCurrentDate = dateFormat.format(currentDate);

        try {
            departDate = dateFormat.parse("2021-"+chatModel.chatRoomMonth+"-"+chatModel.chatRoomDay);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        String strDepartDate = dateFormat.format(departDate);
        departDate.setDate(departDate.getDate()-2); // 이틀 전으로 감소
        return currentDate.after(departDate);

    } //출발 2일전 방인지 체크하는 함수

    private void changeMyChatRoomState(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String,Object> map = new HashMap<>();
        map.put("myChatRoom", "0");
        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).updateChildren(map);
    } //방에서 나갈경우 정보 업데이트

    private void startToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}