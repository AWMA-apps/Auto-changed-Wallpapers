package com.beladsoft.phone_background.ui;

import android.content.Context;
import android.hardware.lights.LightState;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beladsoft.phone_background.Objects.ObjectWallpapers;
import com.beladsoft.phone_background.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AdapterFavAndDownloads extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<ObjectWallpapers> list = new ArrayList<>();
    Context context;
    StorageReference sr;
    DatabaseReference dr;
    String folder;

    public AdapterFavAndDownloads(List<ObjectWallpapers> list, Context context,
                                  StorageReference sr, DatabaseReference dr, String folder) {
        this.list = list;
        this.context = context;
        this.sr = sr;
        this.dr = dr;
        this.folder = folder;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1)
            return new FavVH(LayoutInflater.from(context).inflate(R.layout.item_images, parent, false));
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).getViewType();
    }

    class FavVH extends RecyclerView.ViewHolder {
        ImageView img1;
        TextView tvFav1;

        public FavVH(@NonNull View itemView) {
            super(itemView);
            img1 = itemView.findViewById(R.id.img1);
            tvFav1 = itemView.findViewById(R.id.tvFav1);
        }
    }
}
