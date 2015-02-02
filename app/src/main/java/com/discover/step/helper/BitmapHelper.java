package com.discover.step.helper;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.discover.step.util.DpConverterUtil;

public class BitmapHelper {

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = bitmap.getWidth();

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		//paint.setMaskFilter(new BlurMaskFilter(15, Blur.INNER));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

    public static Bitmap getCenterChorpedRoundedBitmap(Bitmap bitmap, int size) {
        int sourceWidth = bitmap.getWidth();
        int sourceHeight = bitmap.getHeight();

        int newWidth = (int) DpConverterUtil.convertDpToPixel(size);
        int newHeight = (int) DpConverterUtil.convertDpToPixel(size);

        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        Bitmap output = Bitmap.createBitmap(newWidth, newHeight, Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();

        final Rect rect = new Rect(0, 0, newWidth, newHeight);
        final RectF rectBaseF = new RectF(rect);

        final RectF rectModifiedBmpF = new RectF(left, top,left + scaledWidth, top + scaledHeight);
        final float roundPx = newWidth;
        paint.setAntiAlias(true);
        canvas.drawRoundRect(rectBaseF,roundPx,roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

        canvas.drawBitmap(bitmap, null, rectModifiedBmpF, paint);

        return output;
    }
}
