package com.beladsoft.phone_background.RECEIVERS;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.beladsoft.phone_background.MODEL.Constants;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class MyReceiver extends BroadcastReceiver {
    Context context;
    String TAG = "WorkManager";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        String action = intent.getAction();
        Log.w(TAG,"onReceive... "+action);

        if (action.contains("DOWNLOAD")) { // action = 'DOWNLOAD:FOLDER/IMAGE.JPG:0'
            // DOWNLOAD AND SET WALLPAPER
            String folder_image = action.split(":")[1];

            File rootPath = new File(Environment.getExternalStorageDirectory() +
                    "/DCIM", Constants.IMG_FOLDER_NAME);
            if (!rootPath.exists())
                rootPath.mkdirs();
            File localFile = new File(rootPath, "wallpaper.jpg");

            Log.w(TAG, "DownloadNewWallpaper ... Path= " + localFile.getPath());
            // download from storage...
            StorageReference imgRef = FirebaseStorage.getInstance().getReference();

            imgRef.child(folder_image).getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG,"Downloading image has succeed");
                        setWallpaper(localFile.getPath(), context,Integer.parseInt(action.split(":")[2]));
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG,"Downloading Image Failed: \n"+e.getLocalizedMessage());
                        //context.startService(new Intent(context, WallpaperAutoChangerService.class));
                    });
        }
    }

    private void setWallpaper(String path, Context context,int as) {
        Log.w(TAG, "setting wallpaper ... Path= " + path + " AS: " + as);
        WallpaperManager wm = WallpaperManager.getInstance(context);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (as == 0)
                    wm.setBitmap(BitmapFactory.decodeFile(path), null, false, WallpaperManager.FLAG_SYSTEM);
                else if (as == 1)
                    wm.setBitmap(BitmapFactory.decodeFile(path), null, false, WallpaperManager.FLAG_LOCK);
                else
                    wm.setBitmap(BitmapFactory.decodeFile(path));
            } else wm.setBitmap(BitmapFactory.decodeFile(path));
            //setSharedPrefTime(context, System.currentTimeMillis());
            //setNewAlarm(context);
            Log.d(TAG, "Wallpaper setting finished...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}