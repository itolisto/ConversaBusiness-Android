package ee.app.conversamanager.notifications;

import android.content.Intent;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;
import ee.app.conversamanager.ActivityChatWall;
import ee.app.conversamanager.ActivityMain;
import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.model.database.dbCustomer;
import ee.app.conversamanager.utils.Const;

/**
 * Created by edgargomez on 5/5/15.
 */
public class NotificationPressed extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openFromNotification(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        openFromNotification(intent);
    }

    private void openFromNotification(Intent intent) {
        if (intent == null) {
            return;
        }

        if (intent.getExtras() == null) {
            return;
        }

        long notificationId = intent.getExtras().getLong("notificationId", -1);
        if (notificationId != -1) {
            ConversaApp.getInstance(this).getDB().resetGroupCount(notificationId);
        }

        JSONObject additionalData;

        try {
            additionalData = new JSONObject(intent.getStringExtra("data"));
        } catch (JSONException e) {
            return;
        }

        switch (additionalData.optInt("appAction", 0)) {
            case 1:
                String contactId = additionalData.optString("contactId", null);

                if (contactId == null) {
                    break;
                }

                int count = intent.getExtras().getInt("count", 1);

                dbCustomer user = ConversaApp.getInstance(this).getDB().isContact(contactId);
                if (user != null) {
                    // Set extras
                    intent = new Intent(this, ActivityChatWall.class);
                    intent.putExtra(Const.iExtraCustomer, user);
                    intent.putExtra(Const.iExtraAddBusiness, false);
                    intent.putExtra(Const.kAppVersionKey, count);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                break;
            default:
                intent = new Intent(this, ActivityMain.class);
                break;
        }

        startActivity(intent);
    }

}