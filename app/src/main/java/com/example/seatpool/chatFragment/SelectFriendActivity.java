package com.example.seatpool.chatFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.seatpool.R;
import com.example.seatpool.model.ChatModel;
import com.example.seatpool.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class SelectFriendActivity extends AppCompatActivity {
    ChatModel chatModel = new ChatModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.selectFriendActivity_recyclerview);
        recyclerView.setAdapter(new SelectFriendRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Button button = findViewById(R.id.selectFriendActivity_button);


        //실질적으로 이 버튼을 누르는 것이 채팅방 만드는 것임 참고하면 좋을듯.
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                chatModel.users.put(myUid,true); // 이 부분이 나의 uid를 put하는 부분이기 때문에 check박스 부분은 필요없을듯?
                FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel); // put한 나의 chatModel을 chatrooms에 넣는다.
                //여기는 comment부분은 하지 않았음. 따라서 users 정보만 저장되는 것이 맞음.
            }
        });


    }
    class SelectFriendRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserModel> userModels;

        public SelectFriendRecyclerViewAdapter() {
            userModels = new ArrayList<>();
            String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userModels.clear();
                    for(DataSnapshot dataSnapshot :snapshot.getChildren()){

                        UserModel userModel = dataSnapshot.getValue(UserModel.class);
                        if(userModel.uid.equals(myUid)){
                            continue;
                        }
                        userModels.add(userModel);
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_select,parent,false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Glide.with(holder.itemView.getContext())
                    .load(userModels.get(position).profileImageUrl)
                    .apply(new RequestOptions().circleCrop())
                    .into(((CustomViewHolder)holder).imageView);

            ((CustomViewHolder)holder).textView.setText(userModels.get(position).UserName);

            /*holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), MessageActivity.class);
                    intent.putExtra("destinationUid",userModels.get(position).uid);
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(),R.anim.fromright,R.anim.toleft);
                    startActivity(intent,activityOptions.toBundle());
                }
            });*/

            if(userModels.get(position).comment != null){
                ((CustomViewHolder) holder).textView_comment.setText(userModels.get(position).comment);
            }

            ((CustomViewHolder) holder).checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    //체크 된 사람들
                    if(b){
                        chatModel.users.put(userModels.get(position).uid,true); //체크된다면 chatModel의 users에 put해준다. 만약 내가 들어가려고 하면 나의 uid를 put 해야겠지?

                    //체크 안된 사람들
                    }else{
                        chatModel.users.remove(userModels.get(position));
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;
            public TextView textView_comment;
            public CheckBox checkBox;

            public CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.friend_item_image_view1);
                textView = view.findViewById(R.id.friend_item_text_view1);
                textView_comment = view.findViewById(R.id.friend_item_textView_comment1);
                checkBox = view.findViewById(R.id.friend_item_checkbox);
            }
        }
    }



}