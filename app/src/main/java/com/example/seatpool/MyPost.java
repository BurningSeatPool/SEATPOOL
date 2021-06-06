package com.example.seatpool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.seatpool.Fragment.My;
import com.example.seatpool.model.ChatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyPost extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Post> userArrayList;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    String myUid;
    View view;
    ProgressDialog customProgressDialog;

 private List<String> keys = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView = findViewById(R.id.myPostList);
        recyclerView.setHasFixedSize(true); // 성능 강화
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        userArrayList = new ArrayList<>();

        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.setCancelable(false);
        //로딩창을 투명하게
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        customProgressDialog.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                customProgressDialog.dismiss();
            }
        }, 2000);

        database = FirebaseDatabase.getInstance(); // 파이어베이스 db 연동
        databaseReference = database.getReference("Post"); // db 테이블 연결

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userArrayList.clear();
                keys.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    keys.add(snapshot.getKey());
                    if(myUid.equals(post.sp_uid)) {
                        userArrayList.add(0, post);
                    }
                }
                adapter.notifyDataSetChanged(); // 새로고침
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MyPost",String.valueOf(error.toException()));
            }
        });
        adapter = new MyPost.CustomAdapter(userArrayList,this);
        recyclerView.setAdapter(adapter); //리사이클러뷰에 어댑터 연결
    }

    public class CustomAdapter extends RecyclerView.Adapter<MyPost.CustomAdapter.CustomViewHolder> {

        private ArrayList<Post> arrayList;
        private Context context;

        public CustomAdapter(ArrayList<Post> arrayList, Context context) {
            this.arrayList = arrayList;
            this.context = context;
        }

        @NonNull
        @Override
        public MyPost.CustomAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            MyPost.CustomAdapter.CustomViewHolder holder = new MyPost.CustomAdapter.CustomViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyPost.CustomAdapter.CustomViewHolder holder, int position) {
            int num = arrayList.get(position).num;
            String postKey = keys.get(num);

            LinearLayout placeSchedule = (LinearLayout)view.findViewById(R.id.placeSchedule);
            LinearLayout dateSchedule = (LinearLayout)view.findViewById(R.id.dateSchedule);

            Glide.with(holder.itemView)
                    .load(arrayList.get(position).logo)
                    .into(holder.list_item_logo);
            holder.list_item_title.setText(arrayList.get(position).title);
            holder.list_item_depart.setText(arrayList.get(position).depart);
            holder.list_item_arrival.setText(arrayList.get(position).arrival);
            holder.list_item_content.setText(arrayList.get(position).timestamp);
            holder.list_item_comment_number.setText(String.valueOf(arrayList.get(position).commentCount));

            if(arrayList.get(position).title.equals("삭제된 게시글입니다")){
                holder.list_item_title.setTextColor(Color.GRAY);
                holder.list_item_title.setTextSize(20);
                placeSchedule.setVisibility(View.INVISIBLE);
                dateSchedule.setVisibility(View.INVISIBLE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    databaseReference.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Post post = snapshot.getValue(Post.class);
                            if(post.reportCount == 10){
                                myStartActivity(SearchPeopleBoardDeleted.class);
                            }
                            else {
                                int num = arrayList.get(position).num;
                                Intent intent = new Intent(getApplicationContext(),SearchPeopleBoard.class);
                                intent.putExtra("postKey", keys.get(num));
                                startActivity(intent);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            // 삼항 연산자
            return (arrayList != null ? arrayList.size() : 0);
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder {
            ImageView list_item_logo;
            TextView list_item_title;
            TextView list_item_depart;
            TextView list_item_arrival;
            TextView list_item_content;
            TextView list_item_comment_number;

            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);
                this.list_item_logo = itemView.findViewById(R.id.list_item_logo);
                this.list_item_title = itemView.findViewById(R.id.list_item_title);
                this.list_item_depart = itemView.findViewById(R.id.list_item_depart);
                this.list_item_arrival = itemView.findViewById(R.id.list_item_arrival);
                this.list_item_content = itemView.findViewById(R.id.list_item_content);
                this.list_item_comment_number = itemView.findViewById(R.id.list_item_comment_number);
            }
        }
    }

    private void myStartActivity(Class c){
        Intent intent=new Intent(this,c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}