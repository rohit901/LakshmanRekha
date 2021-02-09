package com.domain.covidandro;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.wooplr.spotlight.SpotlightView;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    //private HiddenCameraFragment mHiddenCameraFragment;
    TextView alarmTV;
    Button uploadBtn;
    Boolean isFirstTime;
    Button sendPost;
    String regImgURL;
    private SpotlightView spotLight;

    private String upload_URL = "http://piyush16.pythonanywhere.com/upload";
    private RequestQueue rQueue;

    double usrLat = 0.0;
    double usrLong = 0.0;

    FusedLocationProviderClient mFusedLocationClient;



    private static final int PERMISSION_REQUEST_CODE = 200;
    public static final int APP_PERMISSION_REQUEST = 100;
    private static final int CAMERA_REQUEST = 100;
    public static final String SHARED_PREF = "com.domain.covidandro";
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
    String imageFilePath="";

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
        Date currentTime;
        currentTime = Calendar.getInstance().getTime();
        inImage.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title"+"_"+currentTime, null);
        return Uri.parse(path);
    }


    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }


    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }


    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }


    public static Bitmap getRotateImage(String photoPath, Bitmap bitmap) throws IOException {
        ExifInterface ei = new ExifInterface(photoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }

        return rotatedBitmap;

    }


    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFilePath = image.getAbsolutePath();
        return image;
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

                        Toast.makeText(MainActivity.this, new String(response.data), Toast.LENGTH_LONG).show();

                        uploadBtn.setVisibility(View.GONE);
                        SharedPreferences mPrefs = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
                        SharedPreferences.Editor prefsEditor = mPrefs.edit();
                        prefsEditor.putBoolean("first_time", false);








                        //sendPost.setVisibility(View.VISIBLE);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                spotLight = new SpotlightView.Builder(MainActivity.this)
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
                                        .target(uploadBtn)
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
                params.put("id",id); //user ID
                params.put("loc",usrLat+"@"+usrLong);
                params.put("age", "20");
                params.put("dob", "01/01/1990");
                params.put("pin", "410210");
                params.put("name", "rohit");
                params.put("period", "14");
                params.put("startdate", "02/06/2020");

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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {

            getLastLocation();
            final Bitmap[] gphoto = {null};

            //Bitmap photo = (Bitmap) data.getExtras().get("data");
            //Glide.with(this).load((Bitmap)data.getExtras().get("data")).into()
            //Bitmap gphoto = Glide.with(MainActivity.this).asBitmap().load()
               Glide.with(MainActivity.this).asBitmap().load(imageFilePath).into(new CustomTarget<Bitmap>(){
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                        gphoto[0] = resource;
//                        StorageReference ref
//                                = FirebaseStorage.getInstance().getReference()
//                                .child(
//                                        "images/"
//                                                + id);
                        byte[] BYTE;
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        //resource.compress(Bitmap.CompressFormat.JPEG,50,bytes);
                        //BYTE = bytes.toByteArray();
                        Bitmap resource2;
                     resource2 = getResizedBitmap(resource, 200);
//                        res3 = ImageUtils.getInstant().getCompressedBitmap(getImageUri(MainActivity.this,resource).getPath());
                        //resource2 = BitmapFactory.decodeByteArray(BYTE,0,BYTE.length);


                        uploadImage(resource2);

//                        ref.putFile(getImageUri(MainActivity.this, resource2)).addOnSuccessListener(
//                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                    @Override
//                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                        Toast.makeText(MainActivity.this, "Image Uploaded.", Toast.LENGTH_SHORT).show();
//                                        uploadBtn.setVisibility(View.GONE);
//
//                                    }
//                                }
//                        )
//                                .addOnFailureListener(
//                                        new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//                                                Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
//
//                                            }
//                                        }
//                                );



//                        final Handler handler = new Handler();
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                // Do something after 5s = 5000ms
//
//
//                                //Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
//                                SharedPreferences mPrefs = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
//                                SharedPreferences.Editor prefsEditor = mPrefs.edit();
//                                prefsEditor.putBoolean("first_time", false);
//                                sendPost.setVisibility(View.VISIBLE);
//                                prefsEditor.putString("usr_id",id);
//                                prefsEditor.commit();
//
//
////                                FirebaseStorage.getInstance().getReference()
////                                        .child("images/"+id).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
////                                    @Override
////                                    public void onSuccess(Uri uri) {
////
////                                        regImgURL = uri.toString();
////                                        //Toast.makeText(MainActivity.this, "URL: "+regImgURL, Toast.LENGTH_SHORT).show();
////
////                                        List<Double> loc = new ArrayList<Double>();
////                                        loc.add(usrLat);
////                                        loc.add(usrLong);
////                                        User usr = new User(loc,regImgURL);
////                                        mDatabase.child(id).setValue(usr).addOnSuccessListener(new OnSuccessListener<Void>() {
////                                            @Override
////                                            public void onSuccess(Void aVoid) {
////
////                                                errorTV.setText("Success Writing To DB");
////                                                Toast.makeText(MainActivity.this, "Success Writing to DB", Toast.LENGTH_SHORT).show();
////
////                                            }
////                                        }).addOnFailureListener(new OnFailureListener() {
////                                            @Override
////                                            public void onFailure(@NonNull Exception e) {
////                                                errorTV.setText("Failed, "+e.getMessage());
////                                                Toast.makeText(MainActivity.this, "Failed writing to DB: "+e.getMessage(), Toast.LENGTH_SHORT).show();
////                                            }
////                                        })
////                                        ;
////                                        SharedPreferences mPrefs = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
////                                        SharedPreferences.Editor prefsEditor = mPrefs.edit();
////                                        prefsEditor.putBoolean("first_time", false);
////                                        sendPost.setVisibility(View.VISIBLE);
////                                        prefsEditor.putString("usr_id",id);
////                                        prefsEditor.commit();
////
////                                    }
////                                }).addOnFailureListener(new OnFailureListener() {
////                                    @Override
////                                    public void onFailure(@NonNull Exception e) {
////                                        Toast.makeText(MainActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
////                                        Log.d("e901",e.getMessage());
////                                    }
////                                });
//
//
//                            }
//                        }, 3000);





                    }

                   @Override
                   public void onLoadCleared(@Nullable Drawable placeholder) {

                   }

               });

            Bitmap rotPhoto = null;
//            try {
//                Log.d("rot901","OK");
//                //rotPhoto = handleSamplingAndRotationBitmap(MainActivity.this,getImageUri(MainActivity.this,photo));
//                //rotPhoto = getRotateImage(imageFilePath,photo);
//            } catch (IOException e) {
//                Log.d("rot901",e.getMessage());
//                e.printStackTrace();
//            }



            Log.d("im901",Uri.parse(imageFilePath).toString());

//            ref.putFile(Uri.fromFile(new File(imageFilePath))).addOnSuccessListener(
//                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            Toast.makeText(MainActivity.this, "Image Uploaded.", Toast.LENGTH_SHORT).show();
//                            uploadBtn.setVisibility(View.GONE);
//
//                        }
//                    }
//            )
//                    .addOnFailureListener(
//                            new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                    );







            //startService(new Intent(this, DemoCamService.class));








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




    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, APP_PERMISSION_REQUEST);
        }

        requestMultiplePermissions();







        //errorTV = (TextView) findViewById(R.id.errorTV);

        if(!isOnline()){
            Toast.makeText(this, "Please Connect Device to Internet.", Toast.LENGTH_LONG).show();
        }



        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        uploadBtn = (Button)findViewById(R.id.uploadBtn);
        uploadBtn.setVisibility(View.GONE);
        sendPost = (Button) findViewById(R.id.sendPost_main);
        sendPost.setVisibility(View.GONE);


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

                startService(new Intent(MainActivity.this, Camera2Service.class));


                //startService(new Intent(MainActivity.this, DemoCamService.class));
            }
        });

        //mDatabase = FirebaseDatabase.getInstance().getReference("users");

        SharedPreferences mPrefs = getSharedPreferences(SHARED_PREF, MODE_PRIVATE); //add key
        isFirstTime = mPrefs.getBoolean("first_time", true);
        if(isFirstTime){
            uploadBtn.setVisibility(View.VISIBLE);
            //id = mDatabase.push().getKey();
            id = UUID.randomUUID().toString();
            uploadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

//                    if (isOnline()){
//
//
//                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                    if (cameraIntent.resolveActivity(getPackageManager()) != null) {
//                        File photoFile = null;
//                        try {
//                            photoFile = createImageFile();
//                            //Toast.makeText(MainActivity.this, "Photo registered!", Toast.LENGTH_SHORT).show();
//                        } catch (IOException e) {
//                            Toast.makeText(MainActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                            e.printStackTrace();
//                        }
//                        Uri photoUri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".provider", photoFile);
//                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
//                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
//                    }
//
//                }else{
//                        Toast.makeText(MainActivity.this, "Please connect Device to Internet.", Toast.LENGTH_LONG).show();
//                    }


                    Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                    startActivity(intent);



            }

            });







        }else {
            sendPost.setVisibility(View.VISIBLE);
        }



        alarmTV = (TextView) findViewById(R.id.alarmTV);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        alarmTV.setText(intent.getStringExtra(Camera2Service.EXTRA_RESULT));
                    }
                }, new IntentFilter(Camera2Service.ACTION_RESULT_BROADCAST)
        );



        //mStorageRef = FirebaseStorage.getInstance().getReference();

        final SharedPreferences.Editor pref =    getSharedPreferences("allow_notify", MODE_PRIVATE).edit();
        pref.apply();
        final SharedPreferences sp =    getSharedPreferences("allow_notify", MODE_PRIVATE);
        if(!sp.getBoolean("protected",false)) {
            for (final Intent intent : POWERMANAGER_INTENTS)
                if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.LLDialog));
                    builder.setTitle("Alert").setMessage("Keep App to Protected App List?")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(intent);
                                    sp.edit().putBoolean("protected", true).apply();
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            spotLight = new SpotlightView.Builder(MainActivity.this)
                                                    .introAnimationDuration(800)
                                                    .enableRevealAnimation(true)
                                                    .performClick(true)
                                                    .fadeinTextDuration(400)
                                                    .headingTvColor(Color.parseColor("#eb273f"))
                                                    .headingTvSize(32)
                                                    .headingTvText("Register")
                                                    .subHeadingTvColor(Color.parseColor("#ffffff"))
                                                    .subHeadingTvSize(16)
                                                    .subHeadingTvText("Enter your details &\nCapture Your Photo.")
                                                    .maskColor(Color.parseColor("#dc000000"))
                                                    .target(uploadBtn)
                                                    .lineAnimDuration(400)
                                                    .lineAndArcColor(Color.parseColor("#eb273f"))
                                                    .dismissOnTouch(true)
                                                    .dismissOnBackPress(true)
                                                    .enableDismissAfterShown(true)
                                                    .usageId("sp1") //UNIQUE ID
                                                    .show();
                                        }
                                    },1000);


                                }
                            })
                            .setCancelable(false)
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




//        if (mHiddenCameraFragment != null) {    //Remove fragment from container if present
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .remove(mHiddenCameraFragment)
//                    .commit();
//            mHiddenCameraFragment = null;
//        }

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
//    @Override
//    public void onBackPressed() {
//        Log.d("back901", "BACK PRESS!");
//        if (mHiddenCameraFragment != null) {    //Remove fragment from container if present
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .remove(mHiddenCameraFragment)
//                    .commit();
//            mHiddenCameraFragment = null;
//        }else { //Kill the activity
//            super.onBackPressed();
//        }
//    }





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

//                            SharedPreferences mPrefs = getSharedPreferences(SHARED_PREF, MODE_PRIVATE); //add key
//
//                            if(mPrefs.getBoolean("first_time",true)){
//
//
//
//                            }


                            Toast.makeText(getApplicationContext(), "All permissions are granted by user!", Toast.LENGTH_SHORT).show();

//                            SharedPreferences mPrefs = getSharedPreferences(SHARED_PREF, MODE_PRIVATE); //add key
//                            isFirstTime = mPrefs.getBoolean("first_time", true);
//
//                            if(isFirstTime) {
//
//                                if (mHiddenCameraFragment != null) {    //Remove fragment from container if present
//                                    getSupportFragmentManager()
//                                            .beginTransaction()
//                                            .remove(mHiddenCameraFragment)
//                                            .commit();
//                                    mHiddenCameraFragment = null;
//                                }
//
//
//                                Log.d("901st", "Work Initiated");
//
//                                PeriodicWorkRequest savePhotoRequest =
//                                        new PeriodicWorkRequest.Builder(BackgroundWork.class, 15, TimeUnit.MINUTES)
//                                                .build();
//                                WorkManager.getInstance().enqueue(savePhotoRequest);
//                            }

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
