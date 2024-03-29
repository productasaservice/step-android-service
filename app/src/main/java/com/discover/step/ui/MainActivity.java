package com.discover.step.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialogCompat;
import com.discover.step.R;
import com.discover.step.Session;
import com.discover.step.async.GPSTrackerService;
import com.discover.step.async.StepDataSyncService;
import com.discover.step.bc.DatabaseConnector;
import com.discover.step.bc.ServerConnector;
import com.discover.step.bl.ChallengeManager;
import com.discover.step.bl.LocationStoreProxy;
import com.discover.step.bl.StepManager;
import com.discover.step.interfaces.IGpsLoggerServiceClient;
import com.discover.step.model.Challenge;
import com.discover.step.model.Day;
import com.discover.step.model.StepPoint;
import com.discover.step.social.FbHandlerV3;
import com.facebook.widget.FriendPickerFragment;

import java.util.List;


public class MainActivity extends SocialActivity {

    private static final int GPS_REQUEST_CODE = 695;
    private static final String TAG = "Main Activity";

    private static Fragment fragment, profile, challenge;
    private GPSTrackerService trackingService;
    private Intent serviceIntent;

    public static boolean isOptionsMenuEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, StepDataSyncService.class));

        fragment = new CatchMapFragment();
        profile = new ProfileFragment();
        challenge = new ChallengeFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.map_layoutFl, fragment).commit();

        initActionBar();

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }

        Session.start();
        //FbHandlerV3.getInstance(this).sendChallenge();
        ChallengeManager.getInstance().downloadChallengeByUserId(Session.authenticated_user_social_id);

        //Handle of request.
        //String request_id = "26211636724531788638";
        String request_id = "";

        if (!request_id.equalsIgnoreCase("")) {
            ChallengeManager.getInstance().downloadChallengeByChallengeId(request_id,new ChallengeManager.OnReady<Challenge>() {
                @Override
                public void onReady(final Challenge data) {
                    ChallengeDialog dialog = new ChallengeDialog(MainActivity.this, data);
                    dialog.setOnAcceptButtonListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ChallengeManager.getInstance().acceptChallenge(data.challange_id,null);
                        }
                    });

                }
            });
        }

        Log.d("test--","ts: " + System.currentTimeMillis() + (360000));
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME);
        getSupportActionBar().setIcon(R.drawable.ic_title);
    }

    private void buildAlertMessageNoGps() {
        MaterialDialogCompat.Builder dialogBuilder = new MaterialDialogCompat.Builder(this);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle(R.string.no_gps_title);
        dialogBuilder.setMessage(R.string.no_gps_message);
        dialogBuilder.setPositiveButton(R.string.no_gps_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),GPS_REQUEST_CODE);
            }
        });
        dialogBuilder.setNegativeButton(R.string.no_gps_cancel,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopAndUnbindServiceIfRequired();
                MainActivity.super.onBackPressed();
            }
        });

        dialogBuilder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAndBindService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (isOptionsMenuEnabled) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_profile) {
//            if (profile.isAdded() && profile.isHidden()) {
//                getSupportFragmentManager().beginTransaction()
//                        .show(profile).commit();
//
//                ((ProfileFragment) profile).updateScreenData();
//            } else {
//                getSupportFragmentManager().beginTransaction()
//                        .add(R.id.content_layoutFl, profile).commit();
//            }

            FbHandlerV3.getInstance(this).sendChallenge();

            return true;
        } else if (id == R.id.action_challenge) {
            if (challenge.isAdded() && challenge.isHidden()) {
                getSupportFragmentManager().beginTransaction()
                        .show(challenge).commit();
                ((ChallengeFragment) challenge).updateScreenData();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.content_layoutFl, challenge).commit();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (profile.isVisible()) {
            ((ProfileFragment)profile).closeAnimation(new ProfileFragment.OnAnimEndListener() {
                @Override
                public void onReady() {
                    FragmentManager fm = getSupportFragmentManager();
                    fm.beginTransaction().hide(profile).commit();

                    MainActivity.isOptionsMenuEnabled = true;
                    supportInvalidateOptionsMenu();
                }
            });

            return;
        }

        if (challenge.isVisible()) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().hide(challenge).commit();

            return;
        }

        MaterialDialogCompat.Builder dialogBuilder = new MaterialDialogCompat.Builder(this);
        dialogBuilder.setTitle(R.string.exit_dialog_title);
        dialogBuilder.setMessage(R.string.exit_dialog_text);
        dialogBuilder.setPositiveButton(R.string.exit_dialog_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                MainActivity.super.onBackPressed();
                LocationStoreProxy.getInstance().forceOfStoreStepPoints();
                stopAndUnbindServiceIfRequired();
            }
        });
        dialogBuilder.setNegativeButton(R.string.dialog_cancel,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        dialogBuilder.show();
    }

    /**
     * Starts the service and binds the activity to it.
     */
    private void startAndBindService() {
        serviceIntent = new Intent(this, GPSTrackerService.class);
        serviceIntent.putExtra("immediatestart",true);
        // Start the service in case it isn't already running
        startService(serviceIntent);
        // Now bind to service
        bindService(serviceIntent, gpsServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Stops the service if it isn't logging. Also unbinds.
     */
    private void stopAndUnbindServiceIfRequired() {
        try {
            serviceIntent = new Intent(this, GPSTrackerService.class);
            serviceIntent.putExtra("immediatestop",true);
            startService(serviceIntent);

            unbindService(gpsServiceConnection);

            stopService(serviceIntent);
        } catch (Exception e) {
            Log.d(TAG, "Could not unbind service", e);
        }
    }

    /**
     * Provides a connection to the GPS Logging Service
     */
    private final ServiceConnection gpsServiceConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"Disconnected from GPSLoggingService from MainActivity");
            trackingService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Connected to GPSLoggingService from GoogleMapFragment");
            trackingService = ((GPSTrackerService.GpsLoggingBinder) service).getService();
            GPSTrackerService.setServiceClient((IGpsLoggerServiceClient)fragment);
        }
    };

    public static void showMarkersOnMap(Day day) {
        if (fragment != null) {
            ((GoogleMapFragment) fragment).showInfoLayerWithText("Restoring your STEP points");
            List<StepPoint> stepPoints = StepManager.getInstance().getStepPointsByTimeStamp(day.date_ts);
            if (stepPoints.isEmpty()) {
                ServerConnector.getInstance().getStepList(Session.authenticated_user_social_id,day.date_ts,new ServerConnector.OnServerResponseListener<List<StepPoint>>() {
                    @Override
                    public void onReady(List<StepPoint> response, boolean isSuccess) {
                        if (isSuccess) {
                            ((GoogleMapFragment) fragment).addFurtherSteps(response);
                        } else {
                            ((GoogleMapFragment) fragment).hideInfoLayer();
                        }
                    }
                });
            } else {
                ((GoogleMapFragment) fragment).addFurtherSteps(stepPoints);
            }
        }
    }

    public void addChallengePoint(Challenge challenge) {
        if (fragment != null) {
            ((CatchMapFragment) fragment).addChallengePoint(challenge);
        }
    }
}
