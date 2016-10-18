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
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ee.app.conversabusiness.R;
import ee.app.conversabusiness.settings.PreferencesKeys;

/**
 * Preferences
 * 
 * Holds and managed application's preferences.
 */
public class Preferences {

    private final Context context;
    private SharedPreferences sharedPreferences;

    // Defining SharedPreferences entries
    private static final String BUSINESS_ID = "business_id";

    /**
     * Gets a SharedPreferences instance that points to the default file that is
     * used by the preference framework in the given context.
     */
    public Preferences(Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void cleanSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    /* ******************************************************************************** */
	/* ************************************ GETTERS *********************************** */
	/* ******************************************************************************** */
    public String getDisplayName() {
        return getStringPreference(PreferencesKeys.PREFERENCE_MAIN_LANGUAGE_KEY, "es");
    }

    public String getConversaId() {
        return getStringPreference(PreferencesKeys.PREFERENCE_MAIN_LANGUAGE_KEY, "es");
    }

    public String getAvatarUrl() {
        return getStringPreference(PreferencesKeys.PREFERENCE_MAIN_LANGUAGE_KEY, "es");
    }

    public String getBusinessId() {
        return getStringPreference(BUSINESS_ID, "");
    }

    public String getLanguage() {
        return getStringPreference(PreferencesKeys.PREFERENCE_MAIN_LANGUAGE_KEY, "es");
    }

    public String getLanguageName() {
        String language = getStringPreference(PreferencesKeys.PREFERENCE_MAIN_LANGUAGE_KEY, "es");
        if (language.equals("zz")) {
            return context.getResources().getStringArray(R.array.language_entries)[0];
        } else if (language.equals("en")) {
            return context.getResources().getStringArray(R.array.language_entries)[1];
        } else {
            return context.getResources().getStringArray(R.array.language_entries)[2];
        }
    }

    public int getLanguagePosition() {
        String language = getStringPreference(PreferencesKeys.PREFERENCE_MAIN_LANGUAGE_KEY, "es");
        if (language.equals("zz")) {
            return 0;
        } else if (language.equals("en")) {
            return 1;
        } else {
            return 2;
        }
    }

    public int getUploadQualityPosition() {
        int position = getIntegerPreference(PreferencesKeys.CHAT_QUALITY_KEY, -1);
        if (position == -1) {
            return 1;
        } else {
            return position;
        }
    }

    public String getUploadQuality() {
        int position = getIntegerPreference(PreferencesKeys.CHAT_QUALITY_KEY, -1);
        if (position == -1) {
            return context.getResources().getStringArray(R.array.sett_chat_quality_entries)[1];
        } else {
            return context.getResources().getStringArray(R.array.sett_chat_quality_entries)[position];
        }
    }

    public String getUploadQualityFromNewValue(int position) {
        if (position < 0 || position > context.getResources().getStringArray(R.array.sett_chat_quality_entries).length) {
            return context.getResources().getStringArray(R.array.sett_chat_quality_entries)[1];
        } else {
            return context.getResources().getStringArray(R.array.sett_chat_quality_entries)[position];
        }
    }

    public boolean getDownloadAutomatically() {
        return getBooleanPreference(PreferencesKeys.CHAT_DOWNLOAD_KEY, true);
    }

    public boolean getPlaySoundWhenSending() {
        return getBooleanPreference(PreferencesKeys.CHAT_SOUND_SENDING_KEY, true);
    }

    public boolean getPlaySoundWhenReceiving() {
        return getBooleanPreference(PreferencesKeys.CHAT_SOUND_RECEIVING_KEY, true);
    }

    public boolean getPushNotificationSound() {
        return getBooleanPreference(PreferencesKeys.NOTIFICATION_SOUND_KEY, true);
    }

    public boolean getPushNotificationPreview() {
        return getBooleanPreference(PreferencesKeys.NOTIFICATION_PREVIEW_KEY, true);
    }

    public boolean getInAppNotificationSound() {
        return getBooleanPreference(PreferencesKeys.NOTIFICATION_INAPP_SOUND_KEY, true);
    }

    public boolean getInAppNotificationPreview() {
        return getBooleanPreference(PreferencesKeys.NOTIFICATION_INAPP_PREVIEW_KEY, true);
    }

    private @NonNull
    Set<String> getMediaDownloadAllowed(Context context, String key, @ArrayRes int defaultValuesRes) {
        return getStringSetPreference(
                key,
                new HashSet<>(Arrays.asList(context.getResources().getStringArray(defaultValuesRes))));
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
    
    public void setLanguage(String language) {
        setStringPreference(PreferencesKeys.PREFERENCE_MAIN_LANGUAGE_KEY, language);
    }

    public void setUploadQuality(int position) {
        if (position >= 0 || position < context.getResources().getStringArray(R.array.sett_chat_quality_entries).length) {
            setIntegerPrefrence(PreferencesKeys.CHAT_QUALITY_KEY, position);
        }
    }

    public void setDownloadAutomatically(boolean value) {
        setBooleanPreference(PreferencesKeys.CHAT_DOWNLOAD_KEY, value);
    }

    public void setPlaySoundWhenSending(boolean value) {
        setBooleanPreference(PreferencesKeys.CHAT_SOUND_SENDING_KEY, value);
    }

    public void setPlaySoundWhenReceiving(boolean value) {
        setBooleanPreference(PreferencesKeys.CHAT_SOUND_RECEIVING_KEY, value);
    }

    public void setPushNotificationSound(boolean value) {
        setBooleanPreference(PreferencesKeys.NOTIFICATION_SOUND_KEY, value);
    }

    public void setPushNotificationPreview(boolean value) {
        setBooleanPreference(PreferencesKeys.NOTIFICATION_PREVIEW_KEY, value);
    }

    public void setInAppNotificationSound(boolean value) {
        setBooleanPreference(PreferencesKeys.NOTIFICATION_INAPP_SOUND_KEY, value);
    }

    public void setInAppNotificationPreview(boolean value) {
        setBooleanPreference(PreferencesKeys.NOTIFICATION_INAPP_PREVIEW_KEY, value);
    }

    /* ******************************************************************************** */
	/* ******************************************************************************** */
    private void setBooleanPreference(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    private boolean getBooleanPreference(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    private void setStringPreference(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    private String getStringPreference(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    private int getIntegerPreference(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    private void setIntegerPrefrence(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    private long getLongPreference(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    private void setLongPreference(String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
    }

    private Set<String> getStringSetPreference(String key, Set<String> defaultValues) {
        if (sharedPreferences.contains(key)) {
            return sharedPreferences.getStringSet(key, Collections.<String>emptySet());
        } else {
            return defaultValues;
        }
    }

}