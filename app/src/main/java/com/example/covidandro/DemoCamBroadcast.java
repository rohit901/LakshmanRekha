package com.example.covidandro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

public class DemoCamBroadcast extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        //Toast.makeText(context, "Hi901: "+context.toString(), Toast.LENGTH_LONG).show();
        //context.startForegroundService(new Intent(context, DemoCamService.class));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, DemoCamService.class));
        } else {
            context.startService(new Intent(context, DemoCamService.class));
        }



    }
}
