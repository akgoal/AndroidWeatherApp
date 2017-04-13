package com.deakishin.weatherapp.model.localdb.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Помощник для открытия и базы данных и доступа к ней.
 */
class LocalDbHelper extends SQLiteOpenHelper {

    private final String TAG = getClass().getSimpleName();

    // Версия БД.
    static final int DATABASE_VERSION = 7;
    // Название БД.
    static final String DATABASE_NAME = "LocalDbImpl.db";

    // Идентификаторы городов по умолчанию.
    private static final int[] DEFAULT_IDS = {5601538, 498817};
    // Названия городов по умолчанию.
    private static final String[] DEFAULT_CITY_NAMES = {"Moscow", "Saint Petersburg"};

    // Идентификатор данных по текущей погоде.
    static final int CURRENT_WEATHER_DATA_ID = -1;

    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_WEATHERS =
            "CREATE TABLE " + LocalDbContract.Weathers.TABLE_NAME + " (" +
                    LocalDbContract.Weathers._ID + INT_TYPE + " PRIMARY KEY," +
                    LocalDbContract.Weathers.COLUMN_NAME_CITY_ID + INT_TYPE + COMMA_SEP +
                    LocalDbContract.Weathers.COLUMN_NAME_CITY_NAME + TEXT_TYPE + COMMA_SEP +
                    LocalDbContract.Weathers.COLUMN_NAME_COUNTRY + TEXT_TYPE + COMMA_SEP +
                    LocalDbContract.Weathers.COLUMN_NAME_TEMP + REAL_TYPE + COMMA_SEP +
                    LocalDbContract.Weathers.COLUMN_NAME_CLOUDS + REAL_TYPE + COMMA_SEP +
                    LocalDbContract.Weathers.COLUMN_NAME_HUMIDITY + REAL_TYPE + COMMA_SEP +
                    LocalDbContract.Weathers.COLUMN_NAME_PRESSURE + REAL_TYPE + COMMA_SEP +
                    LocalDbContract.Weathers.COLUMN_NAME_WIND + REAL_TYPE + COMMA_SEP +
                    LocalDbContract.Weathers.COLUMN_NAME_ICON_ID + TEXT_TYPE + COMMA_SEP +
                    LocalDbContract.Weathers.COLUMN_NAME_IS_FORECAST + INT_TYPE + COMMA_SEP +
                    LocalDbContract.Weathers.COLUMN_NAME_DATE + INT_TYPE + " )";
    private static final String SQL_CREATE_META =
            "CREATE TABLE " + LocalDbContract.Meta.TABLE_NAME + " (" +
                    LocalDbContract.Meta._ID + INT_TYPE + " PRIMARY KEY," +
                    LocalDbContract.Meta.COLUMN_DATA_ID + INT_TYPE + COMMA_SEP +
                    LocalDbContract.Meta.COLUMN_LAST_UPDATE + INT_TYPE + COMMA_SEP +
                    LocalDbContract.Meta.COLUMN_REFRESHING + INT_TYPE + " )";

    private static final String SQL_DELETE_WEATHERS =
            "DROP TABLE IF EXISTS " + LocalDbContract.Weathers.TABLE_NAME;
    private static final String SQL_DELETE_META =
            "DROP TABLE IF EXISTS " + LocalDbContract.Meta.TABLE_NAME;

    LocalDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Creating local database");
        db.execSQL(SQL_CREATE_WEATHERS);

        db.execSQL(SQL_CREATE_META);

        for (int i = 0; i < DEFAULT_IDS.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put(LocalDbContract.Weathers.COLUMN_NAME_CITY_ID, DEFAULT_IDS[i]);
            if (i < DEFAULT_CITY_NAMES.length) {
                cv.put(LocalDbContract.Weathers.COLUMN_NAME_CITY_NAME, DEFAULT_CITY_NAMES[i]);
            }
            cv.put(LocalDbContract.Weathers.COLUMN_NAME_IS_FORECAST, false);
            db.insert(LocalDbContract.Weathers.TABLE_NAME, null, cv);

            ContentValues cvMeta = new ContentValues();
            cvMeta.put(LocalDbContract.Meta.COLUMN_DATA_ID, DEFAULT_IDS[i]);
            cvMeta.put(LocalDbContract.Meta.COLUMN_REFRESHING, 0);
            db.insert(LocalDbContract.Meta.TABLE_NAME, null, cvMeta);
        }

        ContentValues cv = new ContentValues();
        cv.put(LocalDbContract.Meta.COLUMN_DATA_ID, CURRENT_WEATHER_DATA_ID);
        cv.put(LocalDbContract.Meta.COLUMN_REFRESHING, 0);
        db.insert(LocalDbContract.Meta.TABLE_NAME, null, cv);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_WEATHERS);
        db.execSQL(SQL_DELETE_META);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}