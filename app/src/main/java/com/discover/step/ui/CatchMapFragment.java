package com.discover.step.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.discover.step.Config;
import com.discover.step.R;
import com.discover.step.Session;
import com.discover.step.async.SafeAsyncTask;
import com.discover.step.bl.AchievementManager;
import com.discover.step.bl.GPSHandlerManager;
import com.discover.step.bl.LocationStoreProxy;
import com.discover.step.bl.PrefManager;
import com.discover.step.bl.StepManager;
import com.discover.step.bl.UserManager;
import com.discover.step.helper.BitmapHelper;
import com.discover.step.interfaces.IGpsLoggerServiceClient;
import com.discover.step.model.Achievement;
import com.discover.step.model.Challenge;
import com.discover.step.model.StepPoint;
import com.discover.step.model.User;
import com.discover.step.util.MarkerImageBuilder;
import com.gc.materialdesign.views.ButtonFloat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geri on 2015.03.04..
 */
public class CatchMapFragment extends Fragment implements IGpsLoggerServiceClient {

    public static final String TAG = "Catch Map Fragment";

    private User currentUser;
    private MapView mMapView;
    private GoogleMap mMap;
    private Marker mainMarker;
    private TextView mInformationTv;

    private ButtonFloat mStartDrawingBt;
    private RelativeLayout mProgressRl, mGMRootRl;

    private boolean mIsAppInBackground = false;

    //Store missing locations.
    List<StepPoint> missingStepPoints;
    List<Challenge> challengePoints;

    Location lastLocation;
    Location currentLocation;
    StepPoint currentStepPoint;

    MainActivity mActivity;

    BroadcastReceiver mChallengeEndedBr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Challenge challenge = (Challenge) intent.getSerializableExtra("challenge");
            if (challenge != null) {
                int index = -1;
                for (int i = 0; i< challengePoints.size();i++) {
                    if (challengePoints.get(i).challange_id.equalsIgnoreCase(challenge.challange_id)) {
                        index = i;
                        break;
                    }
                }

                if (index > -1) {
                    challengePoints.remove(index);
                }

                updateChallengePoints();
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        View view = inflater.inflate(R.layout.fragment_google_map,container,false);

        mInformationTv = (TextView) view.findViewById(R.id.main_google_map_textTv);
        mProgressRl = (RelativeLayout) view.findViewById(R.id.drawing_progressRl);

        mStartDrawingBt = (ButtonFloat) view.findViewById(R.id.buttonFloat);
        mMapView = ((MapView) view.findViewById(R.id.drawing_mapView));
        mGMRootRl = (RelativeLayout) view.findViewById(R.id.google_map_rootRl);

        missingStepPoints = new ArrayList<>();
        challengePoints = new ArrayList<>();

        //Initialize Session.
        Session.start();

        //Initialize Map.
        initMap(savedInstanceState);

        return view;
    }

    /**
     * Initialize map.
     */
    private void initMap(Bundle savedInstanceState) {
        mMapView.onCreate(savedInstanceState);
        MapsInitializer.initialize(mActivity);

        mMap = mMapView.getMap();
        mMapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentLocation = GPSHandlerManager.getInstance().getCurrentLocation();

                //show progress bar till we don't have current location.
                if (currentLocation == null) {
                    mMapView.postDelayed(this, 1000);
                    return;
                }

                final LatLng location = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                mMap.clear();
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setCompassEnabled(false);

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 18));

                //Add main marker.
                addMainMarker();

                mMapView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideInfoLayer(mProgressRl);
                        MainActivity.isOptionsMenuEnabled = true;
                        getActivity().supportInvalidateOptionsMenu();

                        showFunctionInfoDialog();

                    }
                }, 500);

                //init last step location.
                lastLocation = currentLocation;
            }
        }, 1000);
    }

    private void addMainMarker() {
        currentLocation = GPSHandlerManager.getInstance().getCurrentLocation();
        if (currentLocation == null)
            return;

        //Add a placeholder before image loading.
        mainMarker = mMap.addMarker(new MarkerOptions()
                .icon(new MarkerImageBuilder(mActivity.getResources())
                        .asPrimary(true)
                        .withSize(20)
                        .withColor(R.color.main_marker_color).build())
                .position(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude())));

        ImageLoader.getInstance().loadImage(Session.user.picture_url,new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                Bitmap bmp = BitmapHelper.getCenterChorpedRoundedBitmap(loadedImage, 36);

                mainMarker.setIcon(new MarkerImageBuilder(mActivity.getResources())
                        .asPrimary(true)
                        .withSize(20)
                        .withProfileImage(bmp)
                        .withColor(R.color.main_marker_color).build());
            }
        });
    }

    public void addChallengePoint(Challenge challenge) {
        if (mMap == null)
            return;

        challengePoints.add(challenge);
        mMap.addMarker(new MarkerOptions()
                .icon(new MarkerImageBuilder(mActivity.getResources()).asPrimary(false).withColor(challenge.color).withSize(10).build())
                .position(new LatLng(challenge.lat, challenge.lng)));
    }

    public void updateChallengePoints() {
        if (mMap == null)
            return;

        mMap.clear();
        addMainMarker();

        for (Challenge challenge : challengePoints) {
            mMap.addMarker(new MarkerOptions()
                    .icon(new MarkerImageBuilder(mActivity.getResources()).asPrimary(false).withColor(challenge.color).withSize(10).build())
                    .position(new LatLng(challenge.lat, challenge.lng)));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        mIsAppInBackground = false;

        if (mMapView != null) {
            mMapView.onResume();
        }
        super.onResume();

        //Start broadcast listener.
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mChallengeEndedBr,new IntentFilter(Config.CONST_CHALLENGE_HAS_ENDED));
        updateChallengePoints();
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mChallengeEndedBr);

        mIsAppInBackground = true;
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

    @Override
    public void onNewStepPointsAvailable(StepPoint stepPoint) {
        currentStepPoint = stepPoint;
    }

    @Override
    public void onMainPositionChange(Location stepPoint) {
        if (stepPoint != null) {
            mainMarker.setPosition(new LatLng(stepPoint.getLatitude(),stepPoint.getLongitude()));
        }
    }

    public void showInfoLayerWithText(String title) {
        mInformationTv.setText(title);
        showInfoLayer(mProgressRl);
    }

    public void hideInfoLayer() {
        hideInfoLayer(mProgressRl);
    }

    private void showInfoLayer(View view) {
        view.setVisibility(View.VISIBLE);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(1000);

        view.startAnimation(fadeIn);
    }

    private void hideInfoLayer(final View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setDuration(500);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.startAnimation(fadeOut);
    }

    private void showFunctionInfoDialog() {
        //Show information window about highlighted mode, at first start.
        if (!PrefManager.getInstance().isInformationScreenWasShown()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new ShowFunctionDialog(getActivity());
                    PrefManager.getInstance().setIsInformationScreenWasShown();
                }
            },1000);
        }
    }

    public interface OnMarkersLoadedFinishListener {
        public void onReady();
    }
}
