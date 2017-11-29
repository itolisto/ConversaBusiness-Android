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

package ee.app.conversamanager.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Emoticon
 * 
 * Model class for business.
 */

public class nBusiness implements Parcelable {

    private final String mBusinessId;
    private final String mDisplayName;
    private final String mConversaId;
    private final String mAvatarThumbFileId;

    public nBusiness(String mBusinessId, String mDisplayName, String mConversaId, String mAvatarThumbFileId) {
        this.mBusinessId = mBusinessId;
        this.mDisplayName = mDisplayName;
        this.mConversaId = mConversaId;
        this.mAvatarThumbFileId = mAvatarThumbFileId;
    }

    public String getBusinessId() { return mBusinessId; }
    public String getDisplayName() { return mDisplayName; }
    public String getConversaId() { return mConversaId; }
    public String getAvatarThumbFileId() { return mAvatarThumbFileId; }

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
        dest.writeString(this.mBusinessId);
        dest.writeString(this.mDisplayName);
        dest.writeString(this.mConversaId);
        dest.writeString(this.mAvatarThumbFileId);
    }

    // Using the `in` variable, we can retrieve the values that
    // we originally wrote into the `Parcel`.  This constructor is usually
    // private so that only the `CREATOR` field can access.
    protected nBusiness(Parcel in) {
        this.mBusinessId = in.readString();
        this.mDisplayName = in.readString();
        this.mConversaId = in.readString();
        this.mAvatarThumbFileId = in.readString();
    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<MyParcelable> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<nBusiness> CREATOR = new Parcelable.Creator<nBusiness>() {
        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public nBusiness createFromParcel(Parcel source) {
            return new nBusiness(source);
        }
        // We just need to copy this and change the type to match our class.
        @Override
        public nBusiness[] newArray(int size) {
            return new nBusiness[size];
        }
    };

}