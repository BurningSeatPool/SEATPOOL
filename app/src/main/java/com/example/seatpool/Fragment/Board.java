package com.example.seatpool.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.seatpool.Post;
import com.example.seatpool.ProgressDialog;
import com.example.seatpool.R;
import com.example.seatpool.SearchPeopleBoard;
import com.example.seatpool.SearchPeopleBoardDeleted;
import com.example.seatpool.SearchPeopleEdit;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Board extends Fragment {
    ViewGroup viewGroup;
    View view;
    ProgressDialog customProgressDialog;
    private EditText editText;
    ArrayList<Post> filteredList;

    private ImageView refreshImage;
    private ImageView searchImage;

    private RecyclerView recyclerView;
    private CustomAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Post> arrayList;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    boolean allowRefresh = false;
    int num;
    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;
    String userName;
    String pw;
    String id;
    String profile = "https://firebasestorage.googleapis.com/v0/b/seat-34957.appspot.com/o/%EA%B8%B0%EC%B0%A8%ED%91%9C.png?alt=media&token=2b773b14-d415-4bed-bbbd-a92af1b3f9b2";
    private List<String> keys = new ArrayList<>();
    long end;

    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        viewGroup = (ViewGroup)inflater.inflate(R.layout.fragment_board, container, false);
        searchImage = viewGroup.findViewById(R.id.searchPeoplesearchicon);
        editText = viewGroup.findViewById(R.id.findPeopletextView);
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

        long now = System.currentTimeMillis();

        recyclerView = viewGroup.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true); // 성능 강화
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>();
        filteredList = new ArrayList<>();
        database = FirebaseDatabase.getInstance(); // 파이어베이스 db 연동
        databaseReference = database.getReference("Post"); // db 테이블 연결
        viewGroup.findViewById(R.id.sp_action_button).setOnClickListener(onClickListener);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                allowRefresh = true;
                arrayList.clear();
                keys.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    arrayList.add(0, post);
                    keys.add(snapshot.getKey());
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SearchPeople",String.valueOf(error.toException()));
            }
        });

        adapter = new CustomAdapter(arrayList, getActivity());
        recyclerView.setAdapter(adapter);
        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(getActivity());
        //로딩창을 투명하게
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        customProgressDialog.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                end = System.currentTimeMillis();
                customProgressDialog.dismiss();
            }
        }, 1500);


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.getFilter().filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return viewGroup;
    }


    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> implements Filterable  {

        private ArrayList<Post> arrayList;
        ArrayList<Post>  filteredList;
        private Context context;

        public CustomAdapter(ArrayList<Post> arrayList, Context context) {
            this.arrayList = arrayList;
            this.context = context;
            this.filteredList = arrayList;
        }

        @NonNull
        @Override
        public CustomAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            CustomAdapter.CustomViewHolder holder = new CustomAdapter.CustomViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull CustomAdapter.CustomViewHolder holder, int position) {

            int num = filteredList.get(position).num;
            Log.e("tag", String.valueOf(num));
            String postKey = keys.get(num);

            Glide.with(holder.itemView)
                    .load(filteredList.get(position).logo)
                    .into(holder.list_item_logo);
            holder.list_item_title.setText(filteredList.get(position).title);
            holder.list_item_title.setTextColor(Color.BLACK);
            holder.list_item_depart.setText(String.valueOf(filteredList.get(position).depart));
            holder.list_item_arrival.setText(String.valueOf(filteredList.get(position).arrival));
            holder.list_item_content.setText(filteredList.get(position).timestamp);
            holder.list_item_content.setTextColor(Color.GRAY);
            holder.list_item_comment_number.setText(String.valueOf(filteredList.get(position).commentCount));


            if(filteredList.get(position).title.equals("삭제된 게시글입니다")){
                holder.list_item_title.setTextColor(Color.GRAY);
                holder.list_item_content.setTextColor(Color.WHITE);
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
                                int num = filteredList.get(position).num;
                                Intent intent = new Intent(getActivity(), SearchPeopleBoard.class);
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
            return (filteredList != null ? filteredList.size() : 0);
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    String str = constraint.toString();
                    if (str.isEmpty()) {
                        filteredList = arrayList;
                        Log.e("tag", "safs");
                    } else {
                        List<Post> filteringList = new ArrayList<>();
                        for (Post post : arrayList) {
                            if (post.depart.contains(str)) {
                                filteringList.add(post);
                                Log.e("tag", post.depart);
                            }
                        }
                        filteredList = (ArrayList<Post>) filteringList;
                    }
                    FilterResults results = new FilterResults();
                    results.values = filteredList;
                    return results;
                }


                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filteredList = (ArrayList<Post>) results.values;
                    adapter.notifyDataSetChanged();
                }
            };
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

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.sp_action_button:
                    Intent intent = new Intent(getActivity(), SearchPeopleEdit.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    private void myStartActivity(Class c){
        Intent intent=new Intent(getActivity(),c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        customProgressDialog.setCancelable(false);

        if(allowRefresh){
            allowRefresh = false;
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
            customProgressDialog.dismiss();
        }
    }



}