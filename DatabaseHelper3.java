package com.example.bigproject3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper3 extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "app_database.db";
    private static final int DATABASE_VERSION = 1;

    // User table and columns
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_LEVEL = "level";

    // Login record table and columns
    private static final String TABLE_RECORDS = "records";
    private static final String COLUMN_RECORD_ID = "record_id";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_USER = "user";

    public DatabaseHelper3(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTableQuery = "CREATE TABLE " + TABLE_USERS + "(" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_LEVEL + " INTEGER)";
        db.execSQL(createUserTableQuery);

        String createRecordTableQuery = "CREATE TABLE " + TABLE_RECORDS + "(" +
                COLUMN_RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TIME + " TEXT, " +
                COLUMN_USER + " TEXT)";
        db.execSQL(createRecordTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDS);
        onCreate(db);
    }

    public void insertUser(String username, String password, int level) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_LEVEL, level);
        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    public boolean checkUsernameExist(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USERNAME}, COLUMN_USERNAME + " = ?",
                new String[]{username}, null, null, null);
        boolean usernameExists = cursor.moveToFirst();
        cursor.close();
        return usernameExists;
    }

    public int getUserLevel1(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_LEVEL}, COLUMN_USERNAME + " = ?",
                new String[]{username}, null, null, null);
        int level = 0;
        if (cursor.moveToFirst()) {
            level = cursor.getInt(cursor.getColumnIndex(COLUMN_LEVEL));
        }
        cursor.close();
        return level;
    }

    public void deleteUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, COLUMN_USERNAME + " = ?", new String[]{username});
        db.close();
    }

    public void insertRecord(String time, String user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_USER, user);
        db.insert(TABLE_RECORDS, null, values);
        db.close();
    }

    public Cursor getAllRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_RECORDS, null, null, null, null, null, null);
    }

    public void deleteAllRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RECORDS, null, null);
        db.close();
    }

    public String getFirstUsername() {
        SQLiteDatabase db = this.getReadableDatabase();
        String username = "";

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_USERNAME},
                null,
                null,
                null,
                null,
                COLUMN_USER_ID + " ASC",
                "1"
        );

        if (cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
        }

        cursor.close();
        return username;
    }

    public boolean checkCredentials(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};
        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
        boolean match = cursor.getCount() > 0;
        cursor.close();
        return match;
    }

    public String getUserLevel(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
        String level = "0";
        if (cursor.moveToFirst()) {
            level = cursor.getString(cursor.getColumnIndex(COLUMN_LEVEL));
        }
        cursor.close();
        return level;
    }
}
