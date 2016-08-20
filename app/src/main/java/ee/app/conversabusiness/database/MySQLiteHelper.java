//    private static final String TABLE_CONTACTS_CREATE = "CREATE TABLE "
//            + TABLE_CV_CONTACTS + "("
//            + "\"_id\" INTEGER PRIMARY KEY, "
//            + "\"name\" VARCHAR(255) NOT NULL, "
//            + "\"avatar_thumb_file_id\" VARCHAR(255) NOT NULL, "
//            + "\"recent\" INTEGER NOT NULL, "
//            + "\"stopCallingForMessages\" TINYINT NOT NULL DEFAULT '0', "
//            + "\"hasUnreadMessages\" TINYINT NOT NULL DEFAULT '0'); ";
//
//    private static final String TABLE_USER_CREATE = "CREATE TABLE "
//            + TABLE_USER + "("
//            + "\"_id\"            INTEGER PRIMARY KEY, "
//            + "\"name\"           VARCHAR(255) NOT NULL, "
//            + "\"email\"          VARCHAR(255) NOT NULL, "
//            + "\"about\"          VARCHAR(180) NOT NULL, "
//            + "\"founded\"        VARCHAR(14) NOT NULL, "
//            + "\"online_status\"  VARCHAR(15) NOT NULL, "
//            + "\"country_code\"   SMALLINT NOT NULL, "
//            + "\"contact_number\" INTEGER NOT NULL, "
//            + "\"avatar_thumb_file_id\" VARCHAR(255) NOT NULL, "
//            + "\"created\"          INTEGER NOT NULL, "
//            + "\"modified\"         INTEGER NOT NULL, "
//            + "\"token\"            VARCHAR(155) NOT NULL, "
//            + "\"country\" VARCHAR(255) NOT NULL, "
//            + "\"city\"    VARCHAR(255) NOT NULL, "
//            + "\"address\" VARCHAR(255) NOT NULL, "
//            + "\"id_category\" SMALLINT NOT NULL, "
//            + "\"paid_plan\"   SMALLINT NOT NULL, "
//            + "\"android_push_token\" VARCHAR(255) DEFAULT '0' ); ";
//
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
//
//    /************************************************************/
//    /*********************OPEN/CLOSE METHODS*********************/
//    /************************************************************/
//
//    public MySQLiteHelper(Context context) {
//        this.context = context;
//        myDbHelperForMessages = new DatabaseHelperMessages(context);
//        myDbHelperForContacts = new DatabaseHelperContacts(context);
//        myDbHelperForUser = new DatabaseHelperUser(context);
//        myDbHelperForStatistics = new DatabaseHelperStatistics(context);
//
//        openMessagesTable();
//        closeMessagesTable();
//        openContactsTable();
//        closeContactsTable();
//        openUserTable();
//        closeUserTable();
//        openStatisticsTable();
//        closeStatisticsTable();
//    }
//
//    public MySQLiteHelper openMessagesTable() throws SQLException {
//        myDb = myDbHelperForMessages.getWritableDatabase();
//        return this;
//    }
//
//    public void closeMessagesTable() {
//        if (myDbHelperForMessages != null) { myDbHelperForMessages.close(); }
//    }
//
//    public MySQLiteHelper openContactsTable() throws SQLException {
//        myDb = myDbHelperForContacts.getWritableDatabase();
//        return this;
//    }
//
//    public void closeContactsTable() {
//        if (myDbHelperForContacts != null) { myDbHelperForContacts.close(); }
//    }
//
//    public MySQLiteHelper openUserTable() throws SQLException {
//        myDb = myDbHelperForUser.getWritableDatabase();
//        return this;
//    }
//
//    public void closeUserTable() {
//        if (myDbHelperForUser != null) { myDbHelperForUser.close(); }
//    }
//
//    public MySQLiteHelper openStatisticsTable() throws SQLException {
//        myDb = myDbHelperForStatistics.getWritableDatabase();
//        return this;
//    }
//
//    public void closeStatisticsTable() {
//        if (myDbHelperForStatistics != null) { myDbHelperForStatistics.close(); }
//    }
//
//    public boolean deleteDatabase(){
//        context.deleteDatabase(DATABASE_NAME1);
//        context.deleteDatabase(DATABASE_NAME2);
//        context.deleteDatabase(DATABASE_NAME3);
//        context.deleteDatabase(DATABASE_NAME4);
//        return true;
//    }
//
//    /* ******************************************* */
//    /* ******************************************* */
//    /* ******************************************* */
//    /* ******************************************* */
//    /* ******************************************* */
//    /* ******************************************* */
//    /* ******************************************* */
//    /* ******************************************* */
//    /* ******************************************* */
//
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
//    /************************************************************/
//    /*******************CREATE/UPGRADE METHODS*******************/
//    /************************************************************/
//
//    private static class DatabaseHelperMessages extends SQLiteOpenHelper {
//
//        DatabaseHelperMessages(Context context) {
//            super(context, DATABASE_NAME1, null, DATABASE_VERSION);
//        }
//
//        @Override
//        public void onCreate(SQLiteDatabase db) { db.execSQL(TABLE_MESSAGES_CREATE); }
//
//        @Override
//        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            Logger.error(TAG, "Upgrading database MESSAGES from version " + oldVersion + " to "
//                    + newVersion + ", which will destroy all old data");
//            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES_CREATE);
//            onCreate(db);
//        }
//    }
//
//    private static class DatabaseHelperContacts extends SQLiteOpenHelper {
//
//        DatabaseHelperContacts(Context context) {
//            super(context, DATABASE_NAME2, null, DATABASE_VERSION);
//        }
//
//        @Override
//        public void onCreate(SQLiteDatabase db) { db.execSQL(TABLE_CONTACTS_CREATE); }
//
//        @Override
//        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            Logger.error(TAG, "Upgrading database CONTACTS from version " + oldVersion + " to "
//                    + newVersion + ", which will destroy all old data");
//            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS_CREATE);
//            onCreate(db);
//        }
//    }
//
//
//    private static class DatabaseHelperUser extends SQLiteOpenHelper {
//
//        DatabaseHelperUser(Context context) {
//            super(context, DATABASE_NAME3, null, DATABASE_VERSION);
//        }
//
//        @Override
//        public void onCreate(SQLiteDatabase db) { db.execSQL(TABLE_USER_CREATE); }
//
//        @Override
//        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            Logger.error(TAG, "Upgrading database USER from version " + oldVersion + " to "
//                    + newVersion + ", which will destroy all old data");
//            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_CREATE);
//            onCreate(db);
//        }
//    }
//
//    private static class DatabaseHelperStatistics extends SQLiteOpenHelper {
//
//        DatabaseHelperStatistics(Context context) {
//            super(context, DATABASE_NAME4, null, DATABASE_VERSION);
//        }
//
//        @Override
//        public void onCreate(SQLiteDatabase db) { db.execSQL(TABLE_STATISTICS_CREATE); }
//
//        @Override
//        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            Logger.error(TAG, "Upgrading database USER from version " + oldVersion + " to "
//                    + newVersion + ", which will destroy all old data");
//            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATISTICS_CREATE);
//            onCreate(db);
//        }
//    }