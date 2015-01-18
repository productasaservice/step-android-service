package com.discover.step.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import com.discover.step.R;
import com.discover.step.StepApplication;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * Created by User on 2014.12.23..
 */
public class MarkerImageBuilder {

    private Resources res;
    private boolean isPrimary;

    private int main_color;

    public MarkerImageBuilder asPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
        return this;
    }

    public MarkerImageBuilder withColor(String hex) {
        main_color = Color.parseColor(hex);
        return this;
    }

    public MarkerImageBuilder withColor(int color_res) {
        main_color = res.getColor(color_res);
        return this;
    }

    public MarkerImageBuilder(Resources res) {
        this.res = res;
        isPrimary = false;
        main_color = res.getColor(R.color.main_marker_color);
    }

    private Bitmap buildBitmap() {
        return drawMarker();
    }

    public BitmapDescriptor build() {
        return BitmapDescriptorFactory.fromBitmap(buildBitmap());
    }


    private Bitmap drawMarker() {
        int RING_RADIUS = 7;
        int RING_WIDTH = 1;
        int CORE_RADIUS = 5;

        //Draw a ring it will be the main bitmap to draw into.
        Bitmap background = drawRing(RING_RADIUS, RING_WIDTH);
        //Draw the ring's core circle.
        Bitmap core = drawCircle(CORE_RADIUS);
        //Set bitmap to mutable.
        background = background.copy(background.getConfig(), true);
        //Construct a canvas with the specified bitmap to draw into
        Canvas canvas = new Canvas(background);
        //Create a new paint with default settings.
        Paint paint = new Paint();
        //Put it together.
        canvas.drawBitmap(core, (background.getWidth() - core.getWidth()) / 2, (background.getHeight() - core.getHeight()) / 2, paint);

        return background;
    }


    private Bitmap drawCircle(int radius) {
        int SIZE = (int) DpConverterUtil.convertDpToPixel(100);
        int RADIUS = (int) DpConverterUtil.convertDpToPixel(radius);
        //Check that default size is enough
        SIZE = SIZE < 2 * RADIUS ? 2 * RADIUS : SIZE;
        // Create a mutable bitmap
        Bitmap background = Bitmap.createBitmap(SIZE,SIZE,Bitmap.Config.ARGB_8888);
        background = background.copy(background.getConfig(), true);
        //Construct a canvas with the specified bitmap to draw into
        Canvas canvas = new Canvas(background);
        //Create a new paint with default settings.
        Paint paint = new Paint();
        //smooth out the edges of what is being drawn
        paint.setAntiAlias(true);
        //set color
        paint.setColor(main_color);
        //set style
        paint.setStyle(Paint.Style.FILL);
        //draw circle with radius 30
        canvas.drawCircle(SIZE / 2, SIZE / 2, RADIUS, paint);

        return background;
    }

    private Bitmap drawRing(int radius, int width) {
        int SIZE = (int) DpConverterUtil.convertDpToPixel(width);
        int RADIUS = (int) DpConverterUtil.convertDpToPixel(radius);
        int LINE_WIDTH = (int) DpConverterUtil.convertDpToPixel(width);

        //Check that default size is enough
        SIZE = SIZE < 2 * RADIUS ? 2 * RADIUS + 2 * LINE_WIDTH : SIZE;
        // Create a mutable bitmap
        Bitmap background = Bitmap.createBitmap(SIZE,SIZE,Bitmap.Config.ARGB_8888);
        background = background.copy(background.getConfig(), true);
        //Construct a canvas with the specified bitmap to draw into
        Canvas canvas = new Canvas(background);
        //Create a new paint with default settings.
        Paint paint = new Paint();
        //smooth out the edges of what is being drawn
        paint.setAntiAlias(true);
        //set color
        paint.setColor(isPrimary ? main_color : res.getColor(android.R.color.transparent));
        //set style
        paint.setStyle(Paint.Style.STROKE);
        //set stroke
        paint.setStrokeWidth(LINE_WIDTH);
        //set alpha
        if (isPrimary) {
            paint.setAlpha(200);
        }
        //draw circle with radius 30
        canvas.drawCircle(SIZE / 2, SIZE / 2, RADIUS, paint);

        return background;
    }
}
