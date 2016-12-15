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

package ee.app.conversabusiness.model.database;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import ee.app.conversabusiness.messaging.MessageIntentService;
import ee.app.conversabusiness.actions.MessageAction;

/**
 * dbMessage
 * 
 * Model class for messages.
 */
public class dbMessage implements Parcelable {

	private long mId;
	private String mFromUserId;
	private String mToUserId;
	private String mMessageType;
	private String mDeliveryStatus;
	private String mBody;
	private String mLocalUrl;
	private String mRemoteUrl;
	private float mLongitude;
	private float mLatitude;
	private long mCreated;
	private long mViewAt;
	private long mReadAt;
	private String mMessageId;
	private int mWidth;
	private int mHeight;
	private int mDuration;
	private long mBytes;
	private int mProgress;

	public dbMessage() {
		this.mId = -1;
		this.mFromUserId = null;
		this.mToUserId = null;
		this.mMessageType = null;
		this.mDeliveryStatus = null;
		this.mBody = null;
		this.mLocalUrl = null;
		this.mRemoteUrl = null;
		this.mLongitude = 0;
		this.mLatitude = 0;
		this.mCreated = System.currentTimeMillis();
		this.mViewAt = 0;
		this.mReadAt = 0;
		this.mMessageId = null;
		this.mWidth = 0;
		this.mHeight = 0;
		this.mDuration = 0;
		this.mBytes = 0;
		this.mProgress = 0;
	}

	public long getId() { return mId; }
	public String getFromUserId() { return mFromUserId; }
	public String getToUserId() { return mToUserId; }
	public String getMessageType() { return mMessageType; }
	public String getDeliveryStatus() { return mDeliveryStatus; }
	public String getBody() { return mBody; }
	public String getLocalUrl() { return mLocalUrl; }
	public String getRemoteUrl() { return mRemoteUrl; }
	public float getLongitude() { return mLongitude; }
	public float getLatitude() { return mLatitude; }
	public long getCreated() { return mCreated; }
	public long getViewAt() { return mViewAt; }
	public long getReadAt() { return mReadAt; }
	public String getMessageId() { return  mMessageId; }
	public int getWidth() { return mWidth; }
	public int getHeight() { return mHeight; }
	public int getDuration() { return mDuration; }
	public long getBytes() { return mBytes; }
	public int getProgress() { return mProgress; }

	public void setId(long id) { this.mId = id; }
	public void setFromUserId(String fromUserId) { this.mFromUserId = fromUserId; }
	public void setToUserId(String toUserId) { this.mToUserId = toUserId; }
	public void setMessageType(String type) { this.mMessageType = type; }
	public void setDeliveryStatus(String status) { this.mDeliveryStatus = status; }
	public void setBody(String body) { this.mBody = body; }
	public void setLocalUrl(String mFileId) { this.mLocalUrl = mFileId; }
	public void setRemoteUrl(String mRemoteUrl) { this.mRemoteUrl = mRemoteUrl; }
	public void setLongitude(float longitude) { this.mLongitude = longitude; }
	public void setLatitude(float latitude) { this.mLatitude = latitude; }
	public void setCreated(long created) { this.mCreated = created; }
	public void setViewAt(long mViewAt) { this.mViewAt = mViewAt; }
	public void setReadAt(long mReadAt) { this.mReadAt = mReadAt; }
	public void setMessageId(String mMessageId) { this.mMessageId = mMessageId; }
	public void setWidth(int mWidth) { this.mWidth = mWidth; }
	public void setHeight(int mHeight) { this.mHeight = mHeight; }
	public void setDuration(int mDuration) { this.mDuration = mDuration; }
	public void setBytes(long mBytes) { this.mBytes = mBytes; }
	public void setProgress(int mProgress) { this.mProgress = mProgress; }

	/* ******************************************************************************************* */
	/* ******************************************************************************************* */
	public void updateMessageStatus(Context context, String status) {
		if (context == null) {
			return;
		}

		Intent broadcastIntent = new Intent(context, MessageIntentService.class);
		broadcastIntent.putExtra(MessageIntentService.INTENT_EXTRA_MESSAGE, this);
		broadcastIntent.putExtra(MessageIntentService.INTENT_EXTRA_ACTION_CODE, MessageAction.ACTION_MESSAGE_UPDATE_STATUS);
		broadcastIntent.putExtra(MessageIntentService.INTENT_EXTRA_UPDATE_STATUS, status);
		context.startService(broadcastIntent);
	}

	public static void getAllMessageForChat(Context context, String businessId, int count, int skip) {
		if (context == null) {
			return;
		}

		Intent broadcastIntent = new Intent(context, MessageIntentService.class);
		broadcastIntent.putExtra(MessageIntentService.INTENT_EXTRA_ACTION_CODE, MessageAction.ACTION_MESSAGE_RETRIEVE_ALL);
		broadcastIntent.putExtra(MessageIntentService.INTENT_EXTRA_CONTACT_ID, businessId);
		broadcastIntent.putExtra(MessageIntentService.INTENT_EXTRA_MESSAGE_COUNT, count);
		broadcastIntent.putExtra(MessageIntentService.INTENT_EXTRA_MESSAGE_SKIP, skip);
		context.startService(broadcastIntent);
	}

	public static void updateViewMessages(Context context, String businessId) {
		if (context == null) {
			return;
		}

		Intent broadcastIntent = new Intent(context, MessageIntentService.class);
		broadcastIntent.putExtra(MessageIntentService.INTENT_EXTRA_ACTION_CODE, MessageAction.ACTION_MESSAGE_UPDATE_VIEW);
		broadcastIntent.putExtra(MessageIntentService.INTENT_EXTRA_CONTACT_ID, businessId);
		context.startService(broadcastIntent);
	}

	/* ******************************************************************************************* */
	/* ******************************************************************************************* */
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.mId);
		dest.writeString(this.mFromUserId);
		dest.writeString(this.mToUserId);
		dest.writeString(this.mMessageType);
		dest.writeString(this.mDeliveryStatus);
		dest.writeString(this.mBody);
		dest.writeString(this.mLocalUrl);
		dest.writeString(this.mRemoteUrl);
		dest.writeFloat(this.mLongitude);
		dest.writeFloat(this.mLatitude);
		dest.writeLong(this.mCreated);
		dest.writeLong(this.mViewAt);
		dest.writeLong(this.mReadAt);
		dest.writeString(this.mMessageId);
		dest.writeInt(this.mWidth);
		dest.writeInt(this.mHeight);
		dest.writeInt(this.mDuration);
		dest.writeLong(this.mBytes);
		dest.writeInt(this.mProgress);
	}

	protected dbMessage(Parcel in) {
		this.mId = in.readLong();
		this.mFromUserId = in.readString();
		this.mToUserId = in.readString();
		this.mMessageType = in.readString();
		this.mDeliveryStatus = in.readString();
		this.mBody = in.readString();
		this.mLocalUrl = in.readString();
		this.mRemoteUrl = in.readString();
		this.mLongitude = in.readFloat();
		this.mLatitude = in.readFloat();
		this.mCreated = in.readLong();
		this.mViewAt = in.readLong();
		this.mReadAt = in.readLong();
		this.mMessageId = in.readString();
		this.mWidth = in.readInt();
		this.mHeight = in.readInt();
		this.mDuration = in.readInt();
		this.mBytes = in.readLong();
		this.mProgress = in.readInt();
	}

	public static final Parcelable.Creator<dbMessage> CREATOR = new Parcelable.Creator<dbMessage>() {
		@Override
		public dbMessage createFromParcel(Parcel source) {
			return new dbMessage(source);
		}

		@Override
		public dbMessage[] newArray(int size) {
			return new dbMessage[size];
		}
	};
}