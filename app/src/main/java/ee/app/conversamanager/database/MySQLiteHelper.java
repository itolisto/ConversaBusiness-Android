package ee.app.conversamanager.database;

/**
 * Created by edgargomez on 2/11/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.WorkerThread;
import ee.app.conversamanager.ConversaApp;
import ee.app.conversamanager.model.database.NotificationInformation;
import ee.app.conversamanager.model.database.dbCustomer;
import ee.app.conversamanager.model.database.dbMessage;
import ee.app.conversamanager.model.nChatItem;
import ee.app.conversamanager.utils.Const;
import ee.app.conversamanager.utils.Logger;
import ee.app.conversamanager.utils.Utils;

public class MySQLiteHelper {

    private static final String TAG = "MySQLiteHelper";
    private final Context context;
    private DatabaseHelper myDbHelper;

    private static final String DATABASE_NAME1 = "conversabusinessdb.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MESSAGES = "message";
    private static final String TABLE_CV_CONTACTS = "cv_contact";
    private static final String TABLE_NOTIFICATION = "notification";

    private static final String COLUMN_ID = "_id";

    // MESSAGE
    private static final String sMessageFromUserId = "from_user_id";
    private static final String sMessageToUserId = "to_user_id";
    private static final String sMessageType = "message_type";
    private static final String sMessageDeliveryStatus = "delivery_status";
    private static final String sMessageBody = "body";
    private static final String sMessageLocalUrl = "local_url";
    private static final String sMessageRemoteUrl = "remote_url";
    private static final String sMessageLongitude = "longitude";
    private static final String sMessageLatitude = "latitude";
    private static final String sMessageCreatedAt = "created_at";
    private static final String sMessageViewAt = "view_at";
    private static final String sMessageReadAt = "read_at";
    private static final String sMessageMessageId = "message_id";
    private static final String sMessageWidth = "width";
    private static final String sMessageHeight = "height";
    private static final String sMessageDuration = "duration";
    private static final String sMessageBytes = "bytes";
    private static final String sMessageProgress = "progress";

    private static final String TABLE_MESSAGES_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_MESSAGES + "("
            + "\"" + COLUMN_ID + "\" INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "\"" + sMessageFromUserId + "\" CHAR(14) NOT NULL, "
            + "\"" + sMessageToUserId + "\" CHAR(14) NOT NULL, "
            + "\"" + sMessageType + "\" CHAR(1) NOT NULL, "
            + "\"" + sMessageDeliveryStatus + "\" INTEGER NOT NULL, "
            + "\"" + sMessageBody + "\" TEXT, "
            + "\"" + sMessageLocalUrl + "\" TEXT, "
            + "\"" + sMessageRemoteUrl + "\" TEXT, "
            + "\"" + sMessageLongitude + "\" REAL DEFAULT 0, "
            + "\"" + sMessageLatitude + "\" REAL DEFAULT 0, "
            + "\"" + sMessageCreatedAt + "\" INTEGER NOT NULL, "
            + "\"" + sMessageViewAt + "\" INTEGER NOT NULL DEFAULT 0, "
            + "\"" + sMessageReadAt + "\" INTEGER NOT NULL DEFAULT 0, "
            + "\"" + sMessageMessageId + "\" CHAR(20), "
            + "\"" + sMessageWidth + "\" INTEGER DEFAULT 0, "
            + "\"" + sMessageHeight + "\" INTEGER DEFAULT 0, "
            + "\"" + sMessageDuration + "\" INTEGER DEFAULT 0, "
            + "\"" + sMessageBytes + "\" INTEGER DEFAULT 0, "
            + "\"" + sMessageProgress + "\" INTEGER DEFAULT 0);";

    private static final String tmIndex1 = "CREATE INDEX M_search on "  + TABLE_MESSAGES + "(" + sMessageFromUserId + ", " + sMessageToUserId + "); ";
    private static final String tmIndex2 = "CREATE UNIQUE INDEX IF NOT EXISTS C_messageId on "  + TABLE_MESSAGES + "(" + sMessageMessageId + ");";

    // CONTACTS
    private static final String sBusinessCustomerId = "customerId";
    private static final String sBusinessDisplayName = "displayName";
    private static final String sBusinessRecent = "recent";
    private static final String sBusinessComposingMessage = "composingMessageString";
    private static final String sBusinessBlocked = "blocked";
    private static final String sBusinessMuted = "muted";
    private static final String sBusinessCreatedAt = "created_at";
    private static final String sBusinessAvatarFile = "avatar_file_url";

    private static final String TABLE_CONTACTS_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_CV_CONTACTS + "("
            + "\"" + COLUMN_ID + "\" INTEGER PRIMARY KEY, "
            + "\"" + sBusinessCustomerId + "\" CHAR(14) NOT NULL, "
            + "\"" + sBusinessDisplayName + "\" VARCHAR(180) NOT NULL, "
            + "\"" + sBusinessRecent + "\" INTEGER, "
            + "\"" + sBusinessComposingMessage + "\" VARCHAR(255), "
            + "\"" + sBusinessBlocked + "\" CHAR(1) NOT NULL DEFAULT 'N', "
            + "\"" + sBusinessMuted + "\" CHAR(1) NOT NULL DEFAULT 'N', "
            + "\"" + sBusinessCreatedAt + "\" INTEGER NOT NULL, "
            + "\"" + sBusinessAvatarFile + "\" VARCHAR(355) ); ";

    private static final String tcIndex1 = "CREATE UNIQUE INDEX IF NOT EXISTS C_customerId on "  + TABLE_CV_CONTACTS + "(" + sBusinessCustomerId + ");";

    // NOTIFICATIONS
    private static final String sNotificationAndroidId = "android_id";
    private static final String sNotificationGroup = "group_id";
    private static final String sNotificationCount = "count";

    private static final String TABLE_NOTIFICATION_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NOTIFICATION + "("
            + "\"" + COLUMN_ID + "\" INTEGER PRIMARY KEY, "
            + "\"" + sNotificationAndroidId + "\" INTEGER NOT NULL, "
            + "\"" + sNotificationGroup + "\" TEXT NOT NULL, "
            + "\"" + sNotificationCount + "\" INTEGER NOT NULL DEFAULT 0);";

    // TRIGGERS
    private static final String NEW_MESSAGE_TRIGGER = "new_message_trigger";
    private static final String newMessageTrigger = "CREATE TRIGGER IF NOT EXISTS " + NEW_MESSAGE_TRIGGER
            + " AFTER INSERT"
            + " ON " + TABLE_MESSAGES
            + " BEGIN "
            + " update " + TABLE_CV_CONTACTS + " set " + sBusinessRecent + " = new." + sMessageCreatedAt
            + " where " + sBusinessCustomerId + " = new." + sMessageFromUserId
            + " or " + sBusinessCustomerId + " = new." + sMessageToUserId + ";"
            + " END;";

    private static final String DELETE_USER_TRIGGER = "delete_user_trigger";
    private static final String deleteUserTrigger = "CREATE TRIGGER IF NOT EXISTS " + DELETE_USER_TRIGGER
            + " AFTER DELETE"
            + " ON " + TABLE_CV_CONTACTS
            + " FOR EACH ROW"
            + " BEGIN "
            + " delete from " + TABLE_MESSAGES + " where " + sMessageFromUserId + " = old." + sBusinessCustomerId
            + " or " + sMessageToUserId + " = old." + sBusinessCustomerId + ";"
            + " END;";

    /************************************************************/
    /*********************OPEN/CLOSE METHODS*********************/
    /************************************************************/

    public MySQLiteHelper(Context context) {
        this.context = context;
        myDbHelper = new DatabaseHelper(context);
        openDatabase();
    }

    private SQLiteDatabase openDatabase() throws SQLException {
        return myDbHelper.getWritableDatabase();
    }

    public boolean deleteDatabase() {
        deleteAllData();
        return context.deleteDatabase(DATABASE_NAME1);
    }

    public void refreshDbHelper() {
        myDbHelper = new DatabaseHelper(context);
    }

    /************************************************************/
    /*********************OPERATIONS METHODS*********************/
    /************************************************************/
    public dbCustomer saveContact(dbCustomer user) {
        ContentValues contact = new ContentValues();
        contact.put(sBusinessCustomerId, user.getCustomerId());
        contact.put(sBusinessDisplayName, user.getDisplayName());
        contact.put(sBusinessRecent, user.getRecent());
        contact.put(sBusinessComposingMessage, "");
        contact.put(sBusinessBlocked, "N");
        contact.put(sBusinessMuted, "N");
        contact.put(sBusinessCreatedAt, user.getCreated());
        contact.put(sBusinessAvatarFile, user.getAvatarThumbFileId());

        long result = openDatabase().insert(TABLE_CV_CONTACTS, null, contact);

        if (result > 0) {
            user.setId(result);
        }

        return user;
    }

    public List<dbCustomer> getAllContacts() {
        List<dbCustomer> contacts = new ArrayList<>();

        Cursor cursor = openDatabase().query(TABLE_CV_CONTACTS,null,null,null,null,null, sBusinessRecent + " DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            dbCustomer contact = cursorToUser(cursor);
            contacts.add(contact);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return contacts;
    }

    @WorkerThread
    private void deleteAllData() {
        // TODO: if any new folders are added they should be included here
        try {
            File path1 = Utils.getMediaDirectory(context, "images");
            File path2 = Utils.getMediaDirectory(context, "avatars");

            if (path1.exists()) {
                String[] fileNames = path1.list();
                for (String fileName : fileNames) {
                    if (!fileName.equals("lib")) {
                        Utils.deleteFile(new File(path1, fileName));
                    }
                }
            }

            if (path2.exists()) {
                String[] fileNames = path2.list();
                for (String fileName : fileNames) {
                    if (!fileName.equals("lib")) {
                        Utils.deleteFile(new File(path2, fileName));
                    }
                }
            }

            File file = new File(ConversaApp.getInstance(context).getPreferences().getAccountAvatar());
            file.delete();
        } catch (Exception e) {
            Logger.error("deleteAllData", e.getMessage());
        }
    }

    @WorkerThread
    public void deleteAllDataAssociatedWithUser(String ids, int total) {
        String query = "SELECT " + sBusinessCustomerId + " FROM "
                + TABLE_CV_CONTACTS + " WHERE " + COLUMN_ID + " IN (" + ids + ")";

        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();

        List<String> business = new ArrayList<>(total);

        while (!cursor.isAfterLast()) {
            business.add(cursor.getString(0));
            cursor.moveToNext();
        }

        cursor.close();

        // 15 is the min string. It happens when only one user is deleted
        StringBuilder objectIds = new StringBuilder(15);
        for (int i = 0; i < business.size(); i++) {
            objectIds.append("\'");
            objectIds.append(business.get(i));
            objectIds.append("\'");
            if (i + 1 < business.size()) {
                objectIds.append(",");
            }
        }

        deleteAllDataAssociatedToMessagesWithUser(objectIds.toString());
    }

    @WorkerThread
    public void deleteAllDataAssociatedToMessagesWithUser(String objectIds) {
        String query = "SELECT " + sMessageLocalUrl + " FROM " + TABLE_MESSAGES + " WHERE " +
                sMessageType + " NOT IN (\'" + Const.kMessageTypeLocation + "\',\'" +
                Const.kMessageTypeText + "\')" + " AND (" + sMessageFromUserId + " IN (" +
                objectIds + ")" + " OR " + sMessageToUserId + " IN (" + objectIds + ")" + ")";

        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.getString(0) != null) {
                File file = new File(cursor.getString(0));
                try {
                    file.delete();
                } catch (Exception e) {
                    Logger.error("deleteDataAssociated", e.getMessage());
                }
            }
        }

        cursor.close();
    }

    @WorkerThread
    public void deleteContactsById(List<String> customer) {
        String args = TextUtils.join(",", customer);
        // Delete avatars/images/videos/audios associated with contact list
        deleteAllDataAssociatedWithUser(args, customer.size());
        openDatabase().execSQL(String.format("DELETE FROM " + TABLE_CV_CONTACTS
                + " WHERE " + COLUMN_ID + " IN (%s);", args));
    }

    public dbCustomer isContact(String businessId) {
        Cursor cursor = openDatabase().query(TABLE_CV_CONTACTS, null, sBusinessCustomerId + " = ?", new String[]{businessId}, null, null, null);
        cursor.moveToFirst();
        dbCustomer contact = null;

        while (!cursor.isAfterLast()) {
            contact = cursorToUser(cursor);
            cursor.moveToNext();
        }

        cursor.close();

        return contact;
    }

    private dbCustomer cursorToUser(Cursor cursor) {
        dbCustomer contact = new dbCustomer();
        contact.setId(cursor.getLong(0));
        contact.setCustomerId(cursor.getString(1));
        contact.setDisplayName(cursor.getString(2));
        contact.setRecent(cursor.getLong(3));
        contact.setComposingMessage(cursor.getString(4));
        contact.setBlocked(cursor.getString(5).contentEquals("Y"));
        contact.setMuted(cursor.getString(6).contentEquals("Y"));
        contact.setCreated(cursor.getLong(7));
        return contact;
    }

    /* ******************************************* */
    /* ******************************************* */
    /* ******************************************* */

    public void saveMessage(dbMessage newMessage) {
        ContentValues message = new ContentValues();
        message.put(sMessageFromUserId, newMessage.getFromUserId());
        message.put(sMessageToUserId, newMessage.getToUserId());
        message.put(sMessageType, newMessage.getMessageType());
        message.put(sMessageDeliveryStatus, newMessage.getDeliveryStatus());
        message.put(sMessageBody, newMessage.getBody());
        message.put(sMessageLocalUrl, newMessage.getLocalUrl());
        message.put(sMessageRemoteUrl, newMessage.getRemoteUrl());
        message.put(sMessageLongitude, newMessage.getLongitude());
        message.put(sMessageLatitude, newMessage.getLatitude());
        message.put(sMessageCreatedAt, newMessage.getCreated());
        message.put(sMessageViewAt, newMessage.getViewAt());
        message.put(sMessageReadAt, newMessage.getReadAt());
        message.put(sMessageMessageId, newMessage.getMessageId());
        message.put(sMessageWidth, newMessage.getWidth());
        message.put(sMessageHeight, newMessage.getHeight());
        message.put(sMessageDuration, newMessage.getDuration());
        message.put(sMessageBytes, newMessage.getBytes());
        message.put(sMessageProgress, newMessage.getProgress());

        long id = openDatabase().insert(TABLE_MESSAGES, null, message);

        if(id > 0) {
            newMessage.setId(id);
        }
    }

    public int updateDeliveryStatus(long messageId, int status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(sMessageDeliveryStatus, status);
        return openDatabase().update(TABLE_MESSAGES, contentValues, COLUMN_ID + " = ?", new String[]{Long.toString(messageId)});
    }

    public synchronized int updateLocalUrl(long messageId, String url) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(sMessageLocalUrl, url);
        return openDatabase().update(TABLE_MESSAGES, contentValues, COLUMN_ID + " = ?", new String[]{Long.toString(messageId)});
    }

    public nChatItem getLastMessageAndUnredCount(String fromId) {
        String id = ConversaApp.getInstance(context).getPreferences().getAccountBusinessId();

        String query = "SELECT *, " +
                "(" +
                "SELECT COUNT(*) FROM " +
                TABLE_MESSAGES +
                " WHERE " +
                sMessageFromUserId +
                " = \'" +
                fromId +
                "\' AND " +
                sMessageViewAt +
                " = 0) FROM (" +
                "SELECT * FROM " +
                TABLE_MESSAGES +
                " WHERE " +
                sMessageFromUserId +
                " = \'" +
                fromId +
                "\' AND " +
                sMessageToUserId +
                " = \'" +
                id +
                "\'" +
                " OR " +
                sMessageFromUserId +
                " = \'" +
                id +
                "\' AND " +
                sMessageToUserId +
                " = \'" +
                fromId +
                "\'" +
                " ORDER BY " +
                sMessageCreatedAt +
                " DESC LIMIT 1 " +
                ")";

        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();
        dbMessage message = null;
        int count = 0;

        while (!cursor.isAfterLast()) {
            message = cursorToMessage(cursor);
            count = cursor.getInt(19);
            cursor.moveToNext();
        }

        // make sure to close the cursor
        cursor.close();

        return new nChatItem(message, (count > 0));
    }

    public int updateViewMessages(String id) {
        ContentValues contentValues = new ContentValues();
        long currentTimestamp = System.currentTimeMillis();
        contentValues.put(sMessageViewAt, currentTimestamp);
        String fromId = ConversaApp.getInstance(context).getPreferences().getAccountBusinessId();
        return openDatabase().update(TABLE_MESSAGES, contentValues,
                "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)"
                        + " OR "
                        + "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)",
                new String[] {id, fromId, fromId, id} );
    }

    public int updateReadMessages(String id) {
        ContentValues contentValues = new ContentValues();
        long currentTimestamp = System.currentTimeMillis();
        contentValues.put(sMessageReadAt, currentTimestamp);
        String fromId = ConversaApp.getInstance(context).getPreferences().getAccountBusinessId();
        return openDatabase().update(TABLE_MESSAGES, contentValues,
                "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)"
                        + " OR "
                        + "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)",
                new String[] {id, fromId, fromId, id} );
    }

    public int deleteAllMessagesById(String id) {
        deleteAllDataAssociatedToMessagesWithUser("\'" + id + "\'");
        String fromId = ConversaApp.getInstance(context).getPreferences().getAccountBusinessId();
        int result = openDatabase().delete(TABLE_MESSAGES,
                "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)"
                + " OR "
                + "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)",
                new String[] {id, fromId, fromId, id});
        Logger.error("MySQLiteHelper", "A total of  " + result + " messages were deleted from internal database for contact " + id);
        return result;
    }

    public List<dbMessage> getMessagesByContact(String id, int count, int offset) throws SQLException {
        String fromId = ConversaApp.getInstance(context).getPreferences().getAccountBusinessId();
        String query = "SELECT m.* FROM "
                        + TABLE_MESSAGES + " m"
                        + " WHERE m." + sMessageFromUserId + " = \'" + id + "\' AND m." + sMessageToUserId + " = \'" + fromId + "\'"
                        + " UNION ALL " +
                        "SELECT p.* FROM "
                        + TABLE_MESSAGES + " p"
                        + " WHERE p." + sMessageFromUserId + " = \'" + fromId + "\' AND p." + sMessageToUserId + " = \'" + id + "\'"
                        + " ORDER BY " + sMessageCreatedAt + " DESC LIMIT " + count + " OFFSET " + offset;
        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToLast();
        ArrayList<dbMessage> messages = new ArrayList<>(cursor.getCount());

        while (!cursor.isBeforeFirst()) {
            dbMessage contact = cursorToMessage(cursor);
            messages.add(contact);
            cursor.moveToPrevious();
        }
        // make sure to close the cursor
        cursor.close();
        return messages;
    }

    public dbMessage getMessageById(long id) {
        dbMessage message = null;
        String query = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_ID + " = " + id;

        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            message = cursorToMessage(cursor);
            cursor.moveToNext();
        }

        // make sure to close the cursor
        cursor.close();
        return message;
    }

    private dbMessage cursorToMessage(Cursor cursor) {
        dbMessage message = new dbMessage();
        message.setId(cursor.getLong(0));
        message.setFromUserId(cursor.getString(1));
        message.setToUserId(cursor.getString(2));
        message.setMessageType(cursor.getString(3));
        message.setDeliveryStatus(cursor.getInt(4));
        message.setBody(cursor.getString(5));
        message.setLocalUrl(cursor.getString(6));
        message.setRemoteUrl(cursor.getString(7));
        message.setLongitude(cursor.getFloat(8));
        message.setLatitude(cursor.getFloat(9));
        message.setCreated(cursor.getLong(10));
        message.setViewAt(cursor.getLong(11));
        message.setReadAt(cursor.getLong(12));
        message.setMessageId(cursor.getString(13));
        message.setWidth(cursor.getInt(14));
        message.setHeight(cursor.getInt(15));
        message.setDuration(cursor.getInt(16));
        message.setBytes(cursor.getLong(17));
        message.setProgress(cursor.getInt(18));
        return message;
    }

    /* ******************************************* */
    /* ******************************************* */
    /* ******************************************* */

    public NotificationInformation getGroupInformation(String group_id) {
        String query = "SELECT " + COLUMN_ID + "," + sNotificationAndroidId + "," + sNotificationCount + " FROM " + TABLE_NOTIFICATION + " WHERE " + sNotificationGroup + " = \'" + group_id + "\'" + " LIMIT 1";
        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();
        NotificationInformation information = new NotificationInformation(group_id);

        while (!cursor.isAfterLast()) {
            information.setNotificationId(cursor.getInt(0));
            information.setAndroidNotificationId(cursor.getLong(1));
            information.setCount(cursor.getInt(2));
            cursor.moveToNext();
        }

        cursor.close();
        return information;
    }

    public void incrementGroupCount(NotificationInformation information, boolean create) {
        if (create) {
            // Create record
            ContentValues record = new ContentValues();
            record.put(sNotificationAndroidId, information.getAndroidNotificationId());
            record.put(sNotificationGroup, information.getGroupId());
            record.put(sNotificationCount, 1);
            information.setNotificationId(openDatabase().insert(TABLE_NOTIFICATION, null, record));
        } else {
            // Update record
            information.setCount(information.getCount() + 1);
            openDatabase().execSQL(String.format(Locale.US, "UPDATE %s SET %s = (%s + 1) WHERE %s = %d;",
                    TABLE_NOTIFICATION, sNotificationCount, sNotificationCount, COLUMN_ID, information.getNotificationId()));
        }
    }

    public void resetGroupCount(long notificationId) {
        openDatabase().execSQL(String.format(Locale.US, "UPDATE %s SET %s = 0 WHERE %s = %d;",
                TABLE_NOTIFICATION, sNotificationCount, COLUMN_ID, notificationId));
    }

    public void resetAllCounts() {
        openDatabase().execSQL(String.format("UPDATE %s SET %s = 0;",
                TABLE_NOTIFICATION, sNotificationCount));
    }

    /************************************************************/
    /*******************CREATE/UPGRADE METHODS*******************/
    /************************************************************/

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME1, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_MESSAGES_CREATE);
            db.execSQL(tmIndex1);
            db.execSQL(tmIndex2);
            db.execSQL(TABLE_CONTACTS_CREATE);
            db.execSQL(tcIndex1);
            db.execSQL(TABLE_NOTIFICATION_CREATE);
            db.execSQL(newMessageTrigger);
            db.execSQL(deleteUserTrigger);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Logger.error(TAG, "Upgrading database MESSAGES from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CV_CONTACTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATION);
            onCreate(db);
        }
    }

}