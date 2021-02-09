package com.domain.covidandro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.androidhiddencamera.HiddenCameraFragment;

import java.util.concurrent.TimeUnit;

public class DemoCamBroadcast extends BroadcastReceiver {
    private HiddenCameraFragment mHiddenCameraFragment;

    @Override
    public void onReceive(Context context, Intent intent) {


        PeriodicWorkRequest savePhotoRequest =
                new PeriodicWorkRequest.Builder(BackgroundWork.class, 15, TimeUnit.MINUTES)
                        .build();
        WorkManager.getInstance().enqueue(savePhotoRequest);



        //Toast.makeText(context, "Hi901: "+context.toString(), Toast.LENGTH_LONG).show();
        //context.startForegroundService(new Intent(context, DemoCamService.class));

//        if (mHiddenCameraFragment != null) {    //Remove fragment from container if present
//            ((FragmentActivity)context).
//                    getSupportFragmentManager()
//                    .beginTransaction()
//                    .remove(mHiddenCameraFragment)
//                    .commit();
//            mHiddenCameraFragment = null;
//        }
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(new Intent(context, DemoCamService.class));
//        } else {
//            context.startService(new Intent(context, DemoCamService.class));
//        }
//


    }
}
