package ee.app.conversamanager.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ee.app.conversamanager.ConversaApp;

/**
 * Created by edgargomez on 8/10/16.
 */
public class NotificationDeleted extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        if (intent.getExtras() == null) {
            return;
        }

        long notificationId = intent.getExtras().getLong("notificationId", -1);
        if (notificationId != -1) {
            ConversaApp.getInstance(context).getDB().resetGroupCount(notificationId);
        }
    }

}