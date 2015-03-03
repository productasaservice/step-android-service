package com.discover.step.util;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.google.android.gms.maps.MapView;

/**
 * Created by Geri on 2015.03.03..
 */
public class StepMapView extends MapView {

    ScrollView scrollView;

    public StepMapView(Context context) {
        super(context);
        init(context);
    }

    public StepMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StepMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private OnTouchListener mListener;

    private void init(Context context) {
//        TouchableWrapper frameLayout = new TouchableWrapper(context);
//        frameLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
//        addView(frameLayout,
//                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d("test--","lenyomtad..");
                // Disallow ScrollView to intercept touch events.
                if (scrollView != null)
                this.getParent().requestDisallowInterceptTouchEvent(true);

                break;

            case MotionEvent.ACTION_UP:
                Log.d("test--","elenedted..");
                // Allow ScrollView to intercept touch events.
                if (scrollView != null)
                this.getParent().requestDisallowInterceptTouchEvent(false);

                break;
        }

        // Handle MapView's touch events.
        super.onTouchEvent(ev);
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Disallow ScrollView to intercept touch events.
                if (scrollView != null)
                   scrollView.requestDisallowInterceptTouchEvent(true);

                break;

            case MotionEvent.ACTION_UP:
                // Allow ScrollView to intercept touch events.
                if (scrollView != null)
                    scrollView.requestDisallowInterceptTouchEvent(false);

                break;
        }
        super.dispatchTouchEvent(ev);
        return true;
    }

    public void setScrollView(ScrollView scrollView) {
        this.scrollView = scrollView;
    }

//    public void setListener(OnTouchListener listener) {
//        mListener = listener;
//    }
//
//    public interface OnTouchListener {
//        public abstract void onTouch();
//    }
//
//    public class TouchableWrapper extends FrameLayout {
//
//        public TouchableWrapper(Context context) {
//            super(context);
//        }
//
//        @Override
//        public boolean dispatchTouchEvent(MotionEvent event) {
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    mListener.onTouch();
//                    break;
//                case MotionEvent.ACTION_UP:
//                    mListener.onTouch();
//                    break;
//            }
//            return super.dispatchTouchEvent(event);
//        }
//    }
}
