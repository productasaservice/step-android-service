package com.discover.step.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import android.widget.Toast;

import com.discover.step.R;
import com.discover.step.adapters.BaseSpinnerAdapter;
import com.discover.step.bl.ChallengeManager;
import com.discover.step.model.Challenge;
import com.discover.step.util.LocableScrollView;
import com.discover.step.util.MarkerImageBuilder;
import com.discover.step.util.StepMapView;
import com.discover.step.util.TouchableWrapper;
import com.discover.step.util.WorkaroundMapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geri on 2015.03.02..
 */
public class ChallengeFragment extends Fragment {

    private Spinner mTypeSpinner, mDurationSpinner;
    private EditText mTitleEt, mBetEt, mMessageEt;
    private Button mStartBt;
    private LinearLayout mMapLl;
    private StepMapView mMapView;
    private GoogleMap mMap;
    private Marker mMarker;
    private LocableScrollView scrollView;

    private BaseSpinnerAdapter typeAdapter, durationAdapter;

    private Challenge challenge;

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
        mTitleEt = (EditText) view.findViewById(R.id.challenge_titleEt);
        mBetEt = (EditText) view.findViewById(R.id.challenge_betEt);
        mMessageEt = (EditText) view.findViewById(R.id.challenge_messageEt);
        mStartBt = (Button) view.findViewById(R.id.challenge_startBt);
        mMapLl = (LinearLayout) view.findViewById(R.id.challenge_mapLl);
        mMapView = (StepMapView) view.findViewById(R.id.challenge_mapView);
        scrollView = (LocableScrollView) view.findViewById(R.id.challenge_locableSc);

        typeAdapter = new BaseSpinnerAdapter(mActivity, getType());
        durationAdapter = new BaseSpinnerAdapter(mActivity, getDuration());

        mTypeSpinner.setAdapter(typeAdapter);
        mDurationSpinner.setAdapter(durationAdapter);

        challenge = new Challenge();

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

        mStartBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitleEt.getText().toString();
                String message = mMessageEt.getText().toString();
                String bet = mMessageEt.getText().toString();
                int type = mTypeSpinner.getSelectedItemPosition() + 1;
                String duration_s = mDurationSpinner.getSelectedItem().toString().replace(" min","");
                long duration = System.currentTimeMillis() + (Integer.parseInt(duration_s) * 1000 * 60);

                if (message.isEmpty() || bet.isEmpty() || title.isEmpty()) {
                    Toast.makeText(mActivity,"You have to fill every field.",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (type == 2 && mMarker == null) {
                    Toast.makeText(mActivity,"You have to choose a target place.",Toast.LENGTH_SHORT).show();
                    return;
                }

                challenge.title = title;
                challenge.message = message;
                challenge.bet = bet;
                challenge.type = type;
                challenge.duration = duration;

                if (type == 2) {
                    challenge.lat = mMarker.getPosition().latitude;
                    challenge.lng = mMarker.getPosition().longitude;
                }

                Toast.makeText(mActivity,"You have created a new challenge.",Toast.LENGTH_SHORT).show();
                ChallengeManager.getInstance().createNewChallenge(challenge);
                mActivity.addChallengePoint(challenge);
                mActivity.onBackPressed();
            }
        });

        mMapView.onCreate(savedInstanceState);
        mMapView.setScrollView(scrollView);

        mMap = mMapView.getMap();
        mMap.clear();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                mMarker = mMap.addMarker(new MarkerOptions()
                        .icon(new MarkerImageBuilder(mActivity.getResources()).asPrimary(false).withSize(10).withColor(challenge.color).build())
                        .position(latLng));
            }
        });

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

    /**
     * Update screen data.
     */
    public void updateScreenData() {
        challenge = new Challenge();
        mMessageEt.setText("");
        mBetEt.setText("");
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
