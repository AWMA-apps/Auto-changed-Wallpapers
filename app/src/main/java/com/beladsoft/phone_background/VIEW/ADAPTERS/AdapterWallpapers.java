package com.beladsoft.phone_background.VIEW.ADAPTERS;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.beladsoft.phone_background.MODEL.Constants;
import com.beladsoft.phone_background.Objects.ObjectWallpapers;
import com.beladsoft.phone_background.FRAMWORK.Provider;
import com.beladsoft.phone_background.R;
import com.beladsoft.phone_background.VIEW.ACTIVITIES.WallpaperShowActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
public class AdapterWallpapers extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
   List<ObjectWallpapers> list = new ArrayList<>();
    Context context;
    StorageReference sr;
    DatabaseReference dr;

    public AdapterWallpapers(Context context, StorageReference sr, DatabaseReference dr) {
        this.context = context;
        this.sr = sr;
        this.dr = dr;
    }

    public AdapterWallpapers(Context context, DatabaseReference dr) {
        this.context = context;
        this.dr = dr;
    }

    public void addItem(ObjectWallpapers objectWallpapers) {
        list.add(0, objectWallpapers);
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1 || viewType == 2 || viewType == 3)
            return new VH(LayoutInflater.from(context).inflate(R.layout.item_images, parent, false));
        else return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ObjectWallpapers var = list.get(position);
        VH hold = (VH) holder;
        if (var.getViewType() == 1 || var.getViewType() == 2) {
            String img_uri = new Provider().getUrl(context, var.getFolder(), var.getImg());
            if (img_uri != null) {
                Glide.with(context).load(img_uri).into(hold.img1);
            } else {
                sr.child(var.getFolder()).child(var.getImg()).getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(context).load(uri).into(hold.img1);
                    new Provider().setUrl(context, var.getFolder(), var.getImg(), uri);
                    hold.img1.setOnClickListener(v -> {
                        context.startActivity(new Intent(context, WallpaperShowActivity.class)
                                .putExtra("PATH", var.getFolder() + "/" + var.getImg()));
                    });
                });
            }

            if (var.isFav())
                hold.tvFav1.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_24)
                        , null, null, null
                );

            //setTV(hold, var);
            new Provider().setTV(dr, var.getFolder(), context, hold.tvFav1, var.getImg());

            hold.tvFav1.setOnClickListener(v -> {
                new Provider().addRemoveFav(dr, var.getFolder(), context, Provider.isFav(context, var.getImg()), var.getImg(), hold.tvFav1);
            });

            hold.img1.setOnClickListener(v -> {
                context.startActivity(new Intent(context, WallpaperShowActivity.class)
                        .putExtra("PATH", var.getFolder() + "/" + var.getImg()));
            });
        } else if (var.getViewType() == 3) {
            File file = new File(Environment.getExternalStorageDirectory()
                    + "/DCIM", Constants.IMG_FOLDER_NAME);
            Glide.with(context).load(file.getPath() + "/" + var.getFolder() + "_" + var.getImg()).into(hold.img1);
            hold.img1.setOnClickListener(v -> {
                context.startActivity(new Intent(context, WallpaperShowActivity.class)
                        //.putExtra("URI", file.getPath() + "/" + var.getFolder() + "_" + var.getImg())
                        .putExtra("DOWNLOADED", true)
                        .putExtra("PATH", var.getFolder() + "/" + var.getFolder() + "_" + var.getImg()));
            });
            if (var.isFav())
                hold.tvFav1.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_24)
                        , null, null, null
                );
            //setTV(hold, var);
            new Provider().setTV(dr, var.getFolder(), context, hold.tvFav1, var.getImg());

            hold.tvFav1.setOnClickListener(v -> {
                new Provider().addRemoveFav(dr, var.getFolder(), context, Provider.isFav(context, var.getFolder() + "_" + var.getImg()), var.getImg(), hold.tvFav1);
            });
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).viewType;
    }

    class VH extends RecyclerView.ViewHolder {

        ImageView img1;
        TextView tvFav1;

        public VH(@NonNull View itemView) {
            super(itemView);
            img1 = itemView.findViewById(R.id.img1);
            tvFav1 = itemView.findViewById(R.id.tvFav1);
        }
    }
}
