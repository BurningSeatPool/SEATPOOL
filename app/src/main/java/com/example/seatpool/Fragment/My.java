package com.example.seatpool.Fragment;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.example.seatpool.Login;
import com.example.seatpool.MyComment;
import com.example.seatpool.MyPost;
import com.example.seatpool.ProgressDialog;
import com.example.seatpool.R;
import com.example.seatpool.SearchTrain;
import com.example.seatpool.User;
import com.example.seatpool.chat.GroupMessageActivity;
import com.example.seatpool.model.UserModel;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class My extends Fragment {

    ViewGroup viewGroup;
    ImageView myImage;
    TextView myName;
    TextView myEmail;
    TextView myRefund;
    Button refundButton;
    TextView myPost;
    TextView myComment;
    TextView myService;
    TextView myDelete;
    TextView myChatRoom;
    UserModel myUsermodel;
    ProgressDialog customProgressDialog;
    private FirebaseAuth mAuth;
    private TextView signOutTextView;
    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;


    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_my, container, false);
        signOutTextView = viewGroup.findViewById(R.id.myLogOut);
        myImage = (ImageView)viewGroup.findViewById(R.id.myImage);
        myName = (TextView)viewGroup.findViewById(R.id.myName);
        myEmail = (TextView)viewGroup.findViewById(R.id.myEmail);
        myRefund = (TextView)viewGroup.findViewById(R.id.myRefund);
        refundButton = (Button)viewGroup.findViewById(R.id.refundButton);
        myPost = (TextView)viewGroup.findViewById(R.id.myPost);
        myComment = (TextView)viewGroup.findViewById(R.id.myCommnet);
        myService = (TextView)viewGroup.findViewById(R.id.myService);
        myDelete = (TextView)viewGroup.findViewById(R.id.myDelete);
        myChatRoom = (TextView)viewGroup.findViewById(R.id.myChatRoom);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        signOutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startToast("로그아웃 되었습니다.");
                myStartActivity(Login.class);
                getActivity().finish();
            }
        });
        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(getActivity());
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
        }, 1500);



        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                    backKeyPressedTime = System.currentTimeMillis();
                    toast = Toast.makeText(getActivity(), "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                    getActivity().finish();
                    toast.cancel();
                }
            }
        });

        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myUsermodel = new UserModel();
                myUsermodel = snapshot.getValue(UserModel.class);
                myName.setText(myUsermodel.UserName);
                myRefund.setText(Integer.toString(myUsermodel.money));
                Glide.with(getActivity()).load(myUsermodel.profileImageUrl).apply(new RequestOptions().circleCrop()).into(myImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        myEmail.setText(user.getEmail());

        //myRefund Event
        refundButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                if(myRefund.getText().equals("0")){
                    alert.setTitle("환급불가").setMessage("환급할 금액이 없습니다");
                }
                else{
                    HashMap<String,Object> map = new HashMap<>();
                    myUsermodel.money = 0;
                    map.put("money",myUsermodel.money);
                    FirebaseDatabase.getInstance().getReference().child("Users").child(uid).updateChildren(map);
                    alert.setTitle("환급성공").setMessage("환급되었습니다");
                }
                alert.setPositiveButton("확인", null);
                alert.create().show();
            }
        });

        //myChatRoom Event
        myChatRoom.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String chatRoomInfo = myUsermodel.myChatRoom;
                if(chatRoomInfo.equals("0")) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle("참여불가").setMessage("현재 참여중인 그룹이 없습니다");
                    alert.setPositiveButton("확인", null);
                    alert.create().show();
                }
                else {
                    String[] chatRoom = chatRoomInfo.split("/");
                    Log.e("tag", chatRoom[0] +"/" + chatRoom[1]);

                    Intent intent = new Intent(getActivity(), GroupMessageActivity.class);
                    intent.putExtra("destinationRoom",chatRoom[1]);
                    intent.putExtra("chatName",chatRoom[0]);
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                    startActivity(intent, activityOptions.toBundle());
                }
            }
        });

        //myPost Event
        myPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myStartActivity(MyPost.class);
            }
        });

        //myComment Event
        myComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myStartActivity(MyComment.class);
            }
        });

        //myService Event
        myService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://pf.kakao.com/_BtHAs"));
                startActivity(intent);
            }
        });

        //myDelete Event
        myDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("회원탈퇴").setMessage("정말 탈퇴하실 건가요?ㅠ");
                alert.setNegativeButton("취소", null);
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        FirebaseDatabase.getInstance().getReference().child("Users").child(uid)
                                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startToast("계정이 삭제되었습니다");
                                myStartActivity(Login.class);
                                getActivity().finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                startToast("삭제할 수 없습니다.");
                            }
                        });
                    }
                });
                alert.create().show();
            }
        });

        return viewGroup;
    }

    private void startToast(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private void myStartActivity(Class c){
        Intent intent=new Intent(this.getActivity(), c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }




}