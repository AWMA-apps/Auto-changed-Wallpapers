package com.beladsoft.phone_background.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.beladsoft.phone_background.Objects.HomeObjects;
import com.beladsoft.phone_background.R;
import com.beladsoft.phone_background.VIEW.ACTIVITIES.WallpapersActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterHome extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<HomeObjects> list = new ArrayList();
    Context context;
    StorageReference sr;

    public AdapterHome(Context context) {
        this.context = context;
        sr = FirebaseStorage.getInstance().getReference();
    }

    public AdapterHome(Context context, StorageReference sr) {
        this.context = context;
        this.sr = sr;
    }

    public void addItem(HomeObjects object) {
        list.add(0, object);
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1)
            return new ItemHolder(LayoutInflater.from(context).inflate(R.layout.item_home_rv, parent, false));
        else return new SquareImages(LayoutInflater.from(context).inflate(R.layout.item_folders,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HomeObjects var = list.get(position);
        if (var.viewType == 1) {
            ItemHolder hold = (ItemHolder) holder;
            sr.child(var.itemTitle).child("background.jpg").getDownloadUrl().addOnCompleteListener(task -> {
                try {
                    Glide.with(context).load(task.getResult()).into(hold.iv);
                    Glide.with(context).load(task.getResult()).into(hold.civ);
                }catch (Exception e){
                    Glide.with(context).load(R.drawable.ic_launcher_background).into(hold.iv);
                    Glide.with(context).load(R.drawable.ic_launcher_background).into(hold.civ);
                }
            });
            hold.text.setText(var.itemTitle);
            hold.cardView.setOnClickListener(v -> {
                context.startActivity(new Intent(context, WallpapersActivity.class).putExtra("FOLDER",var.itemTitle));
            });
        }else {
            SquareImages hold= (SquareImages) holder;
            sr.child(var.itemTitle).child("background.jpg").getDownloadUrl().addOnCompleteListener(task -> {
                try {
                    Glide.with(context).load(task.getResult()).into(hold.ivFolder);
                }catch (Exception e){
                    Glide.with(context).load(R.drawable.ic_launcher_background).into(hold.ivFolder);
                }
            });
            hold.ivFolder.setOnClickListener(v -> {
                context.startActivity(new Intent(context, WallpapersActivity.class).putExtra("FOLDER",var.itemTitle));
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).viewType;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        CircleImageView civ;
        ImageView iv;
        TextView text;
        CardView cardView;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            civ = itemView.findViewById(R.id.civ);
            iv = itemView.findViewById(R.id.iv);
            text = itemView.findViewById(R.id.text);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    class SquareImages extends RecyclerView.ViewHolder{

        ImageView ivFolder;
        public SquareImages(@NonNull View itemView) {
            super(itemView);
            ivFolder=itemView.findViewById(R.id.ivFolderImage);
        }
    }
}
