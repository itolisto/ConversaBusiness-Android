package ee.app.conversabusiness.model.parse;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.onesignal.OneSignal;
import com.parse.FunctionCallback;
import com.parse.ParseClassName;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import ee.app.conversabusiness.ConversaApp;
import ee.app.conversabusiness.management.AblyConnection;
import ee.app.conversabusiness.utils.Utils;

/**
 * Created by edgargomez on 4/15/16.
 */

@ParseClassName("_User")
public class Account extends ParseUser {

    public static void getBusinessId(final WeakReference<AppCompatActivity> activity) {
        HashMap<String, String> params = new HashMap<>();
        ParseCloud.callFunctionInBackground("getBusinessId", params, new FunctionCallback<String>() {
            @Override
            public void done(String result, ParseException e) {
                Log.e("Utils getBusinessId: ", "BUSINESS_OBJECTID: " + result);
                if(e == null) {
                    // 1. Save Customer object id
                    if (activity.get() != null) {
                        ConversaApp.getInstance(activity.get()).getPreferences().setBusinessId(result, false);
                    }
                    // 2. Subscribe to Customer channels
                    AblyConnection.getInstance().subscribeToChannels();
                    // 3. Subscribe to Customer channels
                    OneSignal.setSubscription(true);
                    Utils.subscribeToTags(result);
                }
            }
        });
    }

    public void setEmail(String email) {
        put("email", email);
    }

    public void setPassword(String password) {
        put("password", password);
    }

}
