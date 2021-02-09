package com.domain.covidandro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.domain.covidandro.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Camera2Service extends Service {
    private static final String TAG ="Camera2Service";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private String upload_URL = "http://piyush16.pythonanywhere.com/";
    private RequestQueue rQueue;
    FusedLocationProviderClient mFusedLocationClient;
    double usrLat = 0.0;
    double usrLong = 0.0;
    String usr_id="";
    SharedPreferences mPrefs;
    public static final String SHARED_PREF = "com.domain.covidandro";

    public static final String
            ACTION_RESULT_BROADCAST = DemoCamService.class.getName() + "ResultBroadcast",
            EXTRA_RESULT = "extra_result";

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    WindowManager windowManager;

    private static final int CAMERA = CameraCharacteristics.LENS_FACING_FRONT;
    private CameraDevice camera;
    private CameraCaptureSession session;
    private ImageReader imageReader;

    private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Camera2Service.this.camera = camera;
            actOnReadyCameraDevice();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
        }
    };

    private CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            Camera2Service.this.session = session;
            try {
                //session.setRepeatingRequest(createCaptureRequest(), null, null);
                session.capture(createCaptureRequest(), null, null);
            } catch (CameraAccessException e){
                Log.e(TAG, e.getMessage());
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {}
    };

    private ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader){
            Image img = reader.acquireLatestImage();
            if (img != null) {
                processImage(img);
                img.close();
            }
        }
    };




    @SuppressLint("MissingPermission")
    public void readyCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        try {
            String pickedCamera = getCamera(manager);
            manager.openCamera(pickedCamera, cameraStateCallback, null);
            imageReader = ImageReader.newInstance(320, 240, ImageFormat.JPEG, 2);
            imageReader.setOnImageAvailableListener(onImageAvailableListener, null);

        } catch (CameraAccessException e){
            e.printStackTrace();
        }
    }


    /**
     *  Return the Camera Id which matches the field CAMERA.
     */
    @RequiresPermission(allOf = {Manifest.permission.CAMERA, Manifest.permission.SYSTEM_ALERT_WINDOW})
    public String getCamera(CameraManager manager){
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CAMERA) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            if( Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this) ) {

                mPrefs = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
                if ((mPrefs.getString("usr_id", "")).equals("")) {

                } else {
                    usr_id = mPrefs.getString("usr_id", "");
                }

                getLastLocation();


                readyCamera();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    Intent intent2 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    this.startActivity(intent2);

                }
            }
        } else {
            Toast.makeText(this, "Please Provide Camera Permission.", Toast.LENGTH_SHORT).show();
        }
        //return super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;

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
    private boolean isLocationEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }




    public void actOnReadyCameraDevice() {
        try {
            camera.createCaptureSession(Arrays.asList(imageReader.getSurface()), sessionStateCallback, null);

        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
    }


    @Override
    public void onDestroy() {

        try{
            session.abortCaptures();
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
        }
        camera.close();
        session.close();
        stopForeground(true);
        stopSelf();

    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.domain.covidandro";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_corona)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }



    private void processImage(Image image){
        //process image data

        final ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        final byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);

        Toast.makeText(this, "Processing Image.", Toast.LENGTH_SHORT).show();

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap rotatedBitmap;
        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);


        File cacheDir = this.getExternalCacheDir() == null ? this.getCacheDir() : this.getExternalCacheDir();
        File path = new File(cacheDir.getAbsolutePath() + File.separator + "IMG_"+ System.currentTimeMillis() + ".jpeg");
//        if(!path.exists()) {
//            path.mkdirs();
//        }
        FileOutputStream outputStream = null;


        try {
            //File outFile = new File(path, "IMG_" + System.currentTimeMillis() + ".jpeg");

            if(!path.exists())
                path.createNewFile();

            outputStream = new FileOutputStream(path);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try{
                if(outputStream != null){
                    if(!usr_id.equals("")) {
                        uploadImage(BitmapFactory.decodeFile(path.getPath()));
                    }
                    outputStream.close();
                    Toast.makeText(this, "File Written.", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

    private void createNotif(String msg){
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;

        String NOTIFICATION_CHANNEL_ID = "";

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NOTIFICATION_CHANNEL_ID = "alert_channel";
            String channelName = "Alert Channel";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
            chan.setLightColor(Color.RED);
            chan.setVibrationPattern(new long[] { 1000, 1000, 1000, 1000, 1000 });
            chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);


            manager.createNotificationChannel(chan);
        }


        Notification notif = new Notification.Builder(this)
                .setContentTitle("Alert!!")
                .setContentText(msg)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setLights(Color.RED,3000,3000)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_corona)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .build();

        manager.notify(NotificationID.getID(),notif);


//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
//        Notification notification = notificationBuilder.setOngoing(true)
//                .setSmallIcon(R.drawable.ic_corona)
//                .setContentTitle("App is running in background")
//                .setPriority(NotificationManager.IMPORTANCE_MIN)
//                .setCategory(Notification.CATEGORY_SERVICE)
//                .build();





    }

    private void uploadImage(final Bitmap bitmap){

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, upload_URL,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Log.d("ressssssoo",new String(response.data));
                        rQueue.getCache().clear();


                        createNotif(new String(response.data));



                        Intent intent = new Intent(ACTION_RESULT_BROADCAST);
                        intent.putExtra(EXTRA_RESULT, new String(response.data));
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                        stopForeground(true);
                        stopSelf();






//                        try {
//                            JSONObject jsonObject = new JSONObject(new String(response.data));
//                            Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
//
//                            jsonObject.toString().replace("\\\\","");
//
//                            if (jsonObject.getString("status").equals("true")) {
//
//                                arraylist = new ArrayList<HashMap<String, String>>();
//                                JSONArray dataArray = jsonObject.getJSONArray("data");
//
//                                String url = "";
//                                for (int i = 0; i < dataArray.length(); i++) {
//                                    JSONObject dataobj = dataArray.getJSONObject(i);
//                                    url = dataobj.optString("pathToFile");
//                                }
//                                Picasso.get().load(url).into(imageView);
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        stopForeground(true);
                        stopSelf();
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
                params.put("id",usr_id); //user ID
                params.put("loc",usrLat+" "+usrLong);
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








    private CaptureRequest createCaptureRequest() {
        try {
            CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.addTarget(imageReader.getSurface());

            // Focus
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            // Flash
            //builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            //Orientation
            int rotation = windowManager.getDefaultDisplay().getRotation();
            builder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));


            return builder.build();
        } catch (CameraAccessException e){
            Log.e(TAG, e.getMessage());
            return null;
        }
    }





    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
