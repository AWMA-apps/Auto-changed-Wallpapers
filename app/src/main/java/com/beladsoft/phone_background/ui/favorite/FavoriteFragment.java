package com.beladsoft.phone_background.ui.favorite;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beladsoft.phone_background.VIEW.ADAPTERS.AdapterWallpapers;
import com.beladsoft.phone_background.MODEL.Constants;
import com.beladsoft.phone_background.Objects.ObjectWallpapers;
import com.beladsoft.phone_background.FRAMWORK.Provider;
import com.beladsoft.phone_background.R;
import com.beladsoft.phone_background.databinding.FragmentFavoriteBinding;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.List;

public class FavoriteFragment extends Fragment {

    private FragmentFavoriteBinding binding;
    private AdapterWallpapers adapter;
    private Context context;
    private TextView tvHint;
    private RecyclerView recyclerView;

    public FavoriteFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = getContext();
        StorageReference sr = FirebaseStorage.getInstance().getReference();
        recyclerView = root.findViewById(R.id.rvFav);
        tvHint = root.findViewById(R.id.tvFavHint);
        recyclerView.setLayoutManager(new GridLayoutManager(context, Provider.calculateNoOfColumns(context,
                Constants.IMG_WIDTH_FOR_RV_LIST)));
        recyclerView.hasFixedSize();

        adapter = new AdapterWallpapers(context, sr,
                FirebaseDatabase.getInstance().getReference());
        recyclerView.setAdapter(adapter);

        loadFavImages();
        return root;
    }

    private void loadFavImages() {
        List<String> favImages = Arrays.asList(
                new Provider().favImages(context)
        );//FOLDER_FOLDER (1).jpg,Folder2_Folder2 (1).jpg

        if (favImages.size() > 0) {
            for (String f_img : favImages) {
                if (f_img.length() < 3 || f_img.length() > 30) continue;
                try {
                    ObjectWallpapers o = new ObjectWallpapers();
                    //Toast.makeText(context, f_img, Toast.LENGTH_SHORT).show();
                    o.setFav(true);
                    o.setFolder(f_img.split("_")[0]);
                    o.setImg(f_img);
                    o.setViewType(2);
                    adapter.addItem(o);
                } catch (Exception e) {

                }
            }
        } else
            tvHint.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}