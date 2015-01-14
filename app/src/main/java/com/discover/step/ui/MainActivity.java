package com.discover.step.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialogCompat;
import com.discover.step.R;
import com.discover.step.bl.NotificationManager;


public class MainActivity extends ActionBarActivity {

    private static final int GPS_REQUEST_CODE = 695;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.map_layoutFl, new GoogleMapFragment()).commit();

        initActionBar();

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME);
        getSupportActionBar().setIcon(R.drawable.ic_title);
    }

    private void buildAlertMessageNoGps() {
        MaterialDialogCompat.Builder dialogBuilder = new MaterialDialogCompat.Builder(this);
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
                MainActivity.super.onBackPressed();
            }
        });

        dialogBuilder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        MaterialDialogCompat.Builder dialogBuilder = new MaterialDialogCompat.Builder(this);
        dialogBuilder.setTitle(R.string.exit_dialog_title);
        dialogBuilder.setMessage(R.string.exit_dialog_text);
        dialogBuilder.setPositiveButton(R.string.exit_dialog_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NotificationManager.getInstance().setEnabled(false);
                MainActivity.super.onBackPressed();
                NotificationManager.getInstance().hideNotification();
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


}
