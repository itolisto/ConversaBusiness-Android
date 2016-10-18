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
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ee.app.conversabusiness.ConversaApp;
import ee.app.conversabusiness.model.database.NotificationInformation;
import ee.app.conversabusiness.model.database.dbCustomer;
import ee.app.conversabusiness.model.database.dbMessage;
import ee.app.conversabusiness.model.nChatItem;
import ee.app.conversabusiness.utils.Const;
import ee.app.conversabusiness.utils.Logger;

public class MySQLiteHelper {

    private static final String TAG = "MySQLiteHelper";
    private final Context context;
    private DatabaseHelper myDbHelper;

    private static final String DATABASE_NAME1 = "conversabusinessdb.db";
    private static final int DATABASE_VERSION = 2;

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
            + "\"" + sMessageDeliveryStatus + "\" CHAR(1) NOT NULL, "
            + "\"" + sMessageBody + "\" TEXT, "
            + "\"" + sMessageLocalUrl + "\" TEXT, "
            + "\"" + sMessageRemoteUrl + "\" TEXT, "
            + "\"" + sMessageLongitude + "\" REAL DEFAULT 0, "
            + "\"" + sMessageLatitude + "\" REAL DEFAULT 0, "
            + "\"" + sMessageCreatedAt + "\" INTEGER NOT NULL, "
            + "\"" + sMessageViewAt + "\" INTEGER NOT NULL DEFAULT 0, "
            + "\"" + sMessageReadAt + "\" INTEGER NOT NULL DEFAULT 0, "
            + "\"" + sMessageMessageId + "\" CHAR(14),"
            + "\"" + sMessageWidth + "\" INTEGER DEFAULT 0,"
            + "\"" + sMessageHeight + "\" INTEGER DEFAULT 0,"
            + "\"" + sMessageDuration + "\" INTEGER DEFAULT 0,"
            + "\"" + sMessageBytes + "\" INTEGER DEFAULT 0, "
            + "\"" + sMessageProgress + "\" INTEGER DEFAULT 0 );";

    private static final String tmIndex1 = "CREATE INDEX M_search on "  + TABLE_MESSAGES + "(" + sMessageFromUserId + ", " + sMessageToUserId + "); ";
    private static final String tmIndex2 = "CREATE UNIQUE INDEX IF NOT EXISTS C_messageId on "  + TABLE_MESSAGES + "(" + sMessageMessageId + ");";

    // CONTACTS
    private static final String sBusinessCustomerId = "customerId";
    private static final String sBusinessDisplayName = "displayName";
    private static final String sBusinessRecent = "recent";
    private static final String sBusinessName = "name";
    private static final String sBusinessComposingMessage = "composingMessageString";
    private static final String sBusinessAvatarFile = "avatar_file_url";
    private static final String sBusinessBlocked = "blocked";
    private static final String sBusinessMuted = "muted";
    private static final String sBusinessCreatedAt = "created_at";

    private static final String TABLE_CONTACTS_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_CV_CONTACTS + "("
            + "\"" + COLUMN_ID + "\" INTEGER PRIMARY KEY, "
            + "\"" + sBusinessCustomerId + "\" CHAR(14) NOT NULL, "
            + "\"" + sBusinessDisplayName + "\" VARCHAR(180) NOT NULL, "
            + "\"" + sBusinessRecent + "\" INTEGER, "
            + "\"" + sBusinessName + "\" VARCHAR(100) NOT NULL, "
            + "\"" + sBusinessComposingMessage + "\" VARCHAR(255), "
            + "\"" + sBusinessAvatarFile + "\" VARCHAR(355), "
            + "\"" + sBusinessBlocked + "\" CHAR(1) NOT NULL DEFAULT 'N', "
            + "\"" + sBusinessMuted + "\" CHAR(1) NOT NULL DEFAULT 'N', "
            + "\"" + sBusinessCreatedAt + "\" INTEGER NOT NULL );";

    private static final String tcIndex1 = "CREATE UNIQUE INDEX IF NOT EXISTS C_customerId on "  + TABLE_CV_CONTACTS + "(" + sBusinessCustomerId + ");";

//    private static final String TABLE_STATISTICS_CREATE = "CREATE TABLE "
//            + TABLE_STATISTICS + "("
//            + "\"_id\" INTEGER PRIMARY KEY, "
//            + "\"conversa_id\" VARCHAR(255) NOT NULL, "
//            + "\"messages_sent_by_push_id\" INTEGER NOT NULL, "
//            + "\"messages_sent_by_business\" INTEGER NOT NULL, "
//            + "\"messages_receive_by_push_id\" INTEGER NOT NULL, "
//            + "\"messages_receive_by_business\" INTEGER NOT NULL, "
//            + "\"max_devices\" INTEGER NOT NULL, "
//            + "\"category_title\" VARCHAR(30) NOT NULL, "
//            + "\"category_avatar\" VARCHAR(30) NOT NULL, "
//            + "\"number_of_contacts\" INTEGER NOT NULL, "
//            + "\"number_of_favs\" INTEGER NOT NULL, "
//            + "\"number_of_block_contacts\" INTEGER NOT NULL, "
//            + "\"number_of_keywords_set\" INTEGER NOT NULL, "
//            + "\"number_of_first_replies\" INTEGER NOT NULL, "
//            + "\"number_of_locations\" INTEGER NOT NULL, "
//            + "\"remaining_diffusion_messages\" INTEGER NOT NULL, "
//            + "\"subscription_finish\" INTEGER NOT NULL,"
//            + "\"lastget\" INTEGER NOT NULL ); ";

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

    public SQLiteDatabase openDatabase() throws SQLException {
        return myDbHelper.getWritableDatabase();
    }

    public boolean deleteDatabase() {
        context.deleteDatabase(DATABASE_NAME1);
        return true;
    }

    /************************************************************/
    /*********************OPERATIONS METHODS*********************/
    /************************************************************/
    public dbCustomer saveContact(dbCustomer user) {
        ContentValues contact = new ContentValues();
        contact.put(sBusinessCustomerId, user.getCustomerId());
        contact.put(sBusinessDisplayName, user.getDisplayName());
        contact.put(sBusinessRecent, user.getRecent());
        contact.put(sBusinessName, user.getName());
        contact.put(sBusinessComposingMessage, "");
        contact.put(sBusinessAvatarFile, user.getAvatarThumbFileId());
        contact.put(sBusinessBlocked, "N");
        contact.put(sBusinessMuted, "N");
        contact.put(sBusinessCreatedAt, user.getCreated());

        long result = openDatabase().insert(TABLE_CV_CONTACTS, null, contact);

        if (result > 0) {
            user.setId(result);
        }

        return user;
    }

    public synchronized int updateContactAvatar(long contactId, String avatarUrl) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(sBusinessAvatarFile, avatarUrl);
        return openDatabase().update(TABLE_CV_CONTACTS, contentValues, COLUMN_ID + " = ?", new String[]{Long.toString(contactId)});
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
    public void deleteDataAssociatedWithUser(String ids, int total) {
        String query = "SELECT " + sBusinessAvatarFile + "," + sBusinessCustomerId + " FROM "
                + TABLE_CV_CONTACTS + " WHERE " + COLUMN_ID + " IN (" + ids + ")";

        Cursor cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();
        List<String> avatars = new ArrayList<>(total);
        List<String> business = new ArrayList<>(total);

        while (!cursor.isAfterLast()) {
            avatars.add(cursor.getString(0));
            business.add(cursor.getString(1));
            cursor.moveToNext();
        }

        cursor.close();
        int i = 0;

        for (i = 0; i < avatars.size(); i++) {
            if (avatars.get(i) != null) {
                File file = new File(avatars.get(i));
                try {
                    file.delete();
                } catch (SecurityException e) {
                    Logger.error("deleteDataAssociated", e.getMessage());
                }
            }
        }

        // 132 is the min string. It happens when only one user is deleted
        StringBuilder objectIds = new StringBuilder(15);
        for (i = 0; i < business.size(); i++) {
            objectIds.append("\'");
            objectIds.append(business.get(i));
            objectIds.append("\'");
            if (i + 1 < business.size()) {
                objectIds.append(",");
            }
        }

        StringBuilder sb = new StringBuilder(132);
        // Delete data associated with messages
        sb.append("SELECT ");
        sb.append(sMessageLocalUrl);
        sb.append(" FROM ");
        sb.append(TABLE_MESSAGES);
        sb.append(" WHERE ");
        sb.append(sMessageType);
        sb.append(" NOT IN (\'");
        sb.append(Const.kMessageTypeLocation);
        sb.append("\',\'");
        sb.append(Const.kMessageTypeText);
        sb.append("\')");
        sb.append(" AND (");
        sb.append(sMessageFromUserId);
        sb.append(" IN (");
        sb.append(objectIds);
        sb.append(")");
        sb.append(" OR ");
        sb.append(sMessageToUserId);
        sb.append(" IN (");
        sb.append(objectIds);
        sb.append(")");
        sb.append(")");

        query = sb.toString();

        cursor = openDatabase().rawQuery(query, new String[]{});
        cursor.moveToFirst();

        for (i = 0; i < cursor.getCount(); i++) {
            if (cursor.getString(0) != null) {
                File file = new File(cursor.getString(0));
                try {
                    file.delete();
                } catch (SecurityException e) {
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
        deleteDataAssociatedWithUser(args, customer.size());
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
        contact.setName(cursor.getString(4));
        contact.setComposingMessage(cursor.getString(5));
        contact.setAvatarThumbFileId(cursor.getString(6));
        boolean b = cursor.getString(7).contentEquals("Y");
        contact.setBlocked(b);
        b = cursor.getString(8).contentEquals("Y");
        contact.setMuted(b);
        contact.setCreated(cursor.getLong(9));
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

    public int updateDeliveryStatus(long messageId, String status) {
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
        String id = ConversaApp.getInstance(context).getPreferences().getBusinessId();

        StringBuilder sbQuery = new StringBuilder(565);
        sbQuery.append("SELECT *, ");
        sbQuery.append("(");
        sbQuery.append("SELECT COUNT(*) FROM ");
        sbQuery.append(TABLE_MESSAGES);
        sbQuery.append(" WHERE ");
        sbQuery.append(sMessageFromUserId);
        sbQuery.append(" = \'");
        sbQuery.append(fromId);
        sbQuery.append("\' AND ");
        sbQuery.append(sMessageViewAt);
        sbQuery.append(" = 0) FROM (");
        sbQuery.append("SELECT * FROM ");
        sbQuery.append(TABLE_MESSAGES);
        sbQuery.append(" WHERE ");
        sbQuery.append(sMessageFromUserId);
        sbQuery.append(" = \'");
        sbQuery.append(fromId);
        sbQuery.append("\' AND ");
        sbQuery.append(sMessageToUserId);
        sbQuery.append(" = \'");
        sbQuery.append(id);
        sbQuery.append("\'");
        sbQuery.append(" OR ");
        sbQuery.append(sMessageFromUserId);
        sbQuery.append(" = \'");
        sbQuery.append(id);
        sbQuery.append("\' AND ");
        sbQuery.append(sMessageToUserId);
        sbQuery.append(" = \'");
        sbQuery.append(fromId);
        sbQuery.append("\'");
        sbQuery.append(" ORDER BY ");
        sbQuery.append(sMessageCreatedAt);
        sbQuery.append(" DESC LIMIT 1 ");
        sbQuery.append(")");

        String query = sbQuery.toString();
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
        String fromId = ConversaApp.getInstance(context).getPreferences().getBusinessId();
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
        String fromId = ConversaApp.getInstance(context).getPreferences().getBusinessId();
        return openDatabase().update(TABLE_MESSAGES, contentValues,
                "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)"
                        + " OR "
                        + "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)",
                new String[] {id, fromId, fromId, id} );
    }

    private int deleteAllMessagesById(String id) {
        String fromId = ConversaApp.getInstance(context).getPreferences().getBusinessId();
        int result = openDatabase().delete(TABLE_MESSAGES,
                "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)"
                + " OR "
                + "(" + sMessageFromUserId + " = ? AND " + sMessageToUserId + " = ?)",
                new String[] {id, fromId, fromId, id});
        Logger.error("MySQLiteHelper", "A total of  " + result + " messages were deleted from internal database for contact " + id);
        return result;
    }

    public List<dbMessage> getMessagesByContact(String id, int count, int offset) throws SQLException {
        String fromId = ConversaApp.getInstance(context).getPreferences().getBusinessId();
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
        ArrayList<dbMessage> messages = new ArrayList<>(cursor.getCount());

        while (!cursor.isAfterLast()) {
            dbMessage contact = cursorToMessage(cursor);
            messages.add(contact);
            cursor.moveToNext();
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
        message.setDeliveryStatus(cursor.getString(4));
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
        message.setBytes(cursor.getInt(17));
        message.setProgress(cursor.getInt(18));
        return message;
    }

    /* ******************************************* */
    /* ******************************************* */
    /* ******************************************* */

//    public Statistics getStatistics() {
//        Statistics statistics = null;
//
//        openStatisticsTable();
//        Cursor cursor = myDb.query(TABLE_STATISTICS,null,null,null,null,null,null);
//        cursor.moveToFirst();
//
//        while (!cursor.isAfterLast()) {
//            statistics = cursorToStatistic(cursor);
//            cursor.moveToNext();
//        }
//        // make sure to close the cursor
//        cursor.close();
//        closeStatisticsTable();
//
//        return statistics;
//    }
//
//    public void saveStatistics(Statistics save) {
//
//        Statistics has = getStatistics();
//
//        if(has == null) {
//
//            ContentValues newStatistics = new ContentValues();
//
//            newStatistics.put("conversa_id", "adsflkj2r3afdsk");
//            newStatistics.put("messages_sent_by_push_id", save.getTotalSentMessagesByPushId());
//            newStatistics.put("messages_sent_by_business", save.getTotalSentMessages());
//            newStatistics.put("messages_receive_by_push_id", save.getTotalReceivedMessagesByPushId());
//            newStatistics.put("messages_receive_by_business", save.getTotalReceivedMessages());
//            newStatistics.put("max_devices", save.getMaxDevices());
//            newStatistics.put("category_title", save.getCategoryTitle());
//            newStatistics.put("category_avatar", save.getCategoryAvatar());
//            newStatistics.put("number_of_contacts", save.getNumberOfContacts());
//            newStatistics.put("number_of_favs", save.getNumberOfFavs());
//            newStatistics.put("number_of_block_contacts", save.getNumberOfBlockedUsers());
//            newStatistics.put("number_of_keywords_set", save.getNumberOfKeywords());
//            newStatistics.put("number_of_first_replies", save.getNumberOfReplies());
//            newStatistics.put("number_of_locations", save.getNumberOfLocations());
//            newStatistics.put("remaining_diffusion_messages", save.getRemainingDiffusion());
//            newStatistics.put("subscription_finish", save.getExpirationDate());
//            newStatistics.put("lastget", save.getLastget());
//
//            openStatisticsTable();
//            myDb.insert(TABLE_STATISTICS, null, newStatistics);
//            closeStatisticsTable();
//        } else {
//            updateStatistics(save);
//        }
//    }
//
//        /*
//        + "\"conversa_id\" VARCHAR(255) NOT NULL, "
//        */
//
//    public boolean updateStatistics(Statistics save){
//        ContentValues contentValues = new ContentValues();
//
//        contentValues.put("conversa_id", "adsflkj2r3afdsk");
//        contentValues.put("messages_sent_by_push_id",       save.getTotalSentMessagesByPushId());
//        contentValues.put("messages_sent_by_business",      save.getTotalSentMessages());
//        contentValues.put("messages_receive_by_push_id",    save.getTotalReceivedMessagesByPushId());
//        contentValues.put("messages_receive_by_business",   save.getTotalReceivedMessages());
//        contentValues.put("max_devices",                    save.getMaxDevices());
//        contentValues.put("category_title",                 save.getCategoryTitle());
//        contentValues.put("category_avatar",                save.getCategoryAvatar());
//        contentValues.put("number_of_contacts",             save.getNumberOfContacts());
//        contentValues.put("number_of_favs",                 save.getNumberOfFavs());
//        contentValues.put("number_of_block_contacts",       save.getNumberOfBlockedUsers());
//        contentValues.put("number_of_keywords_set",         save.getNumberOfKeywords());
//        contentValues.put("number_of_first_replies",        save.getNumberOfReplies());
//        contentValues.put("number_of_locations",            save.getNumberOfLocations());
//        contentValues.put("remaining_diffusion_messages",   save.getRemainingDiffusion());
//        contentValues.put("subscription_finish",            save.getExpirationDate());
//        contentValues.put("lastget",                        save.getLastget());
//
//        openStatisticsTable();
//        myDb.update(TABLE_STATISTICS, contentValues, "_id = ?", new String[] { "1" } );
//        closeStatisticsTable();
//        return true;
//    }
//
//    private Statistics cursorToStatistic(Cursor cursor) {
//        Statistics statistics = new Statistics();
//
//        statistics.setTotalSentMessagesByPushId     (cursor.getString(1));
//        statistics.setTotalSentMessages             (cursor.getString(2));
//        statistics.setTotalReceivedMessagesByPushId (cursor.getString(3));
//        statistics.setTotalReceivedMessages         (cursor.getString(4));
//        statistics.setMaxDevices                    (cursor.getString(5));
//        statistics.setCategoryTitle                 (cursor.getString(6));
//        statistics.setCategoryAvatar                (cursor.getString(7));
//        statistics.setNumberOfContacts              (cursor.getString(8));
//        statistics.setNumberOfFavs                  (cursor.getString(9));
//        statistics.setNumberOfBlockedUsers          (cursor.getString(10));
//        statistics.setNumberOfKeywords              (cursor.getString(11));
//        statistics.setNumberOfReplies               (cursor.getString(12));
//        statistics.setNumberOfLocations             (cursor.getString(13));
//        statistics.setRemainingDiffusion            (cursor.getString(14));
//        statistics.setExpirationDate                (cursor.getString(15));
//        statistics.setLastget                       (cursor.getString(16));
//
//        return statistics;
//    }

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

    public NotificationInformation incrementGroupCount(NotificationInformation information, boolean create) {
        if (create) {
            // Create record
            ContentValues record = new ContentValues();
            record.put(sNotificationAndroidId, information.getAndroidNotificationId());
            record.put(sNotificationGroup, information.getGroupId());
            record.put(sNotificationCount, 1);
            information.setNotificationId(openDatabase().insert(TABLE_NOTIFICATION, null, record));
        } else {
            // Update record
            openDatabase().execSQL(String.format(Locale.US, "UPDATE %s SET %s = (%s + 1) WHERE %s = %d;",
                    TABLE_NOTIFICATION, sNotificationCount, sNotificationCount, COLUMN_ID, information.getNotificationId()));
        }

        return information;
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