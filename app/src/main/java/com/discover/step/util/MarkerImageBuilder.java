package com.discover.step.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
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
    private boolean isAlphaEnabled = false;
    private int size;
    private Bitmap userProfile;


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

    public MarkerImageBuilder withAlphaEnabled(boolean isEnabled) {
        isAlphaEnabled = isEnabled;
        return this;
    }

    public MarkerImageBuilder withSize(int size) {
        this.size = size;
        return this;
    }

    public MarkerImageBuilder withProfileImage(Bitmap profileImage) {
        this.userProfile = profileImage;
        return this;
    }

    public MarkerImageBuilder(Resources res) {
        this.res = res;
        isPrimary = false;
        main_color = res.getColor(R.color.main_marker_color);
        this.size = 5;
    }

    private Bitmap buildBitmap() {
        return drawMarker();
    }

    public BitmapDescriptor build() {
        return BitmapDescriptorFactory.fromBitmap(buildBitmap());
    }

//    private Bitmap drawMarker(int size) {
//        int RING_RADIUS = size + 2;
//        int RING_WIDTH = size - 4;
//        int CORE_RADIUS = size;
//
//        //Draw a ring it will be the main bitmap to draw into.
//        Bitmap background = drawRing(RING_RADIUS, RING_WIDTH);
//        //Draw the ring's core circle.
//        //Bitmap core = drawCircle(CORE_RADIUS);
//        Bitmap core = userProfile == null ? drawCircle(CORE_RADIUS) : userProfile;
//
//
//        //Set bitmap to mutable.
//        background = background.copy(background.getConfig(), true);
//        //Construct a canvas with the specified bitmap to draw into
//        Canvas canvas = new Canvas(background);
//        //Create a new paint with default settings.
//        Paint paint = new Paint();
//
//        canvas.drawBitmap(core, (background.getWidth() - core.getWidth()) / 2, (background.getHeight() - core.getHeight()) / 2, paint);
//
//        //Put it together.
//        Bitmap triangle = drawTriangle(10);
//        Bitmap output = Bitmap.createBitmap(background.getWidth(),background.getHeight() + triangle.getHeight(),Bitmap.Config.ARGB_8888);
//        output = output.copy(output.getConfig(), true);
//        Canvas output_canvas = new Canvas(background);
//        //Create a new paint with default settings.
//        Paint output_paint = new Paint();
//        output_canvas.drawBitmap(background, 0, 0, output_paint);
//        //output_canvas.drawBitmap(triangle, (output.getWidth() - triangle.getWidth()) / 2, background.getHeight(), output_paint);
//        return output;
//    }

    private Bitmap drawMarker(int size) {
        int RING_RADIUS = size + 2;
        int RING_WIDTH = size - 4;
        int CORE_RADIUS = size;
        int offset = (int) DpConverterUtil.convertDpToPixel(1);

        Bitmap triangle = drawTriangle(6);
        //Draw a ring it will be the main bitmap to draw into.
        Bitmap circle = drawCircle(CORE_RADIUS);

        //Set bitmap to mutable.
        Bitmap background = Bitmap.createBitmap(circle.getWidth(),circle.getHeight() + triangle.getHeight(),Bitmap.Config.ARGB_8888);
        background = background.copy(background.getConfig(), true);
        //Construct a canvas with the specified bitmap to draw into
        Canvas canvas = new Canvas(background);
        //Create a new paint with default settings.
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);

        canvas.drawBitmap(triangle, (background.getWidth() - triangle.getWidth()) / 2, circle.getHeight() - offset, paint);
        canvas.drawBitmap(circle, (background.getWidth() - circle.getWidth()) / 2, 0, paint);

        return background;
    }

    private Bitmap drawMarker() {

        return drawMarker(size);
    }


    private Bitmap drawCircle(int radius) {
        int SIZE = (int) DpConverterUtil.convertDpToPixel(100);
        int RADIUS = (int) DpConverterUtil.convertDpToPixel(radius);
        //Check that default size is enough
        //SIZE = SIZE < 2 * RADIUS ? 2 * RADIUS : SIZE;
        SIZE = 2 * RADIUS;
        // Create a mutable bitmap
        Bitmap background = Bitmap.createBitmap(SIZE,SIZE,Bitmap.Config.ARGB_8888);
        background = background.copy(background.getConfig(), true);
        //Draw the ring's core circle.
        Bitmap pic = userProfile == null ? null : userProfile;
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
        //set alpha
        if (isAlphaEnabled) {
            paint.setAlpha(180);
        }
        //draw circle with radius 30
        canvas.drawCircle(RADIUS, RADIUS, RADIUS, paint);
        if (pic != null)
            canvas.drawBitmap(pic, (background.getWidth() - pic.getWidth()) / 2, (background.getHeight() - pic.getHeight()) / 2, paint);

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

    private Bitmap drawTriangle(int size) {
        int SIZE = (int) DpConverterUtil.convertDpToPixel(size);
        int HALF = SIZE / 2;
        int HEIGHT = (int) Math.sqrt(Math.pow(SIZE,2) - Math.pow(HALF,2));

        // Create a mutable bitmap
        Bitmap background = Bitmap.createBitmap(SIZE,HEIGHT,Bitmap.Config.ARGB_8888);
        background = background.copy(background.getConfig(), true);
        //Construct a canvas with the specified bitmap to draw into
        Canvas canvas = new Canvas(background);
        //Create a new paint with default settings.
        Paint paint = new Paint();
        //smooth out the edges of what is being drawn
        paint.setAntiAlias(true);
        //set color
        paint.setColor(main_color);
        paint.setStrokeWidth(4);
        //set style
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        Point a = new Point(0, 0);
        Point b = new Point(SIZE, 0);
        Point c = new Point(HALF, HEIGHT);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(a.x,a.y);
        path.lineTo(b.x,b.y);
        path.lineTo(c.x,c.y);
        path.lineTo(a.x,a.y);

        path.close();

        canvas.drawPath(path, paint);

        return background;
    }
}
