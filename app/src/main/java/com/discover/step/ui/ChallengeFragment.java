package com.discover.step.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.discover.step.R;
import com.discover.step.adapters.BaseSpinnerAdapter;
import com.discover.step.util.LocableScrollView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geri on 2015.03.02..
 */
public class ChallengeFragment extends Fragment {

    private Spinner mTypeSpinner, mDurationSpinner;
    private EditText mBetEt, mMessageEt;
    private MapView mMapView;
    private Button mStartBt;
    private LinearLayout mMapLl;
    private GoogleMap mMap;
    private LocableScrollView scrollView;

    private BaseSpinnerAdapter typeAdapter, durationAdapter;

    private MainActivity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.challenge_fragment, container, false);
        mTypeSpinner = (Spinner) view.findViewById(R.id.challenge_typeSp);
        mDurationSpinner = (Spinner) view.findViewById(R.id.challenge_durationSp);
        mBetEt = (EditText) view.findViewById(R.id.challenge_betEt);
        mMessageEt = (EditText) view.findViewById(R.id.challenge_messageEt);
        mStartBt = (Button) view.findViewById(R.id.challenge_startBt);
        mMapLl = (LinearLayout) view.findViewById(R.id.challenge_mapLl);
        mMapView = (MapView) view.findViewById(R.id.challenge_mapView);
        scrollView = (LocableScrollView) view.findViewById(R.id.challenge_locableSc);

        typeAdapter = new BaseSpinnerAdapter(mActivity, getType());
        durationAdapter = new BaseSpinnerAdapter(mActivity, getDuration());

        mTypeSpinner.setAdapter(typeAdapter);
        mDurationSpinner.setAdapter(durationAdapter);

        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    mMapLl.setVisibility(View.VISIBLE);
                } else {
                    mMapLl.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mMapView.onCreate(savedInstanceState);

        mMap = mMapView.getMap();
        mMap.clear();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);


        return view;
    }

    @Override
    public void onResume() {
        if (mMapView != null) {
            mMapView.onResume();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mMapView != null) {
            mMapView.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
        super.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    private List<String> getType() {
        List<String> typeList = new ArrayList<>();

        typeList.add("Catch me");
        typeList.add("Catch place");

        return typeList;
    }

    private List<String> getDuration() {
        List<String> durationList = new ArrayList<>();

        durationList.add("10 min");
        durationList.add("30 min");
        durationList.add("60 min");
        durationList.add("90 min");
        durationList.add("120 min");

        return durationList;
    }
}
