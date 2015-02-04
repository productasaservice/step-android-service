package com.discover.step.ui;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import com.discover.step.R;
import com.discover.step.Session;
import com.discover.step.async.SafeAsyncTask;
import com.discover.step.bc.DatabaseConnector;
import com.discover.step.bl.AchievementManager;
import com.discover.step.bl.GPSHandlerManager;
import com.discover.step.bl.LocationStoreProxy;
import com.discover.step.bl.PrefManager;
import com.discover.step.bl.StepManager;
import com.discover.step.bl.UserManager;
import com.discover.step.ex.DefaultStepException;
import com.discover.step.interfaces.IGpsLoggerServiceClient;
import com.discover.step.model.Achievement;
import com.discover.step.model.Day;
import com.discover.step.model.StepPoint;
import com.discover.step.model.User;
import com.discover.step.util.MarkerImageBuilder;
import com.gc.materialdesign.views.ButtonFloat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Morpheus on 2014.12.29..
 */
public class GoogleMapFragment extends Fragment implements IGpsLoggerServiceClient {

    public static final String TAG = "Google Map Fragment";

    private User currentUser;
    private MapView mMapView;
    private GoogleMap mMap;
    private Marker mainMarker;
    //private RelativeLayout mInformationRl;
    private TextView mInformationTv;

    private ButtonFloat mStartDrawingBt;
    private RelativeLayout mProgressRl, mGMRootRl;

    private boolean mIsDrawingEnabled = false;
    private boolean mIsUpdateNeeded = false;
    private boolean mIsAppInBackground = false;

    //Store missing locations.
    List<StepPoint> missingStepPoints;

    Location lastLocation;
    Location currentLocation;
    StepPoint currentStepPoint;

    private LatLngBounds BOUNDS;
    private final int MIN_ZOOM = 10;
    private static int STEP_COUNT = 0;

    MainActivity mActivity;

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

        //mInformationRl = (RelativeLayout) view.findViewById(R.id.map_informationRl);
        mProgressRl = (RelativeLayout) view.findViewById(R.id.drawing_progressRl);

        mStartDrawingBt = (ButtonFloat) view.findViewById(R.id.buttonFloat);
        mMapView = ((MapView) view.findViewById(R.id.drawing_mapView));
        mGMRootRl = (RelativeLayout) view.findViewById(R.id.google_map_rootRl);

        missingStepPoints = new ArrayList<>();

        //Highlighted mode switch button
        mStartDrawingBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                highlightDrawing();
            }
        });

        //Initialize Session.
        Session.start();
        PrefManager.getInstance().setUserStepCount(Session.authenticated_user_social_id,Session.step_count);

        mMapView.onCreate(savedInstanceState);

        //Initialize Map.
        initMap();

        return view;
    }

    /**
     * Initialize map.
     */
    private void initMap() {
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

                GroundOverlayOptions newarkMap = new GroundOverlayOptions().image(BitmapDescriptorFactory.fromResource(R.drawable.ground_overlay)).position(location, 8600000f, 8600000f);
                mMap.clear();
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                mMap.addGroundOverlay(newarkMap);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setCompassEnabled(false);

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
                List<LatLng> boundList = getBoundingBox(location, 80);
                BOUNDS = new LatLngBounds(boundList.get(0), boundList.get(1));
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(BOUNDS, 0));

                //Add main marker.
                mainMarker = mMap.addMarker(new MarkerOptions()
                        .icon(new MarkerImageBuilder(mActivity.getResources()).asPrimary(false).withColor(R.color.main_marker_color).build())
                        .position(location));

                //Limit min zoom level.
                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    private float currentZoom = -1;

                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        if (cameraPosition.zoom != currentZoom) {
                            currentZoom = cameraPosition.zoom;
                            if (currentZoom < MIN_ZOOM) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(convertLocationToLatLng(currentLocation), MIN_ZOOM));
                                currentZoom = MIN_ZOOM;
                            }
                        }
                    }
                });

                //Need it to return true to avoid in build feature. (route planning)
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        return true;
                    }
                });

                //On map click event handler.
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        highlightDrawing();
                    }
                });

                //Show map and float button with 500 ms delay.
                //mStartDrawingBt.isAnimate(true);
                mMapView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //mProgressRl.setVisibility(View.GONE);
                        hideInfoLayer(mProgressRl);
                        //mStartDrawingBt.setVisibility(View.VISIBLE);
                        animateMainMarker();
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

    /**
     * Add your last position marker to map.
     * @param location
     */
    private void addStepMark(StepPoint location) {

        if (!mIsAppInBackground) {
            //Add marker to map.
            mMap.addMarker(new MarkerOptions()
                    .icon(new MarkerImageBuilder(getResources()).asPrimary(false).withColor(location.color).build())
                    .position(new LatLng(location.latitude, location.longitude)));

            location.isVisibleOnMap = true;
        } else {
            missingStepPoints.add(location);
            mIsUpdateNeeded = true;
        }

        Achievement achievement = AchievementManager.getInstance().checkForNewBadge();
        if (achievement != null) {
            Toast.makeText(mActivity,achievement.message,Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Add loaded step points.
     * @param stepPoints
     */
    public void addFurtherSteps(List<StepPoint> stepPoints) {
        Log.d("test--","further stpe:" + stepPoints.size());
        for (StepPoint sp : stepPoints) {
            mMap.addMarker(new MarkerOptions()
                    .icon(new MarkerImageBuilder(mActivity.getResources()).asPrimary(false).withColor(sp.color).withAlphaEnabled(true).build())
                    .position(new LatLng(sp.latitude, sp.longitude)));
        }

        hideInfoLayer(mProgressRl);
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

        //Load gps points witch hadn't draw to map.
        if (mIsUpdateNeeded) {
            new DrawMarkersTask(true).execute();
            currentLocation = GPSHandlerManager.getInstance().getCurrentLocation();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 18));
            mainMarker.setPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        }

        mIsUpdateNeeded = false;
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
        mIsAppInBackground = true;
        mIsUpdateNeeded = true;
    }

    @Override
    public void onDestroy() {
        mIsUpdateNeeded = true;

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

        if (stepPoint != null) {
            addStepMark(stepPoint);
        }

        currentStepPoint = stepPoint;
    }

    @Override
    public void onMainPositionChange(Location stepPoint) {
        if (stepPoint != null) {
            mainMarker.setPosition(new LatLng(stepPoint.getLatitude(),stepPoint.getLongitude()));
        }
    }

    /**
     * Highlight enable / disable func.
     */
    private void highlightDrawing() {
        if (mIsDrawingEnabled) {
            mIsDrawingEnabled = false;
            PrefManager.getInstance().setIsHighlightedEnabled(false);
            Toast.makeText(getActivity(), R.string.drawing_disabled, Toast.LENGTH_SHORT).show();
        } else {
            mIsDrawingEnabled = true;
            PrefManager.getInstance().setIsHighlightedEnabled(true);
            Toast.makeText(getActivity(), R.string.drawing_enabled, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set flashing animation to main marker.
     */
    private void animateMainMarker() {
        new Handler().postDelayed(new Runnable() {
            boolean isPrimary = true;

            @Override
            public void run() {
                if (isVisible()) {
                    LatLng curr_loc = mainMarker.getPosition();
                        if (isPrimary) {
                            mainMarker.setIcon(new MarkerImageBuilder(mActivity.getResources()).asPrimary(true).withColor(R.color.main_marker_color).build());
                            isPrimary = false;
                        } else {
                            mainMarker.setIcon(new MarkerImageBuilder(mActivity.getResources()).asPrimary(false).withColor(R.color.main_marker_color).build());
                            isPrimary = true;
                        }

                    mainMarker.setPosition(curr_loc);
                    new Handler().postDelayed(this, 300);
                }
            }
        }, 300);
    }

    public List<LatLng> getBoundingBox(final LatLng pPosition, final int pDistanceInMeters) {

        final List<LatLng> boundingBox = new ArrayList<>();

        final double latRadian = Math.toRadians(pPosition.latitude);

        final double degLatKm = 110.574235;
        final double degLongKm = 110.572833 * Math.cos(latRadian);
        final double deltaLat = pDistanceInMeters / 1000.0 / degLatKm;
        final double deltaLong = pDistanceInMeters / 1000.0 / degLongKm;

        final double minLat = pPosition.latitude - deltaLat;
        final double minLong = pPosition.longitude - deltaLong;
        final double maxLat = pPosition.latitude + deltaLat;
        final double maxLong = pPosition.longitude + deltaLong;

        boundingBox.add(new LatLng(minLat,minLong));
        boundingBox.add(new LatLng(maxLat,maxLong));

        return boundingBox;
    }

    /**
     * Location to LatLng converter.
     * @param location
     * @return
     */
    private LatLng convertLocationToLatLng(Location location) {
        return new LatLng(location.getLatitude(),location.getLongitude());
    }

    /**
     * Draw marker in background thread task.
     */
    private class DrawMarkersTask extends SafeAsyncTask<Void,Void,List<StepPoint>> {

        private boolean isMissingPointsNeeded;
        private OnMarkersLoadedFinishListener listener;

        public DrawMarkersTask(boolean isMissingPointsNeeded) {
            this.isMissingPointsNeeded = isMissingPointsNeeded;
        }

        public DrawMarkersTask(boolean isMissingPointsNeeded, OnMarkersLoadedFinishListener listener) {
            this.isMissingPointsNeeded = isMissingPointsNeeded;
            this.listener = listener;
        }

        @Override
        protected List<StepPoint> doWorkInBackground(Void... params) throws Exception {
            if (isMissingPointsNeeded) {
                LocationStoreProxy.getInstance().forceOfStoreStepPoints();

                return StepManager.getInstance().getNotDrawnStepPoints();
            } else {
                return StepManager.getInstance().getStepPoints();
            }
        }

        @Override
        protected void onSuccess(List<StepPoint> stepPoints) {
            super.onSuccess(stepPoints);
            for (StepPoint sp : stepPoints) {
                mMap.addMarker(new MarkerOptions()
                        .icon(new MarkerImageBuilder(mActivity.getResources()).asPrimary(false).withColor(sp.color).withAlphaEnabled(!isMissingPointsNeeded).build())
                        .position(new LatLng(sp.latitude, sp.longitude)));
            }

            if (listener != null) {
                listener.onReady();
            }
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

    //just for testing.
    private void testPoint() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("test--","beszúrtam egy teszt pontot");
                Location location = new Location("valami");
                location.setLongitude(20);
                location.setLatitude(20);

                //Create new step point instance from location.
                StepPoint stepPoint = new StepPoint();
                stepPoint.bindLocation(location);

                //Increase step.
                Session.increaseStep();

                //Insert step point into db using proxy.
                LocationStoreProxy.getInstance().insertStepPoint(stepPoint);

                new Handler().postDelayed(this,2500);
            }
        }, 2500);
    }
}
