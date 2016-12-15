package ee.app.conversabusiness.dialog;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import ee.app.conversabusiness.ActivityMain;
import ee.app.conversabusiness.ConversaApp;
import ee.app.conversabusiness.R;
import ee.app.conversabusiness.model.database.NotificationInformation;
import ee.app.conversabusiness.model.database.dbMessage;
import ee.app.conversabusiness.notifications.NotificationDeleted;
import ee.app.conversabusiness.notifications.NotificationPressed;
import ee.app.conversabusiness.utils.Const;

/**
 * Created by edgargomez on 8/22/16.
 */
public class PushNotification {

    public static void showMessageNotification(Context context, String from, String jsonData, dbMessage message, NotificationInformation summary) {
        // Press intent
        Intent intent = new Intent(context, ActivityMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // The stack builder object will contain an artificial back stack for `the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ActivityMain.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);

        Intent intentChatWallView = new Intent (context, NotificationPressed.class);
        intentChatWallView.putExtra("notificationId", summary.getNotificationId());
        intentChatWallView.putExtra("data", jsonData);
        intentChatWallView.putExtra("count", summary.getCount());
        stackBuilder.addNextIntent(intentChatWallView);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Delete intent
        Intent delIntent = new Intent(context, NotificationDeleted.class);
        delIntent.putExtra("notificationId", summary.getNotificationId());
        PendingIntent delPendingIntent = PendingIntent.getBroadcast(context, 0 /* Request code */, delIntent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification)
                .setGroup(summary.getGroupId())
                .setContentTitle(from)
                .setContentText(getMessage(context, message))
                .setAutoCancel(true)
                .setDeleteIntent(delPendingIntent)
                .setContentIntent(pendingIntent);

        if (ConversaApp.getInstance(context).getPreferences().getPushNotificationSound()) {
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notificationBuilder.setSound(defaultSoundUri);
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (summary.getCount() > 1) {
            notificationBuilder.setGroupSummary(true).setContentText("You have " + summary.getCount() + " new messages from " + from);
        }

        Log.e("PushNotification", "Notification displayed with id: " + summary.getAndroidNotificationId());
        notificationManager.notify(summary.getAndroidNotificationId(), notificationBuilder.build());
    }

    private static String getMessage(Context context, dbMessage message) {
        switch (message.getMessageType()) {
            case Const.kMessageTypeText:
                return message.getBody();
            case Const.kMessageTypeAudio:
                return context.getString(R.string.contacts_last_message_audio);
            case Const.kMessageTypeVideo:
                return context.getString(R.string.contacts_last_message_video);
            case Const.kMessageTypeImage:
                return context.getString(R.string.contacts_last_message_image);
            case Const.kMessageTypeLocation:
                return context.getString(R.string.contacts_last_message_location);
            default:
                return context.getString(R.string.contacts_last_message_default);
        }
    }

}
