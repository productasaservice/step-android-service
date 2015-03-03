package com.discover.step.util;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by Geri on 2015.03.03..
 */
public class TouchableWrapper extends FrameLayout {

    private OnTouchListener mListener;

    public TouchableWrapper(Context context) {
        super(context);
    }

    public void setListener(OnTouchListener listener) {
        mListener = listener;
    }

    public interface OnTouchListener {
        public abstract void onTouch();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mListener.onTouch();
                break;
            case MotionEvent.ACTION_UP:
                mListener.onTouch();
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}
