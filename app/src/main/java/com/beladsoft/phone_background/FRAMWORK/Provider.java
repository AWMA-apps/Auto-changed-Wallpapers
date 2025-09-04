package com.beladsoft.phone_background.FRAMWORK;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.beladsoft.phone_background.MODEL.Constants;
import com.beladsoft.phone_background.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Provider {
    public static final String FAV_PREF = "FAVORITES";
    public final String IMG_URIS = "IMG_URIS";

    public Provider() {
    }

    public static int calculateNoOfColumns(Context context, float columnWidthDp) { // For example columnWidthdp=180
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int.
        return noOfColumns;
    }


    public String getUrl(Context context, String folder, String img) {
        SharedPreferences preferences = context.getSharedPreferences(IMG_URIS, Context.MODE_PRIVATE);
        return preferences.getString(folder + img, null);
    }

    public void setUrl(Context context, String folder, String img, Uri uri) {
        SharedPreferences.Editor editor = context.getSharedPreferences(IMG_URIS, Context.MODE_PRIVATE).edit();
        editor.putString(folder + img, uri.toString()).apply();
    }

    public static void FavManager(Context context, String folder_img) {
        SharedPreferences preferences = context.getSharedPreferences(FAV_PREF, Context.MODE_PRIVATE);
        List<String> favs = new ArrayList<>(Arrays.asList(preferences.getString(FAV_PREF, "").split(",")));
        if (favs.contains(folder_img)) favs.remove(folder_img);
        else favs.add(folder_img);
        StringBuilder sb = new StringBuilder();
        for (String img : favs) sb.append(img).append(",");
        preferences.edit().putString(FAV_PREF, sb.toString()).apply();
    }

    public static boolean isFav(Context context, String img) {
        SharedPreferences preferences = context.getSharedPreferences(FAV_PREF, Context.MODE_PRIVATE);
        try {
            return preferences.getString(FAV_PREF, "").contains(img);
        } catch (Exception e) {
            return false;
        }
    }

    public String[] favImages(@NonNull Context context) {
        return context.getSharedPreferences(FAV_PREF, Context.MODE_PRIVATE).getString(FAV_PREF, "").split(",");
    }

    public static String validChild(String child) {
        StringBuilder sb = new StringBuilder();
        for (char c : child.toCharArray()) {
            if (c == '.' || c == '#' || c == '/') sb.append("_");
            else sb.append(c);
        }
        return sb.toString();
    }

    public void addRemoveFav(DatabaseReference dr, String folder, Context context, boolean isFav, String img, TextView tvFav) {
        Integer favCount = Integer.parseInt(tvFav.getText().toString());
        if (isFav) {//cancel -1
            if (favCount > 0)
                dr.child(folder).child(Provider.validChild(img)).child("fav").setValue(favCount - 1).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        tvFav.setText(String.valueOf(favCount - 1));
                        Provider.FavManager(context, img);
                        tvFav.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_favorite_border), null, null, null);
                    }
                });
        } else // Add
            dr.child(folder).child(Provider.validChild(img)).child("fav").setValue(favCount + 1).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    tvFav.setText(String.valueOf(favCount + 1));
                    Provider.FavManager(context, img);
                    tvFav.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_24), null, null, null);
                }
            });
    }

    public void setTV(DatabaseReference dr, String folder, Context context, TextView tvFav, String img) {
        dr.child(folder).child(Provider.validChild(img)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Integer favCount = snapshot.child("fav").getValue(Integer.class);
                    if (favCount != null) tvFav.setText(favCount + "");
                    if (Provider.isFav(context, img))
                        tvFav.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_24), null, null, null);
                    else
                        tvFav.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_favorite_border), null, null, null);
                } catch (Exception e) {
                    Toast.makeText(context, "Error get fav\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error get favorites\n" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String getPath(String docPath) {
        //    /tree/primary:ADM/subfolder or /tree/B97E-1c08:DCIM/Camera/img.jpg
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            String[] parts = docPath.split("/");//= tree,B98-E56:DCIM,Camera,img.png
            StringBuilder PathSB = new StringBuilder();
            for (int x = 0; x < parts.length; x++) {// length=4
                if (x == parts.length - 1) continue;//3
                PathSB.append("/").append(parts[x]);//= /tree/B98-E45:DCIM/Camera
            }
            docPath = PathSB.toString();
        }
        String[] pathTerms = docPath.split(":");///tree/primary & DCIM/Camera
        String finalPath = "";
        if (pathTerms[0].contains("primary")) {
            // Internal Storage
            try {
                finalPath = "/storage/emulated/0/" + pathTerms[1];
            } catch (Exception e) {
                finalPath = "/storage/emulated/0/";
            }
        } else {
            String[] term1 = pathTerms[0].split("/");// = _ , tree , BC98-ER5
            try {
                finalPath = "/storage/" + term1[term1.length - 1] + "/" + pathTerms[1];
            } catch (Exception e) {
                finalPath = "/storage/" + term1[term1.length - 1] + "/";
            }
        }
        return finalPath;
    }

    public static FilenameFilter imageFilter = (dir, name) -> (name.endsWith(".jpg") || (name.endsWith(".jpeg")) || name.endsWith(".png") || name.endsWith(".heic"));

    public boolean requestPermission(Context context, Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, R.string.permissionNecessary, Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 901);
                return false;
            } else return true;
        else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, R.string.permissionNecessary, Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}, 901);
                return false;
            } else return true;
        }
    }

    public static int getImageNumber(String folder_name, Context c) {
        SharedPreferences sp = c.getSharedPreferences("SELECTED_FOLDER", Context.MODE_PRIVATE);
        int num = sp.getInt(folder_name, 1);// TOWNS:3
        sp.edit().putInt(folder_name, num + 1).apply();
        return num;
    }

    public static void loadTranslates(Context context) {
        FirebaseDatabase fd = FirebaseDatabase.getInstance();
        DatabaseReference dr = fd.getReference();
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        dr.child("translation").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        Log.d("WorkManager",ds.getKey());
                        editor.putString(ds.getKey(), ds.getValue().toString()).apply();
                    } catch (Exception e) {
                        Log.e("WorkManager", e.getMessage());
                    }
                }
                Log.d("WorkManager", "translation caught...");
                /* translation:
                 *        towns: 'ar:مدن'
                 *        animals: 'ar:حيوان,fr:anamel'*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static String getTheTranslation(String word, String ln, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_DATA, Context.MODE_PRIVATE);
        String translation = sharedPreferences.getString(word, null);
        if (translation == null) {
            loadTranslates(context);
            return word;
        } else {
            String[] all = translation.split(","); /*animals: 'ar:حيوان,fr:anamel'*/
            for (String s : all) {//fr:anamel'
                if (s.split(":")[0].equals(ln)) return s.split(":")[1];
            }
            return word;
        }
    }
}
