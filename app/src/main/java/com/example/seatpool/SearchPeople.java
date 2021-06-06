package com.example.seatpool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.seatpool.model.ChatModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SearchPeople extends AppCompatActivity {
    ChatModel chatModel = new ChatModel();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Post> arrayList;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;


    String userName;
    String pw;
    String id;
    String profile = "https://firebasestorage.googleapis.com/v0/b/seat-34957.appspot.com/o/%EA%B8%B0%EC%B0%A8%ED%91%9C.png?alt=media&token=2b773b14-d415-4bed-bbbd-a92af1b3f9b2";
    private List<String> keys = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_people);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true); // 성능 강화
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>();

        database = FirebaseDatabase.getInstance(); // 파이어베이스 db 연동

        databaseReference = database.getReference("Post"); // db 테이블 연결

        findViewById(R.id.sp_action_button).setOnClickListener(onClickListener);


        DatabaseReference testRef = databaseReference;


        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //파이어베이스 db에서 데이터를 받아오는 곳
                arrayList.clear(); // 기존 배열 초기화
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class); // 만들어 두었던 Post 객체 data에 담는다
                    arrayList.add(post); // 담은 데이터들을 배열 리스트에 넣고 리사이클러 뷰로 보낼 준비
                    keys.add(snapshot.getKey());
                }
                adapter.notifyDataSetChanged(); // 새로고침
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //디비를 가져오던 중 에러 발생 시
                Log.e("SearchPeople",String.valueOf(error.toException()));

            }
        });
        adapter = new CustomAdapter(arrayList,this);
        recyclerView.setAdapter(adapter); //리사이클러뷰에 어댑터 연결


    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

        private ArrayList<Post> arrayList;
        private Context context;
        private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");


        public CustomAdapter(ArrayList<Post> arrayList, Context context) {
            this.arrayList = arrayList;
            this.context = context;
        }

        @NonNull
        @Override
        public CustomAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            CustomAdapter.CustomViewHolder holder = new CustomAdapter.CustomViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull CustomAdapter.CustomViewHolder holder, int position) {
            Glide.with(holder.itemView)
                    .load(arrayList.get(position).logo)
                    .into(holder.list_item_logo);
            holder.list_item_title.setText(arrayList.get(position).title);
            holder.list_item_depart.setText(String.valueOf(arrayList.get(position).depart));
            holder.list_item_arrival.setText(String.valueOf(arrayList.get(position).arrival));
            holder.list_item_content.setText(arrayList.get(position).content);


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(),SearchPeopleBoard.class);
                    intent.putExtra("postKey",keys.get(position));
                    startActivity(intent);
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

            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);
                this.list_item_logo = itemView.findViewById(R.id.list_item_logo);
                this.list_item_title = itemView.findViewById(R.id.list_item_title);
                this.list_item_depart = itemView.findViewById(R.id.list_item_depart);
                this.list_item_arrival = itemView.findViewById(R.id.list_item_arrival);
                this.list_item_content = itemView.findViewById(R.id.list_item_content);
            }
        }
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.sp_action_button:
                    myStartActivity(SearchPeopleEdit.class);
                    finish();
                    break;
            }
        }
    };

    private void myStartActivity(Class c){
        Intent intent=new Intent(this,c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}