package com.example.covidandro;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.androidhiddencamera.HiddenCameraFragment;

public class BackgroundWork extends Worker {

    private HiddenCameraFragment mHiddenCameraFragment;
    public BackgroundWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static boolean isDeviceLocked(Context context) {
        boolean isLocked = false;

        // First we check the locked state
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean inKeyguardRestrictedInputMode = keyguardManager.inKeyguardRestrictedInputMode();

        if (inKeyguardRestrictedInputMode) {
            isLocked = true;

        } else {
            // If password is not set in the settings, the inKeyguardRestrictedInputMode() returns false,
            // so we need to check if screen on for this case

            PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                isLocked = !powerManager.isInteractive();
            } else {
                //noinspection deprecation
                isLocked = !powerManager.isScreenOn();
            }
        }


        return isLocked;
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

        if(!isDeviceLocked(getApplicationContext())) {

            // getApplicationContext().startService(new Intent(getApplicationContext(), DemoCamService.class));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplicationContext().startForegroundService(new Intent(getApplicationContext(), DemoCamService.class));
            } else {
                getApplicationContext().startService(new Intent(getApplicationContext(), DemoCamService.class));
            }


            return Result.success();
        }else{

            return Result.success();

        }
    }
}
