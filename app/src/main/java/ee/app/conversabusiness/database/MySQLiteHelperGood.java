package ee.app.conversabusiness.database;

/**
 * Created by edgargomez on 2/11/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.UiThread;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ee.app.conversabusiness.ConversaApp;
import ee.app.conversabusiness.interfaces.OnContactTaskCompleted;
import ee.app.conversabusiness.interfaces.OnMessageTaskCompleted;
import ee.app.conversabusiness.model.Database.Message;
import ee.app.conversabusiness.model.Database.dCustomer;
import ee.app.conversabusiness.response.ContactResponse;
import ee.app.conversabusiness.response.MessageResponse;
import ee.app.conversabusiness.utils.Logger;

public class MySQLiteHelperGood {

    private OnMessageTaskCompleted messageListeners;
    private OnContactTaskCompleted contactListeners;

    private static final String TAG = "MySQLiteHelper";
    private final Context context;
    private DatabaseHelper myDbHelper;

    private static final String DATABASE_NAME1 = "conversabusinessdb.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MESSAGES = "message";
    private static final String TABLE_CV_CONTACTS = "cv_contact";

    private static final String COLUMN_ID = "_id";

    // MESSAGE
    public static final String sMessageFromUserId = "from_user_id";
    public static final String sMessageToUserId = "to_user_id";
    public static final String sMessageType = "message_type";
    public static final String sMessageDeliveryStatus = "delivery_status";
    public static final String sMessageBody = "body";
    public static final String sMessageFileId = "file_id";
    public static final String sMessageLongitude = "longitude";
    public static final String sMessageLatitude = "latitude";
    public static final String sMessageCreatedAt = "created_at";
    public static final String sMessageModifiedAt = "modified_at";
    public static final String sMessageReadAt = "read_at";
    public static final String sMessageMessageId = "message_id";
    public static final String sMessageWidth = "width";
    public static final String sMessageHeight = "height";
    public static final String sMessageDuration = "duration";
    public static final String sMessageBytes = "bytes";

    private static final String TABLE_MESSAGES_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_MESSAGES + "("
            + "\"" + COLUMN_ID + "\" INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "\"" + sMessageFromUserId + "\" CHAR(14) NOT NULL, "
            + "\"" + sMessageToUserId + "\" CHAR(14) NOT NULL, "
            + "\"" + sMessageType + "\" CHAR(1) NOT NULL, "
            + "\"" + sMessageDeliveryStatus + "\" CHAR(1) NOT NULL, "
            + "\"" + sMessageBody + "\" TEXT, "
            + "\"" + sMessageFileId + "\" TEXT, "
            + "\"" + sMessageLongitude + "\" REAL DEFAULT 0, "
            + "\"" + sMessageLatitude + "\" REAL DEFAULT 0, "
            + "\"" + sMessageCreatedAt + "\" INTEGER NOT NULL, "
            + "\"" + sMessageModifiedAt + "\" INTEGER NOT NULL DEFAULT '0', "
            + "\"" + sMessageReadAt + "\" INTEGER NOT NULL DEFAULT '0', "
            + "\"" + sMessageMessageId + "\" CHAR(14),"
            + "\"" + sMessageWidth + "\" INTEGER DEFAULT 0,"
            + "\"" + sMessageHeight + "\" INTEGER DEFAULT 0,"
            + "\"" + sMessageDuration + "\" INTEGER DEFAULT 0,"
            + "\"" + sMessageBytes + "\" INTEGER DEFAULT 0 );";
    private static final String tmIndex1 = "CREATE INDEX M_search on "  + TABLE_MESSAGES + "(" + sMessageFromUserId + ", " + sMessageToUserId + "); ";
    private static final String tmIndex2 = "CREATE UNIQUE INDEX IF NOT EXISTS C_messageId on "  + TABLE_MESSAGES + "(" + sMessageMessageId + ");";

    // CONTACTS
    public static final String sBusinessCustomerId = "customerId";
    public static final String sBusinessDisplayName = "displayName";
    public static final String sBusinessRecent = "recent";
    public static final String sBusinessName = "name";
    public static final String sBusinessStatus = "statusMessage";
    public static final String sBusinessComposingMessage = "composingMessageString";
    public static final String sBusinessAvatarFile = "avatar_file_url";
    public static final String sBusinessBlocked = "blocked";
    public static final String sBusinessMuted = "muted";
    public static final String sBusinessCreatedAt = "created_at";

    private static final String TABLE_CONTACTS_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_CV_CONTACTS + "("
            + "\"" + COLUMN_ID + "\" INTEGER PRIMARY KEY, "
            + "\"" + sBusinessCustomerId + "\" CHAR(14) NOT NULL, "
            + "\"" + sBusinessDisplayName + "\" VARCHAR(180) NOT NULL, "
            + "\"" + sBusinessRecent + "\" INTEGER, "
            + "\"" + sBusinessName + "\" VARCHAR(100) NOT NULL, "
            + "\"" + sBusinessStatus + "\" VARCHAR(255), "
            + "\"" + sBusinessComposingMessage + "\" VARCHAR(255), "
            + "\"" + sBusinessAvatarFile + "\" VARCHAR(355), "
            + "\"" + sBusinessBlocked + "\" CHAR(1) NOT NULL DEFAULT 'N', "
            + "\"" + sBusinessMuted + "\" CHAR(1) NOT NULL DEFAULT 'N', "
            + "\"" + sBusinessCreatedAt + "\" INTEGER NOT NULL );";
    private static final String tcIndex1 = "CREATE UNIQUE INDEX IF NOT EXISTS C_customerId on "  + TABLE_CV_CONTACTS + "(" + sBusinessCustomerId + ");";

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

    public MySQLiteHelperGood(Context context) {
        this.context = context;
        myDbHelper = new DatabaseHelper(context);
        messageListeners = null;
        contactListeners = null;
        openDatabase();
        closeDatabase();
    }

    public SQLiteDatabase openDatabase() throws SQLException {
        return myDbHelper.getWritableDatabase();
    }

    public void closeDatabase() {
        if (myDbHelper != null) {
            myDbHelper.close();
        }
    }

    public boolean deleteDatabase() {
        context.deleteDatabase(DATABASE_NAME1);
        return true;
    }

    /************************************************************/
    /*********************OPERATIONS METHODS*********************/
    /************************************************************/
    public dCustomer saveContact(dCustomer user) {
        ContentValues contact = new ContentValues();
        contact.put(sBusinessCustomerId, user.getBusinessId());
        contact.put(sBusinessDisplayName, user.getDisplayName());
        contact.put(sBusinessRecent, user.getRecent());
        contact.put(sBusinessName, user.getName());
        contact.put(sBusinessStatus, user.getStatusMessage());
        contact.put(sBusinessComposingMessage, "");
        contact.put(sBusinessAvatarFile, user.getAvatarThumbFileId());
        contact.put(sBusinessBlocked, "N");
        contact.put(sBusinessMuted, "N");
        contact.put(sBusinessCreatedAt, user.getCreated());

        long result = openDatabase().insert(TABLE_CV_CONTACTS, null, contact);
        closeDatabase();

        if (result > 0) {
            user.setId(result);
        }

        return user;
    }

    public List<dCustomer> getAllContacts() {
        List<dCustomer> contacts = new ArrayList<>();

        Cursor cursor = openDatabase().query(TABLE_CV_CONTACTS,null,null,null,null,null, sBusinessRecent + " DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            dCustomer contact = cursorToUser(cursor);
            contacts.add(contact);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        closeDatabase();

        return contacts;
    }

    public dCustomer deleteContactById(dCustomer customer) {
        String id = Long.toString(customer.getId());
        int result = openDatabase().delete(TABLE_CV_CONTACTS, COLUMN_ID + " = ? ", new String[]{id});
        closeDatabase();

        if(result > 0) {
            customer.setId(-1);
        }

        return customer;
    }

    public dCustomer isContact(String businessId) {
        Cursor cursor = openDatabase().query(TABLE_CV_CONTACTS, null, sBusinessCustomerId + " = ?", new String[]{businessId}, null, null, null);
        cursor.moveToFirst();
        dCustomer contact = null;

        while (!cursor.isAfterLast()) {
            contact = cursorToUser(cursor);
            cursor.moveToNext();
        }

        cursor.close();
        closeDatabase();

        return contact;
    }

    public boolean hasPendingMessages(String id) {
        Cursor cursor = openDatabase().query(TABLE_CV_CONTACTS, new String[] {"hasPendingMessages"}, COLUMN_ID + " = ?",new String[] { id },null,null,null);
        cursor.moveToFirst();
        int has = 1;

        while (!cursor.isAfterLast()) {
            has = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();
        closeDatabase();

        return (has == 1);
    }

    public void setHasPendingMessages(String id, int status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("hasPendingMessages", status);
        openDatabase().update(TABLE_CV_CONTACTS, contentValues, "_id = ? ", new String[]{id});
        closeDatabase();
    }

    private dCustomer cursorToUser(Cursor cursor) {
        dCustomer contact = new dCustomer();
        contact.setId(cursor.getLong(0));
        contact.setBusinessId(cursor.getString(1));
        contact.setDisplayName(cursor.getString(2));
        contact.setRecent(cursor.getLong(3));
        contact.setName(cursor.getString(4));
        contact.setStatusMessage(cursor.getString(5));
        contact.setComposingMessage(cursor.getString(6));
        contact.setAvatarThumbFileId(cursor.getString(7));
        boolean b = cursor.getString(8).contentEquals("Y");
        contact.setBlocked(b);
        b = cursor.getString(9).contentEquals("Y");
        contact.setMuted(b);
        contact.setCreated(cursor.getLong(10));
        return contact;
    }

    /* ******************************************* */
    /* ******************************************* */
    /* ******************************************* */

    public Message saveMessage(Message newMessage) {
        ContentValues message = new ContentValues();
        message.put(sMessageFromUserId, newMessage.getFromUserId());
        message.put(sMessageToUserId, newMessage.getToUserId());
        message.put(sMessageType, newMessage.getMessageType());
        message.put(sMessageDeliveryStatus, newMessage.getDeliveryStatus());
        message.put(sMessageBody, newMessage.getBody());
        message.put(sMessageFileId, newMessage.getFileId());
        message.put(sMessageLongitude, newMessage.getLongitude());
        message.put(sMessageLatitude, newMessage.getLatitude());
        message.put(sMessageCreatedAt, newMessage.getCreated());
        message.put(sMessageModifiedAt, newMessage.getModified());
        message.put(sMessageReadAt, newMessage.getReadAt());
        message.put(sMessageMessageId, newMessage.getMessageId());
        message.put(sMessageWidth, newMessage.getWidth());
        message.put(sMessageHeight, newMessage.getHeight());
        message.put(sMessageDuration, newMessage.getDuration());
        message.put(sMessageBytes, newMessage.getBytes());

        long id = openDatabase().insert(TABLE_MESSAGES, null, message);
        closeDatabase();

        if(id > 0) {
            newMessage.setId(id);
        }

        return newMessage;
    }

    public int updateDeliveryStatus(long messageId, String status) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(sMessageDeliveryStatus, status);
        int result = openDatabase().update(TABLE_MESSAGES, contentValues, COLUMN_ID + " = ?", new String[]{Long.toString(messageId)});
        closeDatabase();
        return result;
    }

    public int messageCountForContact(String id) {
        String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES + " WHERE " + sMessageFromUserId + " = \'" + id + "\'";
        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();
        int count = 0;

        while (!cursor.isAfterLast()) {
            count = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();
        closeDatabase();

        return count;
    }

    public Message getLastMessage(String id) {
        String fromId = ConversaApp.getPreferences().getBusinessId();
        Message message = null;
        String query = "SELECT m.* FROM "
                        + TABLE_MESSAGES + " m"
                        + " WHERE m." + sMessageFromUserId + " = \'" + id + "\' AND m." + sMessageToUserId + " = \'" + fromId + "\'"
                        + " UNION ALL " +
                        "SELECT p.* FROM "
                        + TABLE_MESSAGES + " p"
                        + " WHERE p." + sMessageFromUserId + " = \'" + fromId + "\' AND p." + sMessageToUserId + " = \'" + id + "\'"
                        + " ORDER BY " + sMessageCreatedAt + " DESC LIMIT 1";

        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            message = cursorToMessage(cursor);
            cursor.moveToNext();
        }

        // make sure to close the cursor
        cursor.close();
        closeDatabase();

        return message;
    }

    public boolean hasUnreadMessagesOrNewMessages(String id) {
        String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES + " WHERE " + sMessageFromUserId + " = \'" + id + "\' AND " + sMessageReadAt + " = 0";
        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();
        int count = 0;

        while (!cursor.isAfterLast()) {
            count = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();
        closeDatabase();

        return (count > 0);
    }

    public int updateReadMessages(String id) {
        ContentValues contentValues = new ContentValues();
        long currentTimestamp = System.currentTimeMillis();
        contentValues.put(sMessageReadAt, currentTimestamp);
        String fromId = ConversaApp.getPreferences().getBusinessId();
        int result1 = openDatabase().update(TABLE_MESSAGES, contentValues,
                "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)"
                + " OR "
                + "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)",
                new String[] {id, fromId, fromId, id} );
        closeDatabase();
        return result1;
    }

    private int deleteAllMessagesById(String id) {
        String fromId = ConversaApp.getPreferences().getBusinessId();
        int result = openDatabase().delete(TABLE_MESSAGES,
                "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)"
                + " OR "
                + "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)",
                new String[] {id, fromId, fromId, id});
        closeDatabase();
        Logger.error("MySQLiteHelper", "A total of  " + result + " messages were deleted from internal database for contact " + id);
        return result;
    }

    public List<Message> getMessagesByContact(String id, int count, int offset) throws SQLException {
        String fromId = ConversaApp.getPreferences().getBusinessId();
        String query = "SELECT m.* FROM "
                        + TABLE_MESSAGES + " m"
                        + " WHERE m." + sMessageFromUserId + " = \'" + id + "\' AND m." + sMessageToUserId + " = \'" + fromId + "\'"
                        + " UNION ALL " +
                        "SELECT p.* FROM "
                        + TABLE_MESSAGES + " p"
                        + " WHERE p." + sMessageFromUserId + " = \'" + fromId + "\' AND p." + sMessageToUserId + " = \'" + id + "\'"
                        + " ORDER BY " + sMessageCreatedAt + " DESC LIMIT " + count + " OFFSET " + offset;
        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();


        ArrayList<Message> messages = new ArrayList<>(cursor.getCount());  while (!cursor.isAfterLast()) {
            Message contact = cursorToMessage(cursor);
            messages.add(contact);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        closeDatabase();
        return messages;
    }

    private Message cursorToMessage(Cursor cursor) {
        Message message = new Message();
        message.setId(cursor.getLong(0));
        message.setFromUserId(cursor.getString(1));
        message.setToUserId(cursor.getString(2));
        message.setMessageType(cursor.getString(3));
        message.setDeliveryStatus(cursor.getString(4));
        message.setBody(cursor.getString(5));
        message.setFileId(cursor.getString(6));
        message.setLongitude(cursor.getFloat(7));
        message.setLatitude(cursor.getFloat(8));
        message.setCreated(cursor.getLong(9));
        message.setModified(cursor.getLong(10));
        message.setReadAt(cursor.getLong(11));
        message.setMessageId(cursor.getString(12));
        message.setWidth(cursor.getInt(13));
        message.setHeight(cursor.getInt(14));
        message.setDuration(cursor.getInt(15));
        message.setBytes(cursor.getInt(16));
        return message;
    }

    /************************************************************/
    /*******************CREATE/UPGRADE METHODS*******************/
    /************************************************************/

    public void setMessageListener(OnMessageTaskCompleted listener) {
        messageListeners = listener;
    }

    public void removeMessageListener () {
        messageListeners = null;
    }

    @UiThread
    public void notifyMessageListeners(MessageResponse response) {
        if (messageListeners == null) {
            Log.e(TAG, "MessageListeners is null");
            return;
        }

        if (response == null) {
            Log.e(TAG, "MessageResponse parameter is null");
            return;
        }

        switch (response.getActionCode()) {
            case Message.ACTION_MESSAGE_SAVE:
                messageListeners.MessageSent(response);
                break;
            case Message.ACTION_MESSAGE_UPDATE:
            case Message.ACTION_MESSAGE_UPDATE_UNREAD:
                messageListeners.MessageUpdated(response);
                break;
            case Message.ACTION_MESSAGE_DELETE:
                messageListeners.MessageDeleted(response);
                break;
            case Message.ACTION_MESSAGE_RETRIEVE_ALL:
                messageListeners.MessagesGetAll(response);
                break;
            default:
                Log.e(TAG, "Response action code(" + response.getActionCode() + ") not defined");
                break;
        }
    }

    public void setContactListener(OnContactTaskCompleted listener) {
        contactListeners = listener;
    }

    public void removeContactListener () {
        contactListeners = null;
    }

    @UiThread
    public void notifyContactListeners(ContactResponse response) {
        if (contactListeners == null) {
            Log.e(TAG, "ContactListeners is null");
            return;
        }

        if (response == null) {
            Log.e(TAG, "ContactResponse parameter is null");
            return;
        }

        switch (response.getActionCode()) {
            case dCustomer.ACTION_MESSAGE_SAVE:
                contactListeners.ContactAdded(response);
                break;
            case dCustomer.ACTION_MESSAGE_UPDATE:
                contactListeners.ContactUpdated(response);
                break;
            case dCustomer.ACTION_MESSAGE_DELETE:
                contactListeners.ContactDeleted(response);
                break;
            case dCustomer.ACTION_MESSAGE_RETRIEVE_ALL:
                contactListeners.ContactGetAll(response);
                break;
            default:
                Log.e(TAG, "Response action code(" + response.getActionCode() + ") not defined");
                break;
        }
    }

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
            db.execSQL(newMessageTrigger);
            db.execSQL(deleteUserTrigger);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Logger.error(TAG, "Upgrading database MESSAGES from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CV_CONTACTS);
            onCreate(db);
        }
    }

}