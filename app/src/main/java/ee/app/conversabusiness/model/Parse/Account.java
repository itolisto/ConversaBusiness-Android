package ee.app.conversabusiness.model.Parse;

import android.util.Log;

import com.parse.FunctionCallback;
import com.parse.ParseClassName;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;

import ee.app.conversabusiness.ConversaApp;
import ee.app.conversabusiness.utils.Utils;

/**
 * Created by edgargomez on 4/15/16.
 */

@ParseClassName("_User")
public class Account extends ParseUser {

    public static void getBusinessId() {
        HashMap<String, String> params = new HashMap<>();
        ParseCloud.callFunctionInBackground("getBusinessId", params, new FunctionCallback<String>() {
            @Override
            public void done(String result, ParseException e) {
                Log.e("Utils getBusinessId: ", "BUSINESS_OBJECTID: " + result);
                if(e == null) {
                    // 1. Save Customer object id
                    ConversaApp.getPreferences().setBusinessId(result);
                    // 2. Subscribe to Customer channels
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
