/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.domain.covidandro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture.Metadata;

import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceLandmark;
import com.google.mlkit.vision.face.FaceLandmark.LandmarkType;
import java.text.SimpleDateFormat;
import java.io.File;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import static android.content.Context.MODE_PRIVATE;

/**
 * Graphic instance for rendering face position, contour, and landmarks within the associated
 * graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 4.0f;
    private static final float ID_TEXT_SIZE = 30.0f;
    private static final float ID_Y_OFFSET = 40.0f;
    private static final float ID_X_OFFSET = -40.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;
    private static final int NUM_COLORS = 10;
    private static final int[][] COLORS = new int[][]{
            // {Text color, background color}
            {Color.BLACK, Color.YELLOW},
            {Color.WHITE, Color.MAGENTA},
            {Color.BLACK, Color.LTGRAY},
            {Color.WHITE, Color.RED},
            {Color.WHITE, Color.BLUE},
            {Color.WHITE, Color.DKGRAY},
            {Color.BLACK, Color.CYAN},
            {Color.BLACK, Color.YELLOW},
            {Color.WHITE, Color.BLACK},
            {Color.BLACK, Color.GREEN}
    };

    private final Paint facePositionPaint;
    private final Paint[] idPaints;
    private final Paint[] boxPaints;
    private final Paint[] labelPaints;
    //private final Paint guideTxt;
    private final Paint ovalPaint;
    private final Paint ovalClear;
    private static final float OVAL_STROKE_WIDTH = 4f;
    Handler handler;

    private volatile Face face;
    private ImageCapture imageCapture;
    private ExecutorService executorService;
    private Context context;
    boolean shouldAnimate = true;
    public static final String SHARED_PREF = "com.domain.covidandro";
    boolean isPicTaken;
    SharedPreferences mPrefs;
    final Intent intent;
    boolean isDetected = false;
    double usrLat;
    double usrLong;
    FusedLocationProviderClient mFusedLocationClient;
    FaceGraphic(GraphicOverlay overlay, Face face, ImageCapture captureUseCase, ExecutorService executorService, Context context) {
        super(overlay);
        handler = new Handler();
        this.face = face;
        this.context = context;

        //usrLat = 0.0;
        //usrLong = 0.0;
        mPrefs = context.getSharedPreferences(SHARED_PREF, MODE_PRIVATE); //add key
        mPrefs.edit().remove("usr_lat").apply();
        mPrefs.edit().remove("usr_long").apply();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        getLastLocation();
        final int selectedColor = Color.RED;
        this.imageCapture = captureUseCase;
        this.executorService = executorService;
        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);
        intent = new Intent(context, RightProfileActivity.class);

        isPicTaken = mPrefs.getBoolean("pic_taken", false);

        ovalPaint = new Paint();
        ovalPaint.setColor(Color.BLUE);
        ovalPaint.setStyle(Paint.Style.STROKE);
        ovalPaint.setStrokeWidth(OVAL_STROKE_WIDTH);

        ovalClear = new Paint();
        ovalClear.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        ovalClear.setStyle(Paint.Style.FILL);
        ovalClear.setColor(Color.WHITE);

//        guideTxt = new Paint();
//        guideTxt.setStyle(Paint.Style.STROKE);
//        guideTxt.setColor(Color.WHITE);

        int numColors = COLORS.length;
        idPaints = new Paint[numColors];
        boxPaints = new Paint[numColors];
        labelPaints = new Paint[numColors];
        for (int i = 0; i < numColors; i++) {
            idPaints[i] = new Paint();
            idPaints[i].setColor(COLORS[i][0] /* text color */);
            idPaints[i].setTextSize(ID_TEXT_SIZE);

            boxPaints[i] = new Paint();
            boxPaints[i].setColor(COLORS[i][1] /* background color */);
            boxPaints[i].setStyle(Paint.Style.STROKE);
            boxPaints[i].setStrokeWidth(BOX_STROKE_WIDTH);
            boxPaints[i].setPathEffect(new DashPathEffect(new float[] {10,20}, 0)); //Dotted Rectangle.
            //fgPaintSel.setARGB(255, 0, 0,0);
            //fgPaintSel.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));

            labelPaints[i] = new Paint();
            labelPaints[i].setColor(COLORS[i][1]  /* background color */);
            labelPaints[i].setStyle(Paint.Style.FILL);
        }
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */

    private boolean isLocationEnabled(){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }


//    @SuppressLint("MissingPermission")
//    private void currentLoc() {
//        LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100,0,mLocationListener);
//    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){



        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
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
            Toast.makeText(getApplicationContext(), "Lat: "+usrLat+", Long: "+usrLong, Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            prefsEditor.putBoolean("loc_changed",true).apply();
            prefsEditor.putFloat("usr_lat",(float) usrLat).apply();
            prefsEditor.putFloat("usr_long",(float) usrLong).apply();

//            SharedPreferences.Editor prefsEditor = mPrefs.edit();
//            prefsEditor.putFloat("usr_lat",(float) usrLat).apply();
//            prefsEditor.putFloat("usr_long",(float) usrLong).apply();



        }
    };

//    private final LocationListener mLocationListener = new LocationListener() {
//        @Override
//        public void onLocationChanged(final Location location) {
//            //your code here
//
//            usrLat = location.getLatitude();
//            usrLong = location.getLongitude();
//            Toast.makeText(getApplicationContext(), "Lat: "+usrLat+", Long: "+usrLong, Toast.LENGTH_SHORT).show();
//            SharedPreferences.Editor prefsEditor = mPrefs.edit();
//            prefsEditor.putBoolean("loc_changed",true).apply();
//            prefsEditor.putFloat("usr_lat",(float) usrLat).apply();
//            prefsEditor.putFloat("usr_long",(float) usrLong).apply();
//
//
//        }
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//
//        }
//
//        @Override
//        public void onProviderEnabled(String provider) {
//
//        }
//
//        @Override
//        public void onProviderDisabled(String provider) {
//
//        }
//    };





    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (isLocationEnabled()) {
            //currentLoc();
            //requestNewLocationData();
//            requestNewLocationData();
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

                                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                                //prefsEditor.putBoolean("loc_changed",true).apply();
                                prefsEditor.putFloat("usr_lat",(float) usrLat).apply();
                                prefsEditor.putFloat("usr_long",(float) usrLong).apply();



                            }
                        }
                    }
            );
        } else {
            Toast.makeText(context, "Turn on location", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

    }


    @Override
    public void draw(Canvas canvas) {
        Face face = this.face;
        if (face == null) {
            return;
        }
        isPicTaken = mPrefs.getBoolean("pic_taken", false);

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getBoundingBox().centerX());
        float y = translateY(face.getBoundingBox().centerY());
        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, facePositionPaint); //this is small dot in GUI

        // Calculate positions.
        float left = x - scale(face.getBoundingBox().width() / 2.0f);
        float top = y - scale(face.getBoundingBox().height() / 2.0f);
        float right = x + scale(face.getBoundingBox().width() / 2.0f);
        float bottom = y + scale(face.getBoundingBox().height() / 2.0f);
        float lineHeight = ID_TEXT_SIZE + BOX_STROKE_WIDTH;
        float yLabelOffset = -lineHeight;

        // Decide color based on face ID
        int colorID = (face.getTrackingId() == null)
                ? 0 : Math.abs(face.getTrackingId() % NUM_COLORS);

        // Calculate width and height of label box
        float textWidth = idPaints[colorID].measureText("ID: " + face.getTrackingId());
        if (face.getSmilingProbability() != null) {
            yLabelOffset -= lineHeight;
            textWidth = Math.max(textWidth, idPaints[colorID].measureText(
                    String.format(Locale.US, "Happiness: %.2f", face.getSmilingProbability())));
        }
        if (face.getLeftEyeOpenProbability() != null) {
            yLabelOffset -= lineHeight;
            textWidth = Math.max(textWidth, idPaints[colorID].measureText(
                    String.format(Locale.US, "Left eye: %.2f", face.getLeftEyeOpenProbability())));
        }
        if (face.getRightEyeOpenProbability() != null) {
            yLabelOffset -= lineHeight;
            textWidth = Math.max(textWidth, idPaints[colorID].measureText(
                    String.format(Locale.US, "Right eye: %.2f", face.getLeftEyeOpenProbability())));
        }

        // Draw labels


//        canvas.drawRect(left - BOX_STROKE_WIDTH,
//                top + yLabelOffset,
//                left + textWidth + (2 * BOX_STROKE_WIDTH),
//                top,
//                labelPaints[colorID]);
        yLabelOffset += ID_TEXT_SIZE;

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        float lft = 0.125f;
        RectF oval = new RectF(lft*width,0.0625f*height, (1-lft)*width,0.5625f*height);
        float ovBot = 0.5625f*height;
        float ovTop = 0.0625f*height;
        float angY = face.getHeadEulerAngleY();
        float angX = face.getHeadEulerAngleX();
        float angZ = face.getHeadEulerAngleZ();
//        canvas.drawText("Place your Face inside the Oval", width*0.5f, height*0.5f,
//                idPaints[colorID]);
        if( (bottom < ovBot && top > ovTop) && !isDetected){
            isDetected = true;
            if( (angY > -5 && angY < 5) && (angX > -12 && angX < 12) && (angZ > -5 && angZ < 5) ) {
                canvas.drawOval(oval, ovalClear);
                canvas.drawOval(oval, ovalPaint);
                postInvalidate();
                Log.d("901rdetect901", "Detected");
                int widthSpec = View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.UNSPECIFIED);
                int heightSpec = View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.UNSPECIFIED);
                int checkLft = (int) (lft * width + (1 - lft) * width) / 2;
                int checkTop = (int) (0.0625f * height + 0.5625f * height) / 2;
//                check.layout(checkLft, checkTop, checkLft + widthSpec, checkTop+heightSpec );
//                canvas.save();
//                canvas.translate(checkLft, checkTop);
//                check.draw(canvas);
//                canvas.restore();
                // check.setAnimation("check_mark.json");

                //check.setWillNotDraw(true);

//                if(shouldAnimate) {
//                    new CountDownTimer(2000, 1000) {
//
//                        @Override
//                        public void onTick(long miliseconds) {
//                        }
//
//                        @Override
//                        public void onFinish() {
//                            //after 5 seconds draw the second line
//
//                            shouldAnimate = false;
//                        }
//                    }.start();
//                }




                File storageDir =
                        getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                File photoFile = new File(storageDir, "straight_"+new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
                        .format(System.currentTimeMillis()) + ".jpg");

                Metadata metadata = new Metadata();
                metadata.setReversedHorizontal(true);

                ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(
                        photoFile
                ).setMetadata(metadata).build();

                synchronized (imageCapture) {
                    if (!isPicTaken) {
                        Log.d("901rpic901", "picDone");
                        imageCapture.takePicture(outputFileOptions, executorService, new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                                //Toast.makeText(getApplicationContext(), "Saved Image: "+outputFileResults.getSavedUri(), Toast.LENGTH_SHORT).show();
                                Log.e("save901", "Saved Image: " + outputFileResults.getSavedUri());
                                //check.playAnimation();
//                        new CountDownTimer(2000, 1000) {
//
//                        @Override
//                        public void onTick(long miliseconds) {
//                        }
//
//                        @Override
//                        public void onFinish() {
//                            //after 5 seconds draw the second line
//                            Intent intent = new Intent(context, AfterCaptureActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            context.startActivity(intent);
//
//                        }
//                    }.start();
//                                canvas.notifyAll();
//                                synchronized (intent) {
//                                    if(!isPicTaken) {
//                                        SharedPreferences.Editor prefsEditor = mPrefs.edit();
//                                        prefsEditor.putBoolean("pic_taken", true);
//                                        isPicTaken = true;
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        intent.notifyAll();
//                                        context.startActivity(intent);
//
//
//                                    }
//                                }


                            }

                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {

                                Log.e("err901", exception.getMessage());

                            }
                        });



                    }

                    //getLastLocation();
                    //Log.d("loc901F", "Lat: "+usrLat+", Long: "+usrLong);
                    new CountDownTimer(800, 200) {

                        @Override
                        public void onTick(long miliseconds) {
                        }

                        @Override
                        public void onFinish() {



                            synchronized (intent) {
                                if(!isPicTaken) {
                                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                                    float ulat = mPrefs.getFloat("usr_lat",0.0f);
                                    float ulong = mPrefs.getFloat("usr_long", 0.0f);
                                    Log.d("loc901", "Lat: "+ulat+", Long: "+ulong);
                                    //Toast.makeText(context, "Lat: "+ulat+", Long: "+ulong, Toast.LENGTH_LONG).show();
                                    prefsEditor.putBoolean("pic_taken", true);
                                    isPicTaken = true;
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.notifyAll();
                                    context.startActivity(intent);


                                }
                            }

                        }
                    }.start();

                    try {
                        imageCapture.wait(800);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }


                    imageCapture.notifyAll();
                    //canvas.notifyAll();
                }




            }
            Log.d("angle901","EulerX: "+face.getHeadEulerAngleX()+", EulerY: "+face.getHeadEulerAngleY()+", EulerZ: "+face.getHeadEulerAngleZ());

        }

       // canvas.drawRect(left, top, right, bottom, boxPaints[colorID]);       //Face Rectangle Box.
//        canvas.drawText("ID: " + face.getTrackingId(), left, top + yLabelOffset,
//                idPaints[colorID]);
        yLabelOffset += lineHeight;

        // Draws all face contours.
        for (FaceContour contour : face.getAllContours()) {
            for (PointF point : contour.getPoints()) {
                canvas.drawCircle(
                        translateX(point.x), translateY(point.y), FACE_POSITION_RADIUS, facePositionPaint);
            }
        }

        // Draws smiling and left/right eye open probabilities.
        if (face.getSmilingProbability() != null) {
            canvas.drawText(
                    "Smiling: " + String.format(Locale.US, "%.2f", face.getSmilingProbability()),
                    left,
                    top + yLabelOffset,
                    idPaints[colorID]);
            yLabelOffset += lineHeight;
        }

        FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
        if (leftEye != null && face.getLeftEyeOpenProbability() != null) {
            canvas.drawText(
                    "Left eye open: " + String.format(Locale.US, "%.2f", face.getLeftEyeOpenProbability()),
                    translateX(leftEye.getPosition().x) + ID_X_OFFSET,
                    translateY(leftEye.getPosition().y) + ID_Y_OFFSET,
                    idPaints[colorID]);
        } else if (leftEye != null && face.getLeftEyeOpenProbability() == null) {
            canvas.drawText(
                    "Left eye",
                    left,
                    top + yLabelOffset,
                    idPaints[colorID]);
            yLabelOffset += lineHeight;
        } else if (leftEye == null && face.getLeftEyeOpenProbability() != null) {
            canvas.drawText(
                    "Left eye open: " + String.format(Locale.US, "%.2f", face.getLeftEyeOpenProbability()),
                    left,
                    top + yLabelOffset,
                    idPaints[colorID]);
            yLabelOffset += lineHeight;
        }

        FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
        if (rightEye != null && face.getRightEyeOpenProbability() != null) {
            canvas.drawText(
                    "Right eye open: " + String.format(Locale.US, "%.2f", face.getRightEyeOpenProbability()),
                    translateX(rightEye.getPosition().x) + ID_X_OFFSET,
                    translateY(rightEye.getPosition().y) + ID_Y_OFFSET,
                    idPaints[colorID]);
        } else if (rightEye != null && face.getRightEyeOpenProbability() == null) {
            canvas.drawText(
                    "Right eye",
                    left,
                    top + yLabelOffset,
                    idPaints[colorID]);
            yLabelOffset += lineHeight;
        } else if (rightEye == null && face.getRightEyeOpenProbability() != null) {
            canvas.drawText(
                    "Right eye open: " + String.format(Locale.US, "%.2f", face.getRightEyeOpenProbability()),
                    left,
                    top + yLabelOffset,
                    idPaints[colorID]);
        }

        // Draw facial landmarks
        drawFaceLandmark(canvas, FaceLandmark.LEFT_EYE);
        drawFaceLandmark(canvas, FaceLandmark.RIGHT_EYE);
        drawFaceLandmark(canvas, FaceLandmark.LEFT_CHEEK);
        drawFaceLandmark(canvas, FaceLandmark.RIGHT_CHEEK);
    }

    private void drawFaceLandmark(Canvas canvas, @LandmarkType int landmarkType) {
        FaceLandmark faceLandmark = face.getLandmark(landmarkType);
        if (faceLandmark != null) {
            canvas.drawCircle(
                    translateX(faceLandmark.getPosition().x),
                    translateY(faceLandmark.getPosition().y),
                    FACE_POSITION_RADIUS,
                    facePositionPaint);
        }
    }
}
