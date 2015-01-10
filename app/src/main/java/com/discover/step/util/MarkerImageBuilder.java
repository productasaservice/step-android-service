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

    private static final int DEF_CIRCLE_IN_DP = 10;
    private static final int DEF_RING_IN_DP = 150;

    private Resources res;
    private boolean isPrimary;
    private int main_res;
    private int secondary_res;

    private int inner_circle_size;
    private int external_circle_size;
    private int bg_size;
    private int main_color;

    public MarkerImageBuilder asPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
        return this;
    }

    public MarkerImageBuilder withColor(String hex) {
        main_color = Color.parseColor(hex);
        return this;
    }

    public MarkerImageBuilder withColor(int color) {
        main_color = color;
        return this;
    }

    public MarkerImageBuilder withMainRes(int res) {
        main_res = res;
        return this;
    }

    public MarkerImageBuilder withSecondaryRes(int res) {
        secondary_res = res;
        return this;
    }

    public MarkerImageBuilder(Resources res) {
        this.res = res;
        isPrimary = false;
        main_res = R.drawable.main_marker;
        secondary_res = R.drawable.secondary_marker;
        inner_circle_size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,DEF_CIRCLE_IN_DP, StepApplication.getContext().getResources().getDisplayMetrics());
        external_circle_size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,DEF_RING_IN_DP, StepApplication.getContext().getResources().getDisplayMetrics());
        bg_size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,250, StepApplication.getContext().getResources().getDisplayMetrics());
    }

    private Bitmap buildBitmap() {

        //Transparent layer. we wanna draw on it.
        Bitmap _pin = BitmapFactory.decodeResource(res, isPrimary ? main_res : secondary_res);

        return _pin;
    }

    public BitmapDescriptor build() {
        return BitmapDescriptorFactory.fromBitmap(buildBitmap());
    }

//    public Bitmap buildBitmap() {
//        //Transparent layer. we wanna draw on it.
//        //Bitmap _transparent_bg = BitmapFactory.decodeResource(res, R.drawable.marker_transparent_bg_c);
////        Bitmap _circle = Bitmap.createBitmap(inner_circle_size,inner_circle_size, Bitmap.Config.ARGB_8888);
////        Bitmap _output = Bitmap.createBitmap(bg_size,bg_size, Bitmap.Config.ARGB_8888);
////
////        Canvas external_circle = new Canvas(_circle);
////        Paint circle_paint = new Paint();
////        circle_paint.setColor(main_color);
////        circle_paint.setStyle(Paint.Style.STROKE);
////        circle_paint.setStrokeWidth(4f);
////
////        Log.d("test--","trans: " + bg_size + " _circle: " + inner_circle_size);
////
////        Canvas canvas = new Canvas(_output);
////        final Paint paint = new Paint();
////        final Rect rect = new Rect(0, 0,bg_size,bg_size);
////        canvas.drawBitmap(_transparent_bg, rect, rect, paint);
////
////        external_circle.drawCircle(external_circle_size / 2, external_circle_size / 2, external_circle_size / 2, circle_paint);
////
////        external_circle.setBitmap(_circle);
////
////        int left = (bg_size - external_circle_size) / 2;
////        int top = (bg_size - external_circle_size) / 2;
////        canvas.drawBitmap(_circle, left, top, null);
////        return _output;
//        return drawableToBitmap(res.getDrawable(R.drawable.marker_test));
//    }

    private Bitmap drawInnerCircle() {
        Bitmap _circle = Bitmap.createBitmap(inner_circle_size,inner_circle_size, Bitmap.Config.ARGB_8888);
        Canvas inner_circle = new Canvas(_circle);
        Paint circle_paint = new Paint();
        circle_paint.setColor(main_color);
        inner_circle.drawCircle(inner_circle_size / 2, inner_circle_size / 2, inner_circle_size / 2, circle_paint);

        inner_circle.setBitmap(_circle);

        return _circle;
    }


    private Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    //private Bitmap draw
}
