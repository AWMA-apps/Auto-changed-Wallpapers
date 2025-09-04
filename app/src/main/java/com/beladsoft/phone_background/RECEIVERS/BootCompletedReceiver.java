package com.beladsoft.phone_background.RECEIVERS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_REBOOT)
                || action.equals(("android.intent.action.QUICKBOOT_POWERON")))
            context.registerReceiver(new MyReceiver(), new IntentFilter(Intent.ACTION_BOOT_COMPLETED));
    }
}