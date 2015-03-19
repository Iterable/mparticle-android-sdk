package com.mparticle.internal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Class that generates/provides and interface to the mParticle database
 * that's responsible for storing and upload messages.
 *
 * The general flow is that a message is logged the MessageTable. The MessageTable is then
 * processed and purged to create batches of messages to be uploaded, which are then inserted in the UploadTable.
 * Finally, as we successfully upload batches, we remove them from the UploadTable
 *
 */
/* package-private */class MParticleDatabase extends SQLiteOpenHelper {

    private static final int DB_VERSION = 3;
    public static final String DB_NAME = "mparticle.db";

    interface BreadcrumbTable {
        String TABLE_NAME = "breadcrumbs";
        String SESSION_ID = "session_id";
        String API_KEY = "api_key";
        String MESSAGE = "message";
        String CREATED_AT = "breadcrumb_time";
        String CF_UUID = "cfuuid";
    }

    private static final String CREATE_BREADCRUMBS_DDL =
            "CREATE TABLE IF NOT EXISTS " + BreadcrumbTable.TABLE_NAME + " (" + BaseColumns._ID +
                    " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    BreadcrumbTable.SESSION_ID + " STRING NOT NULL, " +
                    BreadcrumbTable.API_KEY + " STRING NOT NULL, " +
                    BreadcrumbTable.MESSAGE + " TEXT, " +
                    BreadcrumbTable.CREATED_AT + " INTEGER NOT NULL, " +
                    BreadcrumbTable.CF_UUID + " TEXT" +
                    ");";

    interface SessionTable {
        String TABLE_NAME = "sessions";
        String SESSION_ID = "session_id";
        String API_KEY = "api_key";
        String START_TIME = "start_time";
        String END_TIME = "end_time";
        String SESSION_FOREGROUND_LENGTH = "session_length";
        String ATTRIBUTES = "attributes";
        String CF_UUID = "cfuuid";
    }

    private static final String CREATE_SESSIONS_DDL =
            "CREATE TABLE IF NOT EXISTS " + SessionTable.TABLE_NAME + " (" + BaseColumns._ID +
                    " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SessionTable.SESSION_ID + " STRING NOT NULL, " +
                    SessionTable.API_KEY + " STRING NOT NULL, " +
                    SessionTable.START_TIME + " INTEGER NOT NULL," +
                    SessionTable.END_TIME + " INTEGER NOT NULL," +
                    SessionTable.SESSION_FOREGROUND_LENGTH + " INTEGER NOT NULL," +
                    SessionTable.ATTRIBUTES + " TEXT, " +
                    SessionTable.CF_UUID + " TEXT" +
                    ");";


    interface MessageTable {
        String TABLE_NAME = "messages";
        String SESSION_ID = "session_id";
        String API_KEY = "api_key";
        String MESSAGE = "message";
        String STATUS = "upload_status";
        String CREATED_AT = "message_time";
        String MESSAGE_TYPE = "message_type";
        String CF_UUID = "cfuuid";
    }

    private static final String CREATE_MESSAGES_DDL =
            "CREATE TABLE IF NOT EXISTS " + MessageTable.TABLE_NAME + " (" + BaseColumns._ID +
                    " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MessageTable.SESSION_ID + " STRING NOT NULL, " +
                    MessageTable.API_KEY + " STRING NOT NULL, " +
                    MessageTable.MESSAGE + " TEXT, " +
                    MessageTable.STATUS + " INTEGER, " +
                    MessageTable.CREATED_AT + " INTEGER NOT NULL, " +
                    MessageTable.MESSAGE_TYPE + " TEXT, " +
                    MessageTable.CF_UUID + " TEXT" +
                    ");";

    interface UploadTable {
        String TABLE_NAME = "uploads";
        String API_KEY = "api_key";
        String MESSAGE = "message";
        String CREATED_AT = "message_time";
        String CF_UUID = "cfuuid";
        String SESSION_ID = "session_id";
    }

    private static final String CREATE_UPLOADS_DDL =
            "CREATE TABLE IF NOT EXISTS " + UploadTable.TABLE_NAME + " (" + BaseColumns._ID +
                    " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UploadTable.API_KEY + " STRING NOT NULL, " +
                    UploadTable.MESSAGE + " TEXT, " +
                    UploadTable.CREATED_AT + " INTEGER NOT NULL, " +
                    UploadTable.CF_UUID + " TEXT, " +
                    UploadTable.SESSION_ID + " TEXT" +
                    ");";

    interface CommandTable {
        String TABLE_NAME = "commands";
        String URL = "url";
        String METHOD = "method";
        String POST_DATA = "post_data";
        String HEADERS = "headers";
        String CREATED_AT = "timestamp";
        String SESSION_ID = "session_id";
        String API_KEY = "api_key";
        String CF_UUID = "cfuuid";
    }

    private static final String CREATE_COMMANDS_DDL =
            "CREATE TABLE IF NOT EXISTS " + CommandTable.TABLE_NAME + " (" + BaseColumns._ID +
                    " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CommandTable.URL + " STRING NOT NULL, " +
                    CommandTable.METHOD + " STRING NOT NULL, " +
                    CommandTable.POST_DATA + " TEXT, " +
                    CommandTable.HEADERS + " TEXT, " +
                    CommandTable.CREATED_AT + " INTEGER, " +
                    CommandTable.SESSION_ID + " TEXT, " +
                    CommandTable.API_KEY + " STRING NOT NULL, " +
                    CommandTable.CF_UUID + " TEXT" +
                    ");";

    interface GcmMessageTable {
        String CONTENT_ID = "content_id";
        String CAMPAIGN_ID = "campaign_id";
        String TABLE_NAME = "gcm_messages";
        String PAYLOAD = "payload";
        String CREATED_AT = "message_time";
        String DISPLAYED_AT = "displayed_time";
        String EXPIRATION = "expiration";
        String BEHAVIOR = "behavior";
        String APPSTATE = "appstate";
        int PROVIDER_CONTENT_ID = -1;
    }

    private static final String CREATE_GCM_MSG_DDL =
            "CREATE TABLE IF NOT EXISTS " + GcmMessageTable.TABLE_NAME + " (" + GcmMessageTable.CONTENT_ID +
                    " INTEGER PRIMARY KEY, " +
                    GcmMessageTable.PAYLOAD + " TEXT NOT NULL, " +
                    GcmMessageTable.APPSTATE + " TEXT NOT NULL, " +
                    GcmMessageTable.CREATED_AT + " INTEGER NOT NULL, " +
                    GcmMessageTable.EXPIRATION + " INTEGER NOT NULL, " +
                    GcmMessageTable.BEHAVIOR + " INTEGER NOT NULL," +
                    GcmMessageTable.CAMPAIGN_ID + " TEXT NOT NULL, " +
                    GcmMessageTable.DISPLAYED_AT + " INTEGER NOT NULL" +
                    ");";

    MParticleDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SESSIONS_DDL);
        db.execSQL(CREATE_MESSAGES_DDL);
        db.execSQL(CREATE_UPLOADS_DDL);
        db.execSQL(CREATE_COMMANDS_DDL);
        db.execSQL(CREATE_BREADCRUMBS_DDL);
        db.execSQL(CREATE_GCM_MSG_DDL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //so far upgrades have only been additive, so just make sure we've got all the tables.
        db.execSQL(CREATE_SESSIONS_DDL);
        db.execSQL(CREATE_MESSAGES_DDL);
        db.execSQL(CREATE_UPLOADS_DDL);
        db.execSQL(CREATE_COMMANDS_DDL);
        db.execSQL(CREATE_BREADCRUMBS_DDL);
        db.execSQL(CREATE_GCM_MSG_DDL);
    }
}
