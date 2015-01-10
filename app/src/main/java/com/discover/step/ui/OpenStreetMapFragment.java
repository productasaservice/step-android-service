package com.discover.step.ui;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.discover.step.R;
import com.discover.step.bl.GPSHandlerManager;
import com.discover.step.overlays.GroundOverlay;

import org.osmdroid.api.IMap;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

/**
 * Created by Morpheus on 2014.12.29..
 */
public class OpenStreetMapFragment extends Fragment {

    private GPSHandlerManager mGpsPositionHandler;
    private ImageView mStartDrawingIv;

    private boolean mIsDrawingEnabled = false;
    private MapController mMapController;
    private MapView mMapView;
    private IMap mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_osm_map,container,false);

        mGpsPositionHandler = GPSHandlerManager.getInstance();

        mStartDrawingIv = (ImageView) view.findViewById(R.id.start_drawingIv);
        mMapView = (MapView) view.findViewById(R.id.drawing_mapView);

        mStartDrawingIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsDrawingEnabled) {
                    //mStartDrawingIv.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_step_off));
                    mIsDrawingEnabled = false;
                    mGpsPositionHandler.stopUpdates();
                } else {
                   // mStartDrawingIv.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_step_on));
                    mIsDrawingEnabled = true;
                    mGpsPositionHandler.startUpdates();
                }
            }
        });

        initMap();

        return view;
    }

    private void initMap() {
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
        mMapView.setClickable(true);
        mMapView.setUseDataConnection(false);

        mMapController = (MapController) mMapView.getController();
        mMapController.setZoom(13);

        GeoPoint gPt = new GeoPoint(51500000, -150000);
        mMapController.setCenter(gPt);

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                Location loc = GPSHandlerManager.getInstance().getCurrentLocation();
                Log.d("test--", "Location: " + loc.getLatitude() + " | " + loc.getLongitude());
                mMapController = (MapController) mMapView.getController();
                mMapController.setZoom(13);
                GeoPoint gPt = new GeoPoint(loc.getLatitude(), loc.getLongitude());

                GroundOverlay whiteGroundOverlay = new GroundOverlay(getActivity());
                whiteGroundOverlay.setPosition(gPt);
                whiteGroundOverlay.setImage(getResources().getDrawable(R.drawable.ground_overlay_512));
                whiteGroundOverlay.setDimensions(9000000f,9000000f);

                mMapView.getOverlays().add(whiteGroundOverlay);

                mMapController.animateTo(gPt);
                mMapView.invalidate();
            }
        },2000);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGpsPositionHandler.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGpsPositionHandler.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mGpsPositionHandler.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGpsPositionHandler.onStop();
    }

}
