package com.example.seatpool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CustomAdaptertest extends RecyclerView.Adapter<CustomAdaptertest.CustomViewHolder> {


    private ArrayList<Post> arrayList;
    private Context context;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    public CustomAdaptertest(ArrayList<Post> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        CustomViewHolder holder = new CustomViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
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
