package com.android.step.ui;

import android.app.Activity;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.step.R;
import com.android.step.bl.GPSHandlerManager;
import com.android.step.bl.NotificationManager;
import com.android.step.util.MarkerImageBuilder;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.IndoorBuilding;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Morpheus on 2014.12.29..
 */
public class GoogleMapFragment extends Fragment {

    private MapView mMapView;
    private GoogleMap mMap;
    private Marker mainMarker;

    private GPSHandlerManager mGpsPositionHandler;
    private ImageView mStartDrawingIv;
    private RelativeLayout mProgressRl;
    private NotificationManager mNotification;

    private boolean mIsDrawingEnabled = false;
    private boolean mIsNotificationIsVisible = false;

    List<Location> stepPoints;
    Location mLocation;

    private LatLngBounds BOUNDS;
    private final int MAX_ZOOM = 18;
    private final int MIN_ZOOM = 10;

    MainActivity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
        mGpsPositionHandler = GPSHandlerManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        View view = inflater.inflate(R.layout.fragment_google_map,container,false);

        mStartDrawingIv = (ImageView) view.findViewById(R.id.start_drawingIv);
        mProgressRl = (RelativeLayout) view.findViewById(R.id.drawing_progressRl);
        mMapView = ((MapView) view.findViewById(R.id.drawing_mapView));
        mMapView.onCreate(savedInstanceState);

        mNotification = NotificationManager.getInstance();

        stepPoints = new ArrayList<>();

        mStartDrawingIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsDrawingEnabled) {
                    mStartDrawingIv.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_step_off));
                    mIsDrawingEnabled = false;
                    mGpsPositionHandler.stopUpdates();
                    Toast.makeText(getActivity(), R.string.drawing_disabled, Toast.LENGTH_SHORT).show();
                } else {
                    mStartDrawingIv.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_step_on));
                    mIsDrawingEnabled = true;
                    mGpsPositionHandler.startUpdates();
                    addStepMark(mGpsPositionHandler.getCurrentLocation());
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
                Location location = GPSHandlerManager.getInstance().getCurrentLocation();
                //Location location = mLocation;
                if (location == null) {
                    mMapView.postDelayed(this,500);
                    return;
                }

                final LatLng current_location = new LatLng(location.getLatitude(),location.getLongitude());
                GroundOverlayOptions newarkMap = new GroundOverlayOptions().image(BitmapDescriptorFactory.fromResource(R.drawable.ground_overlay)).position(current_location,8600000f,8600000f);
                mMap.clear();
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                mMap.addGroundOverlay(newarkMap);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setCompassEnabled(false);

                mProgressRl.setVisibility(View.GONE);

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current_location, 18));
                List<LatLng> boundList = mGpsPositionHandler.getBoundingBox(current_location,80);
                BOUNDS = new LatLngBounds(boundList.get(0),boundList.get(1));
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(BOUNDS,0));

                //Add main marker.
                mainMarker = mMap.addMarker(new MarkerOptions()
                        .icon(new MarkerImageBuilder(getResources()).asPrimary(true).withColor(getResources().getColor(R.color.marker_green)).build())
                        .position(current_location));

                //Set min zoom level.
                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    private float currentZoom = -1;
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        if (cameraPosition.zoom != currentZoom) {
                            currentZoom = cameraPosition.zoom;
                            if (currentZoom < MIN_ZOOM) {
                                mLocation = mGpsPositionHandler.getCurrentLocation();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(),mLocation.getLongitude()), MIN_ZOOM));
                                currentZoom = MIN_ZOOM;
                            }
                        }
                    }
                });

                animateMainMarker();

            }
        },500);

        mGpsPositionHandler.setOnLocationChangeListener(new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null && mLocation != null && mLocation.getLatitude() != location.getLatitude() && mLocation.getLongitude() != location.getLongitude()) {
                        if (mainMarker != null) {
                            mainMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                        }

                        if (!stepPoints.isEmpty() && distanceFromLastPoint(location) >= 5 && mIsDrawingEnabled) {
                            addStepMark(mGpsPositionHandler.getCurrentLocation());
                        }
                    }
                    mLocation = location;
                }
        });
    }



    @Override
    public void onStart() {
        super.onStart();
        mGpsPositionHandler.onStart();
        Log.d("test--", "start update");
    }

    @Override
    public void onResume() {
        if (mMapView != null) {
            mMapView.onResume();
        }
        super.onResume();
        mGpsPositionHandler.onResume();
        Log.d("test--","resume update");
        if (mIsNotificationIsVisible) {
            mNotification.hideNotification();
            mIsNotificationIsVisible = false;
        }
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
        if (!mIsDrawingEnabled) {
            mGpsPositionHandler.onPause();
        }
        Log.d("test--","pause update");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!mIsDrawingEnabled) {
            mGpsPositionHandler.onStop();
        } else if (mIsDrawingEnabled && !mIsNotificationIsVisible) {
            mNotification.showNotification("Step","Tap here to view your steps!",R.drawable.ic_notification);
            mIsNotificationIsVisible = true;
        }

        Log.d("test--","stop update");
    }

    @Override
    public void onDestroy() {
        mGpsPositionHandler.onStop();
        if (mIsNotificationIsVisible) {
            mNotification.hideNotification();
            mIsNotificationIsVisible = false;
        }

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

    private int distanceFromLastPoint(Location location) {
        return (int) location.distanceTo(stepPoints.get(stepPoints.size() - 1));
    }

    private void addStepMark(Location location) {
        stepPoints.add(location);
        mMap.addMarker(new MarkerOptions()
                .icon(new MarkerImageBuilder(getResources()).asPrimary(false).withColor(getResources().getColor(R.color.marker_green)).build())
                .position(new LatLng(location.getLatitude(),location.getLongitude())));
        //animateAddMarker(location,step_mark);
    }

    /**
     * Returns the correction for Lat and Lng if camera is trying to get outside of visible map
     * @param cameraBounds Current camera bounds
     * @return Latitude and Longitude corrections to get back into bounds.
     */
    private LatLng getLatLngCorrection(LatLngBounds cameraBounds) {
        double latitude=0, longitude=0;
        if(cameraBounds.southwest.latitude < BOUNDS.southwest.latitude) {
            latitude = BOUNDS.southwest.latitude - cameraBounds.southwest.latitude;
        }
        if(cameraBounds.southwest.longitude < BOUNDS.southwest.longitude) {
            longitude = BOUNDS.southwest.longitude - cameraBounds.southwest.longitude;
        }
        if(cameraBounds.northeast.latitude > BOUNDS.northeast.latitude) {
            latitude = BOUNDS.northeast.latitude - cameraBounds.northeast.latitude;
        }
        if(cameraBounds.northeast.longitude > BOUNDS.northeast.longitude) {
            longitude = BOUNDS.northeast.longitude - cameraBounds.northeast.longitude;
        }
        return new LatLng(latitude, longitude);
    }

    /**
     * Bounds the user to the overlay.
     */
    private class OverscrollHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            CameraPosition position = mMap.getCameraPosition();
            VisibleRegion region = mMap.getProjection().getVisibleRegion();
            float zoom = 0;
            if(position.zoom < MIN_ZOOM) zoom = MIN_ZOOM;
            if(position.zoom > MAX_ZOOM) zoom = MAX_ZOOM;
            LatLng correction = getLatLngCorrection(region.latLngBounds);
            if(zoom != 0 || correction.latitude != 0 || correction.longitude != 0) {
                zoom = (zoom==0)?position.zoom:zoom;
                double lat = position.target.latitude + correction.latitude;
                double lon = position.target.longitude + correction.longitude;
                CameraPosition newPosition = new CameraPosition(new LatLng(lat,lon), zoom, position.tilt, position.bearing);
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(newPosition);
                mMap.moveCamera(update);
            }
        /* Recursively call handler every 100ms */
            sendEmptyMessageDelayed(0,100);
        }
    }

    private void animateMainMarker() {
        new Handler().postDelayed(new Runnable() {
            boolean isPrimary = true;

            @Override
            public void run() {
                if (isVisible()) {
                    if (isPrimary) {
                        mainMarker.setIcon(new MarkerImageBuilder(getResources()).asPrimary(true).withColor(getResources().getColor(R.color.marker_green)).build());
                        isPrimary = false;
                    } else {
                        mainMarker.setIcon(new MarkerImageBuilder(getResources()).asPrimary(false).withColor(getResources().getColor(R.color.marker_green)).build());
                        isPrimary = true;
                    }
                    new Handler().postDelayed(this, 300);
                }
            }
        }, 300);
    }

    private void animateAddMarker(Location point, final Marker marker){
        final long duration = 400;
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final LatLng target = new LatLng(point.getLatitude(),point.getLongitude());
        Projection proj = mMap.getProjection();

        Point startPoint = proj.toScreenLocation(target);
        startPoint.y = 0;
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);

        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * target.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * target.latitude + (1 - t) * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    // Post again 10ms later.
                    handler.postDelayed(this, 10);
                } else {
                    // animation ended
                }
            }
        });
    }
}
