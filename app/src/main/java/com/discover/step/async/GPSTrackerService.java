package com.discover.step.async;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.discover.step.R;
import com.discover.step.bl.GPSHandlerManager;
import com.discover.step.interfaces.IGpsLoggerServiceClient;
import com.discover.step.ui.MainActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Geri on 2015.01.18..
 */
public class GPSTrackerService extends Service {

    private static int NOTIFICATION_ID = 8675309;
    private static NotificationManager mNotificationManager;
    private static IGpsLoggerServiceClient mainServiceClient;
    private GPSHandlerManager gpsHandlerManager;

    private final IBinder mBinder = new GpsLoggingBinder();

    AlarmManager mNextPointAlarmManager;
    private NotificationCompat.Builder nfc = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mNextPointAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        gpsHandlerManager = GPSHandlerManager.getInstance();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class GpsLoggingBinder extends Binder {
        public GPSTrackerService getService() {
            return GPSTrackerService.this;
        }
    }

    public static void setServiceClient(IGpsLoggerServiceClient client) {
        mainServiceClient = client;
    }

    @Override
    public void onDestroy() {
        mainServiceClient = null;
        gpsHandlerManager.onStop();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        HandleIntent(intent);
        gpsHandlerManager.onStart();
        return START_REDELIVER_INTENT;
    }

    private void HandleIntent(Intent intent) {

        if (intent != null) {
            Bundle bundle = intent.getExtras();

            if (bundle != null) {

                boolean stopRightNow = bundle.getBoolean("immediatestop");
                boolean startRightNow = bundle.getBoolean("immediatestart");

                if (startRightNow) {
                    Log.d("GpsTrackerService", "Intent received - Start Logging Now");
                    gpsHandlerManager.startUpdates();
                    gpsHandlerManager.setOnLocationChangeListener(new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            if (isMainFormVisible()) {
                                mainServiceClient.OnLocationUpdate(location);
                            }
                        }
                    });

                    showNotification();

                }

                if (stopRightNow) {
                    Log.d("GpsTrackerService", "Intent received - Stop logging now");
                    gpsHandlerManager.stopUpdates();
                    gpsHandlerManager.onStop();
                    removeNotification();
                }
            }
        }
    }

    /**
     * Hides the notification icon in the status bar if it's visible.
     */
    private void removeNotification() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    /**
     * Shows a notification icon in the status bar for GPS Logger
     */
    private void showNotification() {
        Log.d("GpsTrackerService", "GpsLoggingService.showNotification");
        // What happens when the notification item is clicked
        Intent contentIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(contentIntent);

        PendingIntent pending = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        String contentText = getString(R.string.notification_text);
        long notificationTime = System.currentTimeMillis();

        if (nfc == null) {
            nfc = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.ic_step_notification)
                    .setContentTitle(getString(R.string.notification_text))
                    .setOngoing(true)
                    .setContentIntent(pending);
        }

        nfc.setContentText(contentText);
        nfc.setWhen(notificationTime);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, nfc.build());
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    private boolean isMainFormVisible() {
        return mainServiceClient != null;
    }

    public Location getCurrentLocation() {
        return gpsHandlerManager.getCurrentLocation();
    }

    public LatLng getCurrentLatLng() {
        Location loc = getCurrentLocation();
        return loc != null ? new LatLng(loc.getLatitude(),loc.getLongitude()) : null;
    }
}
