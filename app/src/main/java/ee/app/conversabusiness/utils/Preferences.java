/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ee.app.conversabusiness.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Preferences
 * 
 * Holds and managed application's preferences.
 */

public class Preferences {

    // Defining SharedPreferences entries
    private static final String BUSINESS_ID = "business_id";
    private static final String FIRST_USER_SERVER_CALL = "first_users_server_call";
    private static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";

    private SharedPreferences sharedPreferences;

    /**
     * Gets a SharedPreferences instance that points to the default file that is
     * used by the preference framework in the given context.
     */
    public Preferences(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /* ******************************************************************************** */
	/* ************************************ GETTERS *********************************** */
	/* ******************************************************************************** */
    public String getBusinessId() {
        return sharedPreferences.getString(BUSINESS_ID, "");
    }

    public boolean isFirstCallForUsers() {
        return sharedPreferences.getBoolean(FIRST_USER_SERVER_CALL, true);
    }

    public boolean getRegistrationToServer() {
        return !(sharedPreferences.getString(SENT_TOKEN_TO_SERVER, "").isEmpty());
    }

    public String getRegistrationToken() {
        return sharedPreferences.getString(SENT_TOKEN_TO_SERVER, "");
    }


    public boolean cleanSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        return editor.commit();
    }
    /* ******************************************************************************** */
	/* ************************************ SETTERS *********************************** */
	/* ******************************************************************************** */
    public void setBusinessId(String id, boolean onBackground) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(BUSINESS_ID, id);
        if (onBackground) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public void setIsFirstCallForUsers(boolean isFirst) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FIRST_USER_SERVER_CALL, isFirst);
        editor.apply();
    }

    public void setRegistrationToServer(String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SENT_TOKEN_TO_SERVER, value);
        editor.apply();
    }

}