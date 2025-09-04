package com.beladsoft.phone_background.VIEW.ACTIVITIES;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.beladsoft.phone_background.MODEL.Constants;
import com.beladsoft.phone_background.Objects.ObjectWallpapers;
import com.beladsoft.phone_background.FRAMWORK.Provider;
import com.beladsoft.phone_background.R;
import com.beladsoft.phone_background.VIEW.ADAPTERS.AdapterWallpapers;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

public class WallpapersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdapterWallpapers adapterWallpapers;
    private ProgressBar progressBar;
    private StorageReference sr;
    private DatabaseReference dr;
    private String folder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpapers);

        sr = FirebaseStorage.getInstance().getReference();
        dr = FirebaseDatabase.getInstance().getReference();
        folder = getIntent().getStringExtra("FOLDER");

        this.setTitle(folder);

        recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new GridLayoutManager(this,
                Provider.calculateNoOfColumns(this, Constants.IMG_WIDTH_FOR_RV_LIST)));
        recyclerView.hasFixedSize();
        adapterWallpapers = new AdapterWallpapers(WallpapersActivity.this, sr, dr);
        recyclerView.setAdapter(adapterWallpapers);
        progressBar = findViewById(R.id.pbLoading);

        loadImages();
    }

    private void loadImages() {
        sr.child(folder).listAll().addOnSuccessListener(this::onSuccess).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void onSuccess(ListResult listResult) {
        for (StorageReference reference : listResult.getItems()) {
            if (reference.getName().contains("background")) continue;
            ObjectWallpapers objectWallpapers = new ObjectWallpapers();
            objectWallpapers.setFav(Provider.isFav(WallpapersActivity.this, folder + "_" + reference.getName()));
            objectWallpapers.setImg(reference.getName());
            objectWallpapers.setFolder(folder);
            objectWallpapers.setViewType(1);
            adapterWallpapers.addItem(objectWallpapers);
        }
        progressBar.setVisibility(View.GONE);
    }
}