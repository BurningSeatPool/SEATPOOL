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
import com.example.seatpool.model.ChatModel;
import com.example.seatpool.model.CommentModel;
import com.example.seatpool.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MyComment extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Post> userArrayList;
    private ArrayList<CommentModel> commentModelArrayList;
    private List<String> boardKey;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    String myUid;
    View view;

    ProgressDialog customProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_comment);

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView = findViewById(R.id.myCommentList);
        recyclerView.setHasFixedSize(true); // 성능 강화
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        userArrayList = new ArrayList<>();
        commentModelArrayList = new ArrayList<>();
        boardKey = new ArrayList<>();

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
                commentModelArrayList.clear();
                boardKey.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);

                    FirebaseDatabase.getInstance().getReference().child("Post").child(snapshot.getKey()).child("commentList")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for(DataSnapshot item : snapshot.getChildren()){
                                        CommentModel commentModel = item.getValue(CommentModel.class);
                                        if(commentModel.searchPeople_uid.equals(myUid)){
                                            boardKey.add(0, snapshot.getRef().getParent().getKey());
                                            userArrayList.add(0, post);
                                            commentModelArrayList.add(0, commentModel);
                                        }
                                    }
                                    adapter.notifyDataSetChanged(); // 새로고침
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { }
                            });
                }
                adapter.notifyDataSetChanged(); // 새로고침
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MyComment",String.valueOf(error.toException()));
            }
        });
        adapter = new MyComment.CustomAdapter(userArrayList, commentModelArrayList, this);
        recyclerView.setAdapter(adapter); //리사이클러뷰에 어댑터 연결
    }

    public class CustomAdapter extends RecyclerView.Adapter<MyComment.CustomAdapter.CustomViewHolder> {
        private ArrayList<Post> userArrayList;
        private ArrayList<CommentModel> commentModelArrayList;
        private Context context;

        public CustomAdapter(ArrayList<Post> userArrayList, ArrayList<CommentModel> commentModelArrayList, Context context) {
            this.userArrayList = userArrayList;
            this.commentModelArrayList = commentModelArrayList;
            this.context = context;
        }

        @NonNull
        @Override
        public MyComment.CustomAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_comment, parent, false);
            MyComment.CustomAdapter.CustomViewHolder holder = new MyComment.CustomAdapter.CustomViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyComment.CustomAdapter.CustomViewHolder holder, int position) {
            String postKey = boardKey.get(position);

            holder.commentContent.setText(commentModelArrayList.get(position).searchPeople_comment);
            holder.item_my_comment_depart.setText(userArrayList.get(position).depart);
            holder.item_my_comment_arrival.setText(userArrayList.get(position).arrival);
            holder.item_my_comment_date.setText(commentModelArrayList.get(position).searchPeople_timestamp);
            holder.item_my_comment_board.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), SearchPeopleBoard.class);
                    intent.putExtra("postKey", postKey);
                    startActivity(intent);
                }
            });
            if(userArrayList.get(position).title.equals("삭제된 댓글입니다")){
                holder.commentContent.setTextColor(Color.GRAY);
            }
        }

        @Override
        public int getItemCount() {
            // 삼항 연산자
            return (userArrayList != null ? userArrayList.size() : 0);
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder {
            TextView commentContent;
            TextView item_my_comment_depart;
            TextView item_my_comment_arrival;
            TextView item_my_comment_date;
            TextView item_my_comment_board;

            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);
                this.commentContent = itemView.findViewById(R.id.commentContent);
                this.item_my_comment_depart = itemView.findViewById(R.id.item_my_comment_depart);
                this.item_my_comment_arrival = itemView.findViewById(R.id.item_my_comment_arrival);
                this.item_my_comment_date = itemView.findViewById(R.id.item_my_comment_date);
                this.item_my_comment_board = itemView.findViewById(R.id.item_my_comment_board);
            }
        }
    }

    private void myStartActivity(Class c){
        Intent intent=new Intent(this,c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}