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

package ee.app.conversabusiness.model.Database;

import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversabusiness.ConversaApp;
import ee.app.conversabusiness.response.ContactResponse;

/**
 * Emoticon
 * 
 * Model class for business.
 */

public class dCustomer implements Parcelable {

    private long mId;
    private String mBusinessId;
    private String mDisplayName;
    private String mAbout;
    private String mStatusMessage;
    private String mComposingMessageString;
    private String mAvatarThumbFileId;
    private boolean mBlocked;
    private boolean mMuted;
    private long mRecent;
    private long mCreated;

    // MESSAGE ACTIONS
    public static final int ACTION_MESSAGE_SAVE = 1;
    public static final int ACTION_MESSAGE_UPDATE = 2;
    public static final int ACTION_MESSAGE_DELETE = 3;
    public static final int ACTION_MESSAGE_RETRIEVE_ALL = 4;

    public dCustomer() {
        this.mId = -1;
        this.mComposingMessageString = "";
        this.mBlocked = false;
        this.mMuted = false;
        this.mCreated = System.currentTimeMillis();
        this.mRecent = this.mCreated;
    }

    public long getId() { return mId; }
    public String getBusinessId() { return mBusinessId; }
    public String getDisplayName() { return mDisplayName; }
    public String getAbout() { return mAbout; }
    public String getStatusMessage() { return mStatusMessage; }
    public String getComposingMessage() { return mComposingMessageString; }
    public String getAvatarThumbFileId() { return mAvatarThumbFileId; }
    public boolean isBlocked() { return mBlocked; }
    public boolean isMuted() { return mMuted; }
    public long getRecent() { return mRecent; }
    public long getCreated() { return mCreated; }

    public void setId(long mId) { this.mId = mId; }
    public void setBusinessId(String mBusinessId) { this.mBusinessId = mBusinessId; }
    public void setDisplayName(String mDisplayName) { this.mDisplayName = mDisplayName; }
    public void setAbout(String about) { this.mAbout = about; }
    public void setStatusMessage(String mStatusMessage) { this.mStatusMessage = mStatusMessage; }
    public void setComposingMessage(String mComposingMessageString) { this.mComposingMessageString = mComposingMessageString; }
    public void setAvatarThumbFileId(String mAvatarThumbFileId) { this.mAvatarThumbFileId = mAvatarThumbFileId; }
    public void setBlocked(boolean mBlocked) { this.mBlocked = mBlocked; }
    public void setMuted(boolean mMuted) { this.mMuted = mMuted; }
    public void setRecent(long mRecent) { this.mRecent = mRecent; }
    public void setCreated(long mCreated) { this.mCreated = mCreated; }

    /* ******************************************************************************************* */
    /* ******************************************************************************************* */

    public void saveToLocalDatabase() {
        ContactAsyncTaskRunner runner = new ContactAsyncTaskRunner();
        runner.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ACTION_MESSAGE_SAVE, this);
    }

    public static void getAllContacts() {
        ContactAsyncTaskRunner runner = new ContactAsyncTaskRunner();
        runner.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ACTION_MESSAGE_RETRIEVE_ALL);
    }

    private static class ContactAsyncTaskRunner extends AsyncTask<Object, String, ContactResponse> {

        public ContactAsyncTaskRunner() { }

        @Override
        protected ContactResponse doInBackground(Object... params) {
            if (params.length == 0)
                return new ContactResponse(-1);

            int actionCode = (int)params[0];
            dCustomer user = new dCustomer();
            List<dCustomer> users = new ArrayList<>();

            try {
                Log.e("ContactAsyncTaskRunner", "INTENTANDO GUARDAR/ACTUALIZAR/ELIMINAR USUARIO...");

                switch (actionCode) {
                    case ACTION_MESSAGE_SAVE:
                        user = ConversaApp.getDB().saveContact((dCustomer) params[1]);
                        break;
                    case ACTION_MESSAGE_UPDATE:
                        break;
                    case ACTION_MESSAGE_DELETE:
                        break;
                    case ACTION_MESSAGE_RETRIEVE_ALL:
                        users = ConversaApp.getDB().getAllContacts();
                        break;
                }

            } catch (SQLException e) {
                Log.e("ContactAsyncTaskRunner", "No se pudo guardar usuario porque ocurrio el siguiente error: " + e.getMessage());
            }

            return new ContactResponse(actionCode, user, users);
        }

        @Override
        protected void onPostExecute(ContactResponse contactResponse) {
            ConversaApp.getDB().notifyContactListeners(contactResponse);
        }
    }

    /* ******************************************************************************************* */
    /* ******************************************************************************************* */

    // In the vast majority of cases you can simply return 0 for this.
    // There are cases where you need to use the constant `CONTENTS_FILE_DESCRIPTOR`
    // But this is out of scope of this tutorial
    @Override
    public int describeContents() {
        return 0;
    }

    // This is where you write the values you want to save to the `Parcel`.
    // The `Parcel` class has methods defined to help you save all of your values.
    // Note that there are only methods defined for simple values, lists, and other Parcelable objects.
    // You may need to make several classes Parcelable to send the data you want.
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mId);
        dest.writeString(this.mBusinessId);
        dest.writeString(this.mDisplayName);
        dest.writeString(this.mAbout);
        dest.writeString(this.mStatusMessage);
        dest.writeString(this.mComposingMessageString);
        dest.writeString(this.mAvatarThumbFileId);
        dest.writeByte(this.mBlocked ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mMuted ? (byte) 1 : (byte) 0);
        dest.writeLong(this.mRecent);
        dest.writeLong(this.mCreated);
    }

    // Using the `in` variable, we can retrieve the values that
    // we originally wrote into the `Parcel`.  This constructor is usually
    // private so that only the `CREATOR` field can access.
    protected dCustomer(Parcel in) {
        this.mId = in.readLong();
        this.mBusinessId = in.readString();
        this.mDisplayName = in.readString();
        this.mAbout = in.readString();
        this.mStatusMessage = in.readString();
        this.mComposingMessageString = in.readString();
        this.mAvatarThumbFileId = in.readString();
        this.mBlocked = in.readByte() != 0;
        this.mMuted = in.readByte() != 0;
        this.mRecent = in.readLong();
        this.mCreated = in.readLong();
    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<MyParcelable> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<dCustomer> CREATOR = new Parcelable.Creator<dCustomer>() {
        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public dCustomer createFromParcel(Parcel source) {
            return new dCustomer(source);
        }
        // We just need to copy this and change the type to match our class.
        @Override
        public dCustomer[] newArray(int size) {
            return new dCustomer[size];
        }
    };
}
