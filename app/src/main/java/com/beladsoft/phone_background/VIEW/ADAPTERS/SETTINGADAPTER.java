package com.beladsoft.phone_background.VIEW.ADAPTERS;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beladsoft.phone_background.R;

import java.util.ArrayList;
import java.util.List;

public class SETTINGADAPTER extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public SETTINGADAPTER() {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        List<String> listOfCategories = new ArrayList<>();
        listOfCategories.add("one");

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class Items extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTextView;

        public Items(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvTextView = itemView.findViewById(R.id.tvTextView);
        }
    }
}
