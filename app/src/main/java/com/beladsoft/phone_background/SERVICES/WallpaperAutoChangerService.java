package com.beladsoft.phone_background.SERVICES;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;

import com.beladsoft.phone_background.RECEIVERS.MyReceiver;

public class WallpaperAutoChangerService extends Service {
    public WallpaperAutoChangerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        registerReceiver(new MyReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        return null;
    }
}