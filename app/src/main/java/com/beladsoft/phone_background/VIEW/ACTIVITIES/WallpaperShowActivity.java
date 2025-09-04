package com.beladsoft.phone_background.VIEW.ACTIVITIES;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.beladsoft.phone_background.MODEL.Constants;
import com.beladsoft.phone_background.FRAMWORK.Provider;
import com.beladsoft.phone_background.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;


public class WallpaperShowActivity extends AppCompatActivity {

    private ImageView ivImage, ivClose;
    private TextView tvFav, tvSetWallpaper, tvDownload, tvMSG;
    private String path, folder, img, imgUrl;
    private ProgressBar pbDownloadingImageToStorage;
    private StorageReference sr;
    private LinearLayout llPB;
    private boolean setWallpaper, hasDownloaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_wallpaper_show);
        sr = FirebaseStorage.getInstance().getReference();

        path = getIntent().getStringExtra("PATH");// = towns/town1.jpg
        //imgUrl = getIntent().getStringExtra("URI");
        //hasDownloaded = getIntent().getBooleanExtra("DOWNLOADED", false);
        assert path != null;
        folder = path.split("/")[0];
        img = path.split("/")[1];

        ivImage = findViewById(R.id.iv);
        ivClose = findViewById(R.id.ivClose);
        tvFav = findViewById(R.id.tvFav);
        tvSetWallpaper = findViewById(R.id.tvSetWallpaper);
        tvDownload = findViewById(R.id.tvDownload);
        tvMSG = findViewById(R.id.tvMSG);
        pbDownloadingImageToStorage = findViewById(R.id.pbDownloading);
        llPB = findViewById(R.id.llPB);

        // download manager
        imageIsDownloaded();
        if (!hasDownloaded) {
            imgUrl = new Provider().getUrl(this, folder, img);
        }

        // show image
        if (imgUrl == null) {
            sr.child(path).getDownloadUrl().addOnSuccessListener(uri -> {
                imgUrl = uri.toString();
                new Provider().setUrl(WallpaperShowActivity.this, folder, img, uri);
                glideImage(imgUrl);
            }).addOnFailureListener(e -> tvMSG.setText(getString(R.string.failedToLoadImage)));
        } else {
            glideImage(imgUrl);
        }

        // close page
        ivClose.setOnClickListener(view -> this.finish());

        // fav manager
        if (Provider.isFav(this, folder + "_" + img))
            tvFav.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    ContextCompat.getDrawable(this, R.drawable.ic_baseline_favorite_24), null, null, null
            );
        tvFav.setOnClickListener(view -> {
            new Provider().addRemoveFav(
                    FirebaseDatabase.getInstance().getReference(),
                    folder, WallpaperShowActivity.this,
                    Provider.isFav(WallpaperShowActivity.this, folder + "_" + img), img, tvFav
            );
        });
        //setFavTV();
        new Provider().setTV(FirebaseDatabase.getInstance().getReference(),
                folder, WallpaperShowActivity.this, tvFav
                , img);


        //set wallpaper
        tvSetWallpaper.setOnClickListener(view -> {
            setWallpaper = true;
            tvSetWallpaper.setEnabled(false);
            if (hasDownloaded) setWallpaper(imgUrl);
            else DownloadImage();
        });

    }

    private void imageIsDownloaded() {
        File imageFile = new File(Environment.getExternalStorageDirectory()
                + "/DCIM/" + Constants.IMG_FOLDER_NAME + "/"+ img);
        this.hasDownloaded = imageFile.exists();
        if (hasDownloaded) {
            tvDownload.setTextColor(ContextCompat.getColor(this, R.color.green));
            tvDownload.setShadowLayer(0, 0, 0, ContextCompat.getColor(this, R.color.transparet));
            tvDownload.setEnabled(false);
            imgUrl = imageFile.getAbsolutePath();
        } else {
            tvDownload.setOnClickListener(view -> {
                setWallpaper = false;
                DownloadImage();
            });
        }
    }

    private void setWallpaper(String path) {
        tvSetWallpaper.setEnabled(false);
        WallpaperManager wm = WallpaperManager.getInstance(this);
        try {
            wm.setBitmap(BitmapFactory.decodeFile(path));
            Toast.makeText(this, R.string.done, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
            tvSetWallpaper.setEnabled(true);
        }
    }

    private void glideImage(String imgUrl) {
        Glide.with(this).load(imgUrl).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                tvMSG.setText(getString(R.string.failedToLoadImage));
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                tvMSG.setVisibility(View.GONE);
                return false;
            }
        }).into(ivImage);
    }


    private void DownloadImage() {
        // request permission
        if (!new Provider().requestPermission(this,this)) return;
        //requestPermis();
        tvDownload.setEnabled(false);
        llPB.setVisibility(View.VISIBLE);
        File rootPath = new File(Environment.getExternalStorageDirectory() + "/DCIM", Constants.IMG_FOLDER_NAME);
        if (!rootPath.exists())
            rootPath.mkdirs();

        File localFile;
        if (setWallpaper) localFile = new File(rootPath, "wallpaper.jpg");
        else localFile = new File(rootPath,  img);

        StorageReference imgRef = sr.child(path);
        imgRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    if (setWallpaper) {
                        setWallpaper(localFile.getPath());
                    } else {
                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                    }
                    llPB.setVisibility(View.INVISIBLE);
                    tvDownload.setEnabled(true);
                }).addOnProgressListener(snapshot -> pbDownloadingImageToStorage.setProgress(
                        (int) (snapshot.getBytesTransferred() / snapshot.getTotalByteCount() * 100)
                ))
                .addOnFailureListener(e -> {
                    llPB.setVisibility(View.INVISIBLE);
                    tvDownload.setEnabled(true);
                    tvSetWallpaper.setEnabled(true);
                    Toast.makeText(this, "Failed to download image\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    private boolean requestPermis() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permissionNecessary, Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 901);
                return false;
            } else return true;
        else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permissionNecessary, Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,
                        new String[]
                                {Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}, 901);
                return false;
            } else return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "PERMISSION_GRANTED", Toast.LENGTH_SHORT).show();
            DownloadImage();
        }
    }
}