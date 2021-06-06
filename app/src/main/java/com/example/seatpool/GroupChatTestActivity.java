package com.example.seatpool;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.seatpool.model.ChatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class GroupChatTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_test);
        findViewById(R.id.groupChatTestActivity_floatingActionButton).setOnClickListener(onClickListener);


    }


    void showDialog(){
        final LinearLayout linearLayout = (LinearLayout) View.inflate(GroupChatTestActivity.this, R.layout.dialog_create_chatroom,null);
        new AlertDialog.Builder(GroupChatTestActivity.this).setView(linearLayout)
                .setPositiveButton("생성", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText editText = linearLayout.findViewById(R.id.createDialog_editText);
                String groupChatroomName = editText.getText().toString();

                ChatModel chatModel = new ChatModel();
                chatModel.users.put(FirebaseAuth.getInstance().getCurrentUser().getUid(),true);
                FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel);
                dialogInterface.dismiss();
            }
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.groupChatTestActivity_floatingActionButton:
                    //showDialog();
                    ChatModel chatModel = new ChatModel();
                    chatModel.users.put(FirebaseAuth.getInstance().getCurrentUser().getUid(),true);
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel); //버튼클릭시 채팅방 생성되긴 함
                    break;
            }
        }
    };


}