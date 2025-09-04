package com.beladsoft.phone_background.ui.downloaded;


import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.beladsoft.phone_background.databinding.FragmentDownloadedBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

public class DownloadedFragment extends Fragment {

    private FragmentDownloadedBinding binding;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private AdapterWallpapers adapter;
    private Context context;
    private TextView tvHint;

    public DownloadedFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDownloadedBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = getContext();
        recyclerView = root.findViewById(R.id.rv);
        progressBar = root.findViewById(R.id.pbLoading);
        tvHint = root.findViewById(R.id.tvDHint);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new GridLayoutManager(context, Provider.calculateNoOfColumns(context,
                Constants.IMG_WIDTH_FOR_RV_LIST)));
        adapter = new AdapterWallpapers(context, FirebaseDatabase.getInstance().getReference());
        recyclerView.setAdapter(adapter);

        loadDownloadedImages();
        return root;
    }

    private void loadDownloadedImages() {
        File file = new File(Environment.getExternalStorageDirectory()
                + "/DCIM", Constants.IMG_FOLDER_NAME);
        if (file.isDirectory()) {
            String[] images = file.list(Provider.imageFilter);//FOLDER_FOLDER (X).JPG
            if (images != null && images.length > 0) {
                for (String img : images) {
                    try {
                        ObjectWallpapers o = new ObjectWallpapers();
                        o.setViewType(3);
                        o.setFav(Provider.isFav(context, img));
                        o.setFolder(img.split("_")[0]);
                        o.setImg(img.split("_")[1]);
                        adapter.addItem(o);
                    } catch (Exception e) {
                       // Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            } else tvHint.setVisibility(View.VISIBLE);
        } else {
            Snackbar.make(recyclerView, file.getPath() + "\nNOT file.isDirectory()", Snackbar.LENGTH_LONG);
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}