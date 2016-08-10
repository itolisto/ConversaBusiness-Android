package ee.app.conversabusiness.model.Parse;

import android.util.Log;

import com.parse.FunctionCallback;
import com.parse.ParseClassName;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ee.app.conversabusiness.ConversaApp;
import ee.app.conversabusiness.SendBirdManager;

/**
 * Created by edgargomez on 4/15/16.
 */

@ParseClassName("_User")
public class Account extends ParseUser {

    public static void getBusinessId() {
        HashMap<String, String> params = new HashMap<>();
        try {
            String result = ParseCloud.callFunction("getBusinessId", params);
            // 1. Save Business object id
            ConversaApp.getPreferences().setBusinessId(result, true);
            // 2. Subscribe to Customer channels
            List<String> channels = new ArrayList<>();
            channels.add(result + "_pvt");
            channels.add(result + "_pbc");
            SendBirdManager.getInstance().joinChannels(channels);
            Log.e("Utils getBusinessId: ", "BUSINESS_OBJECTID: " + result);
        } catch (ParseException e) {
            Log.e("Utils getBusinessId: ", "BUSINESS_OBJECTID error: " + e.getMessage());
        }
    }

    public static void getBusinessIdAsync() {
        HashMap<String, String> params = new HashMap<>();
        ParseCloud.callFunctionInBackground("getBusinessId", params, new FunctionCallback<String>() {
            @Override
            public void done(String result, ParseException e) {
                Log.e("Utils getBusinessId: ", "BUSINESS_OBJECTID: " + result);
                if(e == null) {
                    // 1. Save Customer object id
                    ConversaApp.getPreferences().setBusinessId(result, true);
                    // 2. Subscribe to Customer channels
                    List<String> channels = new ArrayList<>();
                    channels.add(result + "_pvt");
                    channels.add(result + "_pbc");
                    SendBirdManager.getInstance().joinChannels(channels);
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
