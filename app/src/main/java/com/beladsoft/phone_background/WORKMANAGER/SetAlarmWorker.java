package com.beladsoft.phone_background.WORKMANAGER;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.beladsoft.phone_background.MODEL.Constants;
import com.beladsoft.phone_background.FRAMWORK.Provider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
public class SetAlarmWorker extends Worker {
    SharedPreferences pref;
    private final Context context;
    private String TAG = "WorkManager";
    private StorageReference sr;
    public SetAlarmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        pref = context.getSharedPreferences(Constants.SETTINGS_DATA, Context.MODE_PRIVATE);
        sr = FirebaseStorage.getInstance().getReference();
        Log.w(TAG, "SetAlarmWorker func");
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.w(TAG, "start func doWork");

        // start testing...

        File rootPath = new File(Environment.getExternalStorageDirectory() +
                "/DCIM", Constants.IMG_FOLDER_NAME);
        if (!rootPath.exists())
            rootPath.mkdirs();
        File localFile = new File(rootPath, "wallpaper.jpg");
        Log.w(TAG, "Path= " + localFile.getPath());

        final Result[] result = {Result.retry()};
        CountDownLatch countDownLatch = new CountDownLatch(1);
        FirebaseStorage fs = FirebaseStorage.getInstance();
        StorageReference sr = fs.getReference();
        return whichSource();
        // download from storage...
       /* sr.child("Animals").child("Animals_Animals (1).jpg").getFile(localFile)
                .addOnCompleteListener(taskSnapshot -> {
                    Log.w(TAG, "addOnCompleteListener");
                    if (taskSnapshot.isSuccessful()) {
                        Log.d(TAG, "Downloading image has succeed");
                        setWallpaper(localFile.getPath(), context);
                        result[0] = Result.success(new Data.Builder()
                                .putString("Key", "TRUE").build());
                    } else {
                        Log.e(TAG, "Downloading Image Failed: \n" + taskSnapshot.getException().getMessage());
                        result[0] = Result.retry();
                    }
                    countDownLatch.countDown();
                });
        try {
            countDownLatch.await();
            Log.w(TAG, "countDownLatch.await()");
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "InterruptedException: CountDownLatch " + e.getLocalizedMessage());
        }
        return result[0];

        //return whichSource();

        */
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }

    private Result whichSource() {
        int source = pref.getInt(Constants.SOURCE, 0);
        Log.w(TAG, "the source is ... " + source);
        if (source == 0) {// From App
            String[] categories = pref.getString(Constants.SELECTED_CATEGORIES, "Towns").split(",");
            String folder = categories[new Random().nextInt(categories.length)];
            int imgNumber = Provider.getImageNumber(folder,context);
            String image = folder + "_" + folder + " (" + (imgNumber) + ").jpg";// Towns_Towns22.jpg Towns_Towns (9)
            Log.w(TAG, "From : " + folder + "/" + image);
            return DownloadNewWallpaper(context, folder, image);
        } else if (source == 1) {// Downloaded
            LoadImageFromDownloadedImages(context);
            return Result.success();
        } else if (source == 2) {// Fav images
            return loadImageFromFav(context);
        } else {// Folder
            LoadImageFromSelectedFolder(context);
            return Result.success();
        }
    }

    private void LoadImageFromSelectedFolder(Context context) {
        String folderPath = pref.getString("FOLDER_PATH", "");
        File file = new File(folderPath);
        if (file.isDirectory()) {
            String[] images = file.list(new Provider().imageFilter);
            if (images != null && images.length > 0) {
                int r = new Random().nextInt(images.length);
                setWallpaper(file.getPath() + "/" + images[r], context);
            } else
                Toast.makeText(context, "No Images in " + file.getPath(), Toast.LENGTH_SHORT).show();
        } else Toast.makeText(context, "folder not directory", Toast.LENGTH_SHORT).show();
    }

    private Result loadImageFromFav(Context context) {
        List<String> favImages = Arrays.asList(context.getSharedPreferences(Provider.FAV_PREF, Context.MODE_PRIVATE)
                .getString(Provider.FAV_PREF, "").split(","));//FOLDER_image.jpg,FOLDER2_img.jpg,
        if (favImages.size() > 0) {
            int r = new Random().nextInt(favImages.size());
            String[] FOLDER_folder1 = favImages.get(r).split("_");
            return DownloadNewWallpaper(context, FOLDER_folder1[0], FOLDER_folder1[1]);
        } else return Result.success();
    }

    private void LoadImageFromDownloadedImages(Context context) {
        File file = new File(Environment.getExternalStorageDirectory()
                + "/DCIM", Constants.IMG_FOLDER_NAME);
        if (file.isDirectory()) {
            String[] images = file.list((dir, name) -> (name.endsWith(".jpg") ||
                    (name.endsWith(".jpeg")) || name.endsWith(".png")));
            if (images != null && images.length > 0) {
                int r = new Random().nextInt(images.length);
                setWallpaper(file.getPath() + "/" + images[r], context);
            }
        }
    }

    public boolean isNetworkAvailable(Context context) {
        String d_METHOD = "DMethod";
        int conType = pref.getInt(d_METHOD, 0);//both,wifi,mobile
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null) return false;
        if (activeNetworkInfo.isConnected())
            return (conType == 1 && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                    || (conType == 2 && activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
                    || conType == 0;
        else return false;
    }

    private Result DownloadNewWallpaper(Context context, String folder, String img) {
        // select image path to save in.
        Log.w(TAG, "Download... " + folder + "/" + img);
        //      context.registerReceiver(new MyReceiver(),
//                new IntentFilter("DOWNLOAD:" + folder + "/" + img+":"+pref.getInt(SET_AS, 2)));

        File rootPath = new File(Environment.getExternalStorageDirectory() +
                "/DCIM", Constants.IMG_FOLDER_NAME);
        if (!rootPath.exists())
            rootPath.mkdirs();
        File localFile = new File(rootPath, "wallpaper.jpg");


        Result[] result = {Result.retry()};
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Log.w(TAG, "DownloadNewWallpaper ... Path= " + localFile.getPath());
        // download from storage...
        sr.child(folder).child(img).getFile(localFile)
                .addOnCompleteListener(taskSnapshot -> {
                    Log.w(TAG, "addOnCompleteListener");
                    if (taskSnapshot.isSuccessful()) {
                        Log.d(TAG, "Downloading image has succeed");
                        setWallpaper(localFile.getPath(), context);
                        result[0] = Result.success(new Data.Builder()
                                .putString("Key", "TRue").build());
                    } else {
                        Log.e(TAG, "Downloading Image Failed: \n" +
                                taskSnapshot.getException().toString()+"\n\n"+
                                taskSnapshot.getException().getMessage()+"\n\n"+
                                taskSnapshot.getException().getLocalizedMessage());
                        result[0] = Result.retry();
                    }
                    countDownLatch.countDown();
                });
        try {
            countDownLatch.await();
            Log.w(TAG, "countDownLatch.await()");
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "InterruptedException: CountDownLatch " + e.getLocalizedMessage());
        }
        return result[0];
    }

    private void setWallpaper(String path, Context context) {
        String SET_AS = "SET_AS";
        int as = pref.getInt(SET_AS, 2);//0:home,1:lock,2:both
        Log.w(TAG, "setWallpaper ... Path= " + path + " AS: " + as);
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
