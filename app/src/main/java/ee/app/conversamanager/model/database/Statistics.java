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

package ee.app.conversamanager.model.database;

import android.support.v7.app.AppCompatActivity;

import ee.app.conversamanager.R;

/**
 * User
 * 
 * Model class for Statistics.
 */

public class Statistics {

    //USED BY USER
    private String mMaxDevicesCount;
    private String mTitle;
    private String mCategoryAvatar;
    private String mNumberContacts;
    private String mNumberOfFavs;
    private String mBlockContacts;
    private String mKeywords;
    private String mReplies;
    private String mLocations;
    private String mTotalMessagesSent;
    private String mTotalMessagesReceived;
    private String mTotalMessagesSentByPushId;
    private String mTotalMessagesReceivedByPushId;
    private String mRemaining;
    private String mExpiration;
    private String mLastGet;

    private final int NUMBER_OF_FIELDS = 14;

	public Statistics(String mMaxDevicesCount, String mTitle, String mCategoryAvatar,
                      String mNumberContacts, String mNumberOfFavs, String mBlockContacts,
                      String mKeywords, String mReplies, String mLocations, String mRemaining,
                      String mExpiration, String mTotalMessagesSent, String mTotalMessagesReceived,
                      String mTotalMessagesSentByPushId, String mTotalMessagesReceivedByPushId, String mLastGet) {
		super();
        this.mMaxDevicesCount       = mMaxDevicesCount;
        this.mTitle                 = mTitle;
		this.mCategoryAvatar        = mCategoryAvatar;
        this.mNumberContacts        = mNumberContacts;
        this.mNumberOfFavs          = mNumberOfFavs;
        this.mBlockContacts         = mBlockContacts;
        this.mKeywords              = mKeywords;
        this.mReplies               = mReplies;
        this.mLocations             = mLocations;
        this.mRemaining             = mRemaining;
        this.mExpiration            = mExpiration;
        this.mLastGet               = mLastGet;
        this.mTotalMessagesSent     = mTotalMessagesSent;
        this.mTotalMessagesReceived = mTotalMessagesReceived;
        this.mTotalMessagesSentByPushId     = mTotalMessagesSentByPushId;
        this.mTotalMessagesReceivedByPushId = mTotalMessagesReceivedByPushId;
	}

    public Statistics(){}
	/* ******************************************************************************** */
	/* ************************************ GETTERS *********************************** */
	/* ******************************************************************************** */
	public String getMaxDevices()            { return mMaxDevicesCount; }
    public String getCategoryTitle()         { return mTitle; }
    public String getCategoryAvatar()        { return mCategoryAvatar; }
    public String getNumberOfContacts()      { return mNumberContacts; }
    public String getNumberOfFavs()          { return mNumberOfFavs; }
    public String getNumberOfBlockedUsers()  { return mBlockContacts; }
    public String getNumberOfKeywords()      { return mKeywords; }
    public String getNumberOfReplies()       { return mReplies; }
    public String getNumberOfLocations()     { return mLocations; }
    public String getRemainingDiffusion()    { return mRemaining; }
    public String getLastget()               { return mLastGet; }
    public String getExpirationDate()        { return mExpiration; }
    public String getTotalSentMessages()       { return mTotalMessagesSent; }
    public String getTotalReceivedMessages()   { return mTotalMessagesReceived; }
    public String getTotalSentMessagesByPushId()     { return mTotalMessagesSent; }
    public String getTotalReceivedMessagesByPushId() { return mTotalMessagesReceived; }

    public int getTotalOfStatistics(){ return NUMBER_OF_FIELDS; }
	/* ******************************************************************************** */
	/* ************************************ SETTERS *********************************** */
	/* ******************************************************************************** */
    public void setMaxDevices(String mMaxDevicesCount)        { this.mMaxDevicesCount = mMaxDevicesCount; }
	public void setCategoryTitle(String mTitle)               { this.mTitle = mTitle; }
	public void setCategoryAvatar(String mCategoryAvatar)     { this.mCategoryAvatar = mCategoryAvatar; }
    public void setNumberOfContacts(String mNumberContacts)   { this.mNumberContacts = mNumberContacts; }
    public void setNumberOfFavs(String mNumberOfFavs)         { this.mNumberOfFavs = mNumberOfFavs; }
    public void setNumberOfKeywords(String mKeywords)         { this.mKeywords = mKeywords; }
    public void setNumberOfReplies(String mReplies)           { this.mReplies = mReplies; }
    public void setNumberOfLocations(String mLocations)       { this.mLocations = mLocations; }
    public void setRemainingDiffusion(String mRemaining)      { this.mRemaining = mRemaining; }
    public void setLastget(String mLastGet)                   { this.mLastGet = mLastGet; }
    public void setExpirationDate(String mExpiration)         { this.mExpiration = mExpiration; }
    public void setNumberOfBlockedUsers(String mBlockContacts)  { this.mBlockContacts = mBlockContacts; }
    public void setTotalSentMessages(String mTotalMessagesSent) { this.mTotalMessagesSent = mTotalMessagesSent; }
    public void setTotalSentMessagesByPushId(String mTotalMessagesSent) { this.mTotalMessagesSentByPushId = mTotalMessagesSent; }
    public void setTotalReceivedMessages(String mTotalMessagesReceived) { this.mTotalMessagesReceived = mTotalMessagesReceived; }
    public void setTotalReceivedMessagesByPushId(String mTotalMessagesReceived) { this.mTotalMessagesReceivedByPushId = mTotalMessagesReceived; }
    /* ******************************************************************************** */
	/* ******************************************************************************** */
    public String getField(int position) {
        String field;
        
        switch(position) {
            case 0:
                field =  mMaxDevicesCount;
                break;
            case 1:
                field =  mCategoryAvatar + ";" + mTitle;
                break;
            case 2:
                field =  mNumberContacts;
                break;
            case 3:
                field =  mNumberOfFavs;
                break;
            case 4:
                field =  mKeywords;
                break;
            case 5:
                field =  mReplies;
                break;
            case 6:
                field =  mLocations;
                break;
            case 7:
                field =  mRemaining;
                break;
            case 8:
                field =  mLastGet;
                break;
            case 9:
                field =  mExpiration;
                break;
            case 10:
                field =  mBlockContacts;
                break;
            case 11:
                field =  mTotalMessagesSent;
                break;
            case 12:
                field =  mTotalMessagesSentByPushId;
                break;
            case 13:
                field =  mTotalMessagesReceived;
                break;
            case 14:
                field =  mTotalMessagesReceivedByPushId;
                break;
            default:
                field =  "null";
                break;
        }
        
        return field;
    }

    public String getTitle(int position, AppCompatActivity activity) {
        String field;

        switch(position) {
            case 0:
                field =  activity.getString(R.string.s0);
                break;
            case 1:
                field =  activity.getString(R.string.s1);
                break;
            case 2:
                field =  activity.getString(R.string.s2);
                break;
            case 3:
                field =  activity.getString(R.string.s3);
                break;
            case 4:
                field =  activity.getString(R.string.s4);
                break;
            case 5:
                field =  activity.getString(R.string.s5);
                break;
            case 6:
                field =  activity.getString(R.string.s6);
                break;
            case 7:
                field =  activity.getString(R.string.s7);
                break;
            case 8:
                field =  activity.getString(R.string.s8);
                break;
            case 9:
                field =  activity.getString(R.string.s9);
                break;
            case 10:
                field =  activity.getString(R.string.s10);
                break;
            case 11:
                field =  activity.getString(R.string.s11);
                break;
            case 12:
                field =  activity.getString(R.string.s12);
                break;
            case 13:
                field =  activity.getString(R.string.s13);
                break;
            case 14:
                field =  activity.getString(R.string.s14);
                break;
            default:
                field =  "null";
                break;
        }

        return field;
    }
}
