package com.domain.covidandro;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;


public class SmartAcquisGraphic extends GraphicOverlay.Graphic {
    private static final int TEXT_COLOR = Color.RED;
    private static final float TEXT_SIZE = 60.0f;

    private final Paint textPaint;
    private final Paint ovalPaint;
    private final Paint bgPaint;
    private final Paint ovalSpotPaint;
    private final GraphicOverlay overlay;

    private static final float OVAL_STROKE_WIDTH = 4f;


    public SmartAcquisGraphic(
            GraphicOverlay overlay) {
        super(overlay);
        this.overlay = overlay;
        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);

        ovalPaint = new Paint();
        ovalPaint.setColor(Color.BLUE);
        ovalPaint.setStyle(Paint.Style.STROKE);
        ovalPaint.setStrokeWidth(OVAL_STROKE_WIDTH);
        //ovalPaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));

        ovalSpotPaint = new Paint();
        ovalSpotPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        ovalSpotPaint.setStyle(Paint.Style.FILL);
        ovalSpotPaint.setColor(Color.WHITE);

        bgPaint = new Paint();
        bgPaint.setAlpha(140);

        postInvalidate();
    }

    @Override
    public synchronized void draw(Canvas canvas) {
        float x = TEXT_SIZE * 0.8f;
        float y = TEXT_SIZE * 5.8f;
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        float left = 0.125f;
        RectF oval = new RectF(left*width,0.0625f*height, (1-left)*width,0.5625f*height);

        canvas.drawPaint(bgPaint);
        canvas.drawOval(oval, ovalSpotPaint);
        canvas.drawOval(oval, ovalPaint);


    }
}
