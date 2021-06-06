package com.example.seatpool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.seatpool.Fragment.Board;
import com.example.seatpool.model.CommentModel;
import com.example.seatpool.model.UserModel;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchPeopleBoard extends AppCompatActivity {
    private String commentUserName;
    private ArrayList<CommentModel> testArrayList;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;
    private String postKey;
    private TextView postTitle;
    private TextView postContent;
    private TextView depart;
    private TextView arrival;
    private String myUid;
    private String writerUid;
    private View writerView;
    private ImageView writerImageView;
    private TextView writerUserName;
    private Button myPostDel;
    private TextView postReport;
    private TextView writeTime;
    private ArrayList<String> commentKey;
    private String comKey;
    Post destPost = new Post();
    Date time = new Date();
    ProgressDialog customProgressDialog;
    private LinearLayout deleteLayout;

    private EditText writeComment;
    private String comment;
    private Button commentButton;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Post");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_people_board);
        postTitle = findViewById(R.id.searchPeopleBoard_title);
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        postContent = findViewById(R.id.searchPeopleBoard_contents);
        depart = findViewById(R.id.searchPeopleBoard_depart);
        arrival = findViewById(R.id.searchPeopleBoard_arrival);
        writerView = (View) findViewById(R.id.searchPeopleBoard_writerInfoLinearLayout);
        writerImageView = findViewById(R.id.searchPeopleBoard_writerprofileImage);
        writerUserName = findViewById(R.id.searchPeopleBoard_writerName);
        writeComment = findViewById(R.id.searchPeopleBoard_commentEditText);
        writeTime = findViewById(R.id.searchPeopleBoard_writeTime);
        commentButton = findViewById(R.id.searchPeopleBoard_commentButton);
        myPostDel = findViewById(R.id.myPostDel);
        postReport = findViewById(R.id.Report);
        deleteLayout = findViewById(R.id.searchPeopleBoard_deleteLayout);
        recyclerView = findViewById(R.id.searchPeopleBoard_recyclerview);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        testArrayList = new ArrayList<>();
        commentKey = new ArrayList<>();

        Intent intent = getIntent();
        postKey = intent.getStringExtra("postKey");
        System.out.println("postKey = " + postKey);

        addCommentList();

        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(this);
        //로딩창을 투명하게
        customProgressDialog.setCancelable(false);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        customProgressDialog.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                customProgressDialog.dismiss();
            }
        }, 2000);

        adapter = new SearchPeopleAdapter(testArrayList, this);
        recyclerView.setAdapter(adapter);

        databaseReference.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                destPost = snapshot.getValue(Post.class);
                postTitle.setText(destPost.title);
                postContent.setText(destPost.content);
                depart.setText(destPost.depart);
                arrival.setText(destPost.arrival);
                writeTime.setText(destPost.timestamp);
                writerUid = destPost.sp_uid;
                if(!myUid.equals(writerUid)){
                    deleteLayout.setVisibility(View.INVISIBLE);
                }

                FirebaseDatabase.getInstance().getReference().child("Users").child(writerUid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                UserModel writer = new UserModel();
                                writer = snapshot.getValue(UserModel.class);
                                writerUserName.setText(writer.UserName);
                                Glide.with(writerView).load(writer.profileImageUrl).apply(new RequestOptions().circleCrop()).into(writerImageView);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }

        });




        //delete post
        myPostDel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(SearchPeopleBoard.this);

                if(myUid.equals(writerUid)){
                    alert.setTitle("게시글 삭제").setMessage("삭제하시겠습니까?");
                    alert.setNegativeButton("취소", null);
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            onPostDelete();

                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Map<String, Object> map = new HashMap<>();
                                    int num = 0;

                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                        Post post = snapshot.getValue(Post.class);
                                        map.put(snapshot.getKey() + "/num", num++);
                                        databaseReference.updateChildren(map);
                                    }
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("SearchPeople",String.valueOf(error.toException()));
                                }
                            });
                        }
                    });
                }
                else{
                    alert.setTitle("삭제 불가").setMessage("게시글은 본인만 삭제할 수 있습니다");
                    alert.setPositiveButton("확인", null);
                }
                alert.create().show();
            }
        });

        //report post
        postReport.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                AlertDialog.Builder alert = new AlertDialog.Builder(SearchPeopleBoard.this);

                alert.setTitle("게시글 신고").setMessage("게시글을 신고하시겠습니까?");
                alert.setNegativeButton("취소", null);
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        databaseReference.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            Map<String, Object> map = new HashMap<>();

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Post post = snapshot.getValue(Post.class);
                                int editedCount = ++post.reportCount;
                                map.put(postKey + "/reportCount", editedCount);

                                if(editedCount == 10) {
                                    map.put(postKey + "/title", "삭제된 게시글입니다");
                                    map.put(postKey + "/arrival", "  ");
                                    map.put(postKey + "/depart",  "  ");


                                    alert.setTitle("삭제").setMessage("해당 게시글이 삭제되었습니다");
                                    alert.setNegativeButton("", null);
                                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    });
                                    alert.create().show();
                                }
                                databaseReference.updateChildren(map);

                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
                    }
                });
                alert.create().show();
            }
        });

        //add comment
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentPush();
                addCommentList();
                InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
    }

    void commentPush() {
        CommentModel commentModel = new CommentModel();
        comment = writeComment.getText().toString();
        commentModel.searchPeople_comment = comment;
        commentModel.searchPeople_uid = myUid;
        commentModel.searchPeople_timestamp = simpleDateFormat.format(time);
        commentModel.report_count = 0;

        databaseReference.child(postKey).child("commentList").push()
                .setValue(commentModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                writeComment.setText("");
            }
        });

        databaseReference.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
            Map<String, Object> map = new HashMap<>();
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                int editedCount = ++post.commentCount;
                map.put(postKey+"/commentCount", editedCount);
                databaseReference.updateChildren(map);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public class SearchPeopleAdapter extends RecyclerView.Adapter<SearchPeopleAdapter.CustomViewHolder> {
        List<CommentModel> commentModels;
        private Context context;

        public SearchPeopleAdapter(ArrayList<CommentModel> commentModels, Context context) {
            this.commentModels = commentModels;
            this.context = context;
        }

        @NonNull
        @Override
        public SearchPeopleAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board_comment, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchPeopleAdapter.CustomViewHolder holder, int position) {
            FirebaseDatabase.getInstance().getReference("Post").child(postKey).child("commentList")
                    .child(commentKey.get(position)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    CommentModel commentModel = snapshot.getValue(CommentModel.class);
                    if(commentModel.report_count == 10)
                        holder.comment_userName.setText(" ");
                    else{
                        FirebaseDatabase.getInstance().getReference().child("Users").child(testArrayList.get(position).searchPeople_uid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {

                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        UserModel testUser = snapshot.getValue(UserModel.class);
                                        commentUserName = testUser.UserName;
                                        ((CustomViewHolder)holder).comment_userName.setText(commentUserName);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) { }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
            holder.comment_content.setText(testArrayList.get(position).searchPeople_comment);
            holder.comment_timestamp.setText(testArrayList.get(position).searchPeople_timestamp);

            //delete comment
            holder.comment_del.setOnClickListener(new View.OnClickListener() {
                String commentUid = testArrayList.get(position).searchPeople_uid;
                @Override
                public void onClick(View view) {
                    comKey = commentKey.get(position);

                    AlertDialog.Builder alert = new AlertDialog.Builder(SearchPeopleBoard.this);

                    if(myUid.equals(commentUid)){
                        alert.setTitle("댓글 삭제").setMessage("삭제하시겠습니까?");
                        alert.setNegativeButton("취소", null);
                        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                onCommentDelete();
                                testArrayList.remove(position);
                                recyclerView.removeViewAt(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemChanged(position, testArrayList.size());
                            }
                        });
                    }
                    else{
                        alert.setTitle("삭제 불가").setMessage("댓글은 본인만 삭제할 수 있습니다");
                        alert.setPositiveButton("확인", null);
                    }
                    alert.create().show();
                }
            });

            //report comment
            holder.comment_report.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(SearchPeopleBoard.this);

                    alert.setTitle("댓글 신고").setMessage("댓글을 신고하시겠습니까?");
                    alert.setNegativeButton("취소", null);
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            comKey = commentKey.get(position);
                            databaseReference.child(postKey).child("commentList").child(comKey)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        Map<String, Object> map = new HashMap<>();

                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            CommentModel commentModel = snapshot.getValue(CommentModel.class);
                                            int editedCount = ++commentModel.report_count;
                                            if(editedCount == 10){
                                                map.put(postKey + "/commentList/" + comKey + "/searchPeople_comment", "삭제된 댓글입니다");
                                                map.put(postKey + "/commentList/" + comKey + "/report_count", editedCount);
                                                addCommentList();
                                            }
                                            else if(editedCount > 10) {
                                                return;
                                            }
                                            else map.put(postKey + "/commentList/" + comKey + "/report_count", editedCount);
                                            databaseReference.updateChildren(map);
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) { }
                                    });
                        }
                    });
                    alert.create().show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return testArrayList.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public TextView comment_userName;
            public TextView comment_content;
            public TextView comment_timestamp;
            public TextView comment_del;
            public TextView comment_report;

            public CustomViewHolder(View view) {
                super(view);
                comment_userName = view.findViewById(R.id.boardComment_userName);
                comment_content = view.findViewById(R.id.boardComment_content);
                comment_timestamp = view.findViewById(R.id.boardComment_timestamp);
                comment_del = view.findViewById(R.id.boardComment_del);
                comment_report = view.findViewById(R.id.boardComment_report);
            }
        }
    }

    public void addCommentList(){
        FirebaseDatabase.getInstance().getReference().child("Post").child(postKey).child("commentList")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        testArrayList.clear();
                        commentKey.clear();
                        for(DataSnapshot item : snapshot.getChildren()){
                            CommentModel commentModel = item.getValue(CommentModel.class);
                            testArrayList.add(commentModel);
                            commentKey.add(item.getKey());
                            System.out.println(commentModel.searchPeople_comment);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void onPostDelete()
    {
        FirebaseDatabase.getInstance().getReference().child("Post").child(postKey)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startToast("삭제되었습니다");

                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                startToast("삭제할 수 없습니다.");
            }
        });
    }

    private void onCommentDelete()
    {
        FirebaseDatabase.getInstance().getReference().child("Post").child(postKey).child("commentList")
                .child(comKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                startToast("삭제되었습니다");

                databaseReference.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    Map<String, Object> map = new HashMap<>();
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
                        int editedCount = --post.commentCount;
                        map.put(postKey+"/commentCount", editedCount);
                        databaseReference.updateChildren(map);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                startToast("삭제할 수 없습니다.");
            }
        });
    }

    private void startToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}