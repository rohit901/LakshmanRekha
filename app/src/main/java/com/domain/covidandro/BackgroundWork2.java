package com.domain.covidandro;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.androidhiddencamera.HiddenCameraFragment;

public class BackgroundWork2 extends Worker {

    private HiddenCameraFragment mHiddenCameraFragment;
    public BackgroundWork2(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {


        //Toast.makeText(getApplicationContext(), "Work Started.", Toast.LENGTH_SHORT).show();
        if (mHiddenCameraFragment != null) {    //Remove fragment from container if present
            ((FragmentActivity)getApplicationContext()).
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(mHiddenCameraFragment)
                    .commit();
            mHiddenCameraFragment = null;
        }

       // getApplicationContext().startService(new Intent(getApplicationContext(), DemoCamService.class));

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            getApplicationContext().startForegroundService(new Intent(getApplicationContext(), DemoCamService.class));
//        } else {
//            getApplicationContext().startService(new Intent(getApplicationContext(), DemoCamService.class));
//        }


        getApplicationContext().startService(new Intent(getApplicationContext(), DemoCamService.class));



        return Result.success();
    }
}
