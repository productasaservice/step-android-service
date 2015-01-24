package com.discover.step.ui;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.discover.step.R;
import com.discover.step.async.SafeAsyncTask;
import com.discover.step.bl.GPSHandlerManager;
import com.discover.step.bl.LocationStoreProxy;
import com.discover.step.bl.StepManager;
import com.discover.step.interfaces.IGpsLoggerServiceClient;
import com.discover.step.model.StepPoint;
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

    private MapView mMapView;
    private GoogleMap mMap;
    private Marker mainMarker;

    private ButtonFloat mStartDrawingBt;
    private RelativeLayout mProgressRl, mGMRootRl;

    private boolean mIsDrawingEnabled = false;
    private boolean mIsUpdateNeeded = false;
    private boolean mIsAppInBackground = false;

    //Store missing locations.
    List<StepPoint> missingStepPoints;

    Location lastLocation;
    Location currentLocation;

    private LatLngBounds BOUNDS;
    private final int MIN_ZOOM = 10;

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

        mStartDrawingBt = (ButtonFloat) view.findViewById(R.id.buttonFloat);
        mProgressRl = (RelativeLayout) view.findViewById(R.id.drawing_progressRl);
        mMapView = ((MapView) view.findViewById(R.id.drawing_mapView));
        mGMRootRl = (RelativeLayout) view.findViewById(R.id.google_map_rootRl);
        mMapView.onCreate(savedInstanceState);

        missingStepPoints = new ArrayList<>();

        mStartDrawingBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsDrawingEnabled) {
                    mIsDrawingEnabled = false;
                    Toast.makeText(getActivity(), R.string.drawing_disabled, Toast.LENGTH_SHORT).show();
                } else {
                    mIsDrawingEnabled = true;
                    Toast.makeText(getActivity(), R.string.drawing_enabled, Toast.LENGTH_SHORT).show();
                }
            }
        });

        initMap();

        return view;
    }

    private void initMap() {
        MapsInitializer.initialize(mActivity);

        mMap = mMapView.getMap();
        mMapView.postDelayed(new Runnable() {
            @Override
            public void run() {
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

                //Add GPS points from db.
                missingStepPoints.addAll(StepManager.getInstance().getStepPoints());

                //Show map and float button with 500 ms delay.
                mStartDrawingBt.isAnimate(true);
                mMapView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressRl.setVisibility(View.GONE);
                        mStartDrawingBt.setVisibility(View.VISIBLE);
                        new DrawMarkersTask(false).execute();
                        animateMainMarker();
                    }
                }, 500);

                //init last step location.
                lastLocation = currentLocation;
            }
        }, 1000);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {

        if (mMapView != null) {
            mMapView.onResume();
        }
        super.onResume();

        mIsAppInBackground = false;

        if (mIsUpdateNeeded) {
            new DrawMarkersTask(true).execute();
        }
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
        mIsDrawingEnabled = false;
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

    /**
     * Calculate distance from last step.
     * @param location
     * @return
     */
    private int distanceFromLastPoint(Location location) {
        return (int) location.distanceTo(lastLocation);
    }

    /**
     * Add your last position marker to map.
     * @param location
     */
    private void addStepMark(Location location) {
        lastLocation = location;
        StepPoint draw_point = new StepPoint();
        draw_point.bindLocation(location);
        draw_point.color = mIsDrawingEnabled ? "#" + Integer.toHexString(getResources().getColor(R.color.main_marker_color)) :
                "#" + Integer.toHexString(getResources().getColor(R.color.secondary_marker_color));
        draw_point.isDrawnPoint = mIsDrawingEnabled;

        if (!mIsAppInBackground) {
            //Add marker to map.
            mMap.addMarker(new MarkerOptions()
                    .icon(new MarkerImageBuilder(getResources()).asPrimary(false).withColor(draw_point.color).build())
                    .position(new LatLng(location.getLatitude(), location.getLongitude())));

            draw_point.isVisibleOnMap = true;
        }

        //Insert step point into db using proxy.
        LocationStoreProxy.getInstance().insertStepPoint(draw_point);
    }

    /**
     * Draw the missing markers and update camera & main marker position.
     */
//    private void updateMissingMarkers() {
//        mMapView.post(new Runnable() {
//            @Override
//            public void run() {
//                if (!missingStepPoints.isEmpty()) {
//                    Location current_location = GPSHandlerManager.getInstance().getCurrentLocation();
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(current_location.getLatitude(),current_location.getLongitude()), 18));
//                    mainMarker.setPosition(new LatLng(current_location.getLatitude(),current_location.getLongitude()));
//
//                    List<StepPoint> temp = new ArrayList<>();
//                    int COUNT = 15;
//
//                    for (StepPoint sp : missingStepPoints) {
//
//                        if (temp.size() < COUNT) {
//                            temp.add(sp);
//                        } else {
//                            double avg_lat = 0;
//                            double avg_lng = 0;
//                            for (StepPoint p : temp) {
//                                avg_lat = avg_lat + p.latitude;
//                                avg_lng = avg_lng + p.longitude;
//                            }
//
//                            mMap.addMarker(new MarkerOptions()
//                                    .icon(new MarkerImageBuilder(mActivity.getResources()).asPrimary(false).withColor(sp.color).build())
//                                    .position(new LatLng(avg_lat / temp.size(), avg_lng / temp.size())));
//                            temp = new ArrayList<>();
//                            temp.add(sp);
//                        }
//                    }
//
//                    missingStepPoints.clear();
//                }
//            }
//        });
//    }

    //Draw section.
    @Override
    public void OnLocationUpdate(Location location) {
        if (location != null && currentLocation != null && currentLocation.getLatitude() != location.getLatitude() && currentLocation.getLongitude() != location.getLongitude()) {
            if (!mIsAppInBackground && mainMarker != null) {
                mainMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            }

            if (lastLocation != null && distanceFromLastPoint(location) >= 5) {
                addStepMark(location);
            }
        }
        currentLocation = location;
    }

    @Override
    public void OnStartLogging() {

    }

    @Override
    public void OnStopLogging() {

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

    private LatLng convertLocationToLatLng(Location location) {
        return new LatLng(location.getLatitude(),location.getLongitude());
    }

    private class DrawMarkersTask extends SafeAsyncTask<Void,Void,List<StepPoint>> {

        private boolean isMissingPointsNeeded;

        public DrawMarkersTask(boolean isMissingPointsNeeded) {
            this.isMissingPointsNeeded = isMissingPointsNeeded;
        }

        @Override
        protected List<StepPoint> doWorkInBackground(Void... params) throws Exception {
            if (isMissingPointsNeeded) {
                LocationStoreProxy.getInstance().forceOfStoreStepPoints();

                return StepManager.getInstance().getNotDrawnStepPoints();
            } else {
                return StepManager.getInstance().getNotDrawnStepPoints();
            }
        }

        @Override
        protected void onSuccess(List<StepPoint> stepPoints) {
            super.onSuccess(stepPoints);
            if (isMissingPointsNeeded) {
                currentLocation = GPSHandlerManager.getInstance().getCurrentLocation();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 18));
                mainMarker.setPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            }

            for (StepPoint sp : stepPoints) {
                mMap.addMarker(new MarkerOptions()
                        .icon(new MarkerImageBuilder(mActivity.getResources()).asPrimary(false).withColor(sp.color).build())
                        .position(new LatLng(sp.latitude,sp.longitude)));
            }
        }
    }
}
