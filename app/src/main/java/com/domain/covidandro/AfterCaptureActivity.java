package com.domain.covidandro;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.domain.covidandro.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.wooplr.spotlight.SpotlightView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AfterCaptureActivity extends AppCompatActivity {
    Handler handler;
    String imageFilePath = "";
    String id;
    FusedLocationProviderClient mFusedLocationClient;
    double usrLat = 0.0;
    double usrLong = 0.0;
    private RequestQueue rQueue;
    private String upload_URL = "http://piyush16.pythonanywhere.com/upload";
    public static final String SHARED_PREF = "com.domain.covidandro";
    private SpotlightView spotLight;
    Button sendPost;
    TextView resultTV;
    boolean isFirstTime = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_capture);
        sendPost = (Button) findViewById(R.id.sendPost);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        resultTV = (TextView) findViewById(R.id.alarmTV_ac);
        SharedPreferences mPrefs = getSharedPreferences(SHARED_PREF, MODE_PRIVATE); //add key
        isFirstTime = mPrefs.getBoolean("first_time", true);


        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        resultTV.setText(intent.getStringExtra(Camera2Service.EXTRA_RESULT));
                    }
                }, new IntentFilter(Camera2Service.ACTION_RESULT_BROADCAST)
        );

        if(isFirstTime) {
            sendPost.setVisibility(View.GONE);
        }

        sendPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//
//
//
//                    }
//                },2000);


//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    startForegroundService(new Intent(MainActivity.this, Camera2Service.class));
//                } else {
//                    startService(new Intent(MainActivity.this, Camera2Service.class));
//                }

                startService(new Intent(AfterCaptureActivity.this, Camera2Service.class));


                //startService(new Intent(MainActivity.this, DemoCamService.class));
            }
        });


        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                File[] files = dir.listFiles();
                Log.d("Files", "Size: " + files.length);

                if (isFirstTime) {

                    if (files.length == 0) {
                        Toast.makeText(AfterCaptureActivity.this, "Image Capture Failed, Try Again.", Toast.LENGTH_SHORT).show();
                    } else if (files.length > 0) {
                        Toast.makeText(AfterCaptureActivity.this, "Capture Success.", Toast.LENGTH_SHORT).show();
                        imageFilePath = files[0].getAbsolutePath();
                        //Log.d("files901",imageFilePath);
                        id = UUID.randomUUID().toString();
                        registerData();
                    }
            }

            }
        },500);




    }

    private boolean isLocationEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
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

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void uploadImage(final Bitmap bitmap){

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, upload_URL,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Log.d("ressssssoo",new String(response.data));
                        rQueue.getCache().clear();

                        Toast.makeText(AfterCaptureActivity.this, new String(response.data), Toast.LENGTH_LONG).show();


                        SharedPreferences mPrefs = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
                        SharedPreferences.Editor prefsEditor = mPrefs.edit();
                        prefsEditor.putBoolean("first_time", false).apply();








                        sendPost.setVisibility(View.VISIBLE);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                spotLight = new SpotlightView.Builder(AfterCaptureActivity.this)
                                        .introAnimationDuration(800)
                                        .enableRevealAnimation(true)
                                        .performClick(true)
                                        .fadeinTextDuration(400)
                                        .headingTvColor(Color.parseColor("#eb273f"))
                                        .headingTvSize(32)
                                        .headingTvText("Captures Image In BG")
                                        .subHeadingTvColor(Color.parseColor("#ffffff"))
                                        .subHeadingTvSize(16)
                                        .subHeadingTvText("Front cam image is sent to server for face match.")
                                        .maskColor(Color.parseColor("#dc000000"))
                                        .target(sendPost)
                                        .lineAnimDuration(400)
                                        .lineAndArcColor(Color.parseColor("#eb273f"))
                                        .dismissOnTouch(true)
                                        .dismissOnBackPress(true)
                                        .enableDismissAfterShown(true)
                                        .usageId("sp2") //UNIQUE ID
                                        .show();
                            }
                        },1000);
                        prefsEditor.putString("usr_id",id);
                        prefsEditor.commit();

                        Log.d("901st", "Work Initiated");

                        PeriodicWorkRequest savePhotoRequest =
                                new PeriodicWorkRequest.Builder(BackgroundWork.class, 15, TimeUnit.MINUTES)
                                        .build();
                        WorkManager.getInstance().enqueue(savePhotoRequest);




                        //createNotif(new String(response.data));



//                        Intent intent = new Intent(ACTION_RESULT_BROADCAST);
//                        intent.putExtra(EXTRA_RESULT, new String(response.data));
//                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                // params.put("tags", "ccccc");  add string parameters

                SharedPreferences mPrefs = getSharedPreferences(SHARED_PREF, MODE_PRIVATE); //add key
                String name = "";
                String dob = "";
                String pin = "";
                String period = "";
                String startDate = "";
                String age = "";
                name = mPrefs.getString("name_str", "");
                dob = mPrefs.getString("dob_str", "");
                pin = mPrefs.getString("pin_str", "");
                period = mPrefs.getString("period_str", "");
                startDate = mPrefs.getString("start_str", "");
                String[] dobArray = new String[3];
                dobArray = dob.split("/");
                int year = Integer.parseInt(dobArray[2]);
                int month = Integer.parseInt(dobArray[1]);
                int day = Integer.parseInt(dobArray[0]);
                age = getAge(year,month-1,day);

                params.put("id",id); //user ID
                params.put("loc",usrLat+"@"+usrLong);
                params.put("age", age);
                params.put("dob", dob);
                params.put("pin", pin);
                params.put("name", name);
                params.put("period", period);
                params.put("startdate", startDate);

                return params;
            }

            /*
             *pass files using below method
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("image", new DataPart(imagename + ".jpeg", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };


        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        rQueue = Volley.newRequestQueue(getApplicationContext());
        rQueue.add(volleyMultipartRequest);
    }


    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private String getAge(int year, int month, int day){
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        String ageS = Integer.toString(age);

        return ageS;
    }


    private void registerData(){

        getLastLocation();
        final Bitmap[] gphoto = {null};


        Glide.with(AfterCaptureActivity.this).asBitmap().load(imageFilePath).into(new CustomTarget<Bitmap>(){
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                byte[] BYTE;
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                //resource.compress(Bitmap.CompressFormat.JPEG,50,bytes);
                //BYTE = bytes.toByteArray();
                Bitmap resource2;
                resource2 = getResizedBitmap(resource, 200);



                uploadImage(resource2);




            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }

        });


    }

}