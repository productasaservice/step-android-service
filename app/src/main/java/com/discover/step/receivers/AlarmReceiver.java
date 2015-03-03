package com.discover.step.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.discover.step.Config;
import com.discover.step.R;
import com.discover.step.model.Challenge;

/**
 * Created by Geri on 2015.03.03..
 */
public class AlarmReceiver  extends BroadcastReceiver {
    private NotificationManager mNotificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Challenge challenge = (Challenge)intent.getExtras().getSerializable("challenge");

        if (challenge != null) {
            NotificationCompat.Builder builder =  new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setTicker("End of " + challenge.title)
                    .setContentTitle(challenge.title)
                    .setContentText("Challenge time has expired" + (challenge.type == 1 ? ", You win!" : ", You lose!")).setAutoCancel(true);

            String id = challenge.challange_id.substring(0,5);
            mNotificationManager.notify((int) Integer.parseInt(id), builder.build());

            Intent endIntent = new Intent(Config.CONST_CHALLENGE_HAS_ENDED);
            endIntent.putExtra("challenge",challenge);
            LocalBroadcastManager.getInstance(context).sendBroadcast(endIntent);
        }
    }
}
