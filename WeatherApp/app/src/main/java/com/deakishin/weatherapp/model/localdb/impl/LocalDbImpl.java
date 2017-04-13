package com.deakishin.weatherapp.model.localdb.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.deakishin.weatherapp.model.entities.DataStatus;
import com.deakishin.weatherapp.model.entities.WeatherData;
import com.deakishin.weatherapp.model.localdb.LocalDb;
import com.deakishin.weatherapp.model.localdb.QueryListResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Синглетон-класс локальной базы данных.
 */
public class LocalDbImpl implements LocalDb {

    private final String TAG = getClass().getSimpleName();

    // Помощник для работы с БД.
    private LocalDbHelper mDbHelper;
    // Объект для выполнения запросов к БД.
    private SQLiteDatabase mSQLiteDatabase;

    private static LocalDbImpl sLocalDb;

    /**
     * @param context Контекст приложения.
     * @return Объект для работы с локальной базой данных.
     */
    public static LocalDbImpl getInstance(Context context) {
        if (sLocalDb == null) {
            sLocalDb = new LocalDbImpl(context);
        }
        return sLocalDb;
    }

    private LocalDbImpl(Context context) {
        mDbHelper = new LocalDbHelper(context.getApplicationContext());

        mSQLiteDatabase = mDbHelper.getWritableDatabase();
    }

    @Override
    public QueryListResult<WeatherData> getCurrentWeatherData() {
        return getWeathers(false, 0);
    }

    // Возвращает записи по погоде. Если forecast, то возвращаются данные по текущей погоде,
    // иначе - по прогнозу погоды для города с идентификатором cityId.
    private QueryListResult<WeatherData> getWeathers(boolean forecast, int cityId) {
        Log.i(TAG, "Executing query for weather data. Forecast: " + forecast + ". CityId: " + cityId);

        QueryListResult<WeatherData> result = new QueryListResult<>();

        List<WeatherData> dataList = new ArrayList<>();

        SQLiteDatabase db = mSQLiteDatabase;

        String[] projection = {
                LocalDbContract.Weathers.COLUMN_NAME_CITY_ID,
                LocalDbContract.Weathers.COLUMN_NAME_CITY_NAME,
                LocalDbContract.Weathers.COLUMN_NAME_COUNTRY,
                LocalDbContract.Weathers.COLUMN_NAME_TEMP,
                LocalDbContract.Weathers.COLUMN_NAME_HUMIDITY,
                LocalDbContract.Weathers.COLUMN_NAME_PRESSURE,
                LocalDbContract.Weathers.COLUMN_NAME_WIND,
                LocalDbContract.Weathers.COLUMN_NAME_CLOUDS,
                LocalDbContract.Weathers.COLUMN_NAME_ICON_ID,
                LocalDbContract.Weathers.COLUMN_NAME_DATE
        };

        String selection = null;
        String[] selectionArgs;
        String orderBy = null;
        if (forecast) {
            selection = LocalDbContract.Weathers.COLUMN_NAME_IS_FORECAST + " = ? and "
                    + LocalDbContract.Weathers.COLUMN_NAME_CITY_ID + " = ?";
            selectionArgs = new String[]{"1", "" + cityId};
            orderBy = LocalDbContract.Weathers.COLUMN_NAME_DATE + " ASC";
        } else {
            selection = LocalDbContract.Weathers.COLUMN_NAME_IS_FORECAST + " = ?";
            selectionArgs = new String[]{"0"};
        }

        Cursor c = db.query(LocalDbContract.Weathers.TABLE_NAME, projection, selection, selectionArgs, null, null, orderBy);
        int idIdx = c.getColumnIndex(LocalDbContract.Weathers.COLUMN_NAME_CITY_ID);
        int cityIdx = c.getColumnIndex(LocalDbContract.Weathers.COLUMN_NAME_CITY_NAME);
        int countryIdx = c.getColumnIndex(LocalDbContract.Weathers.COLUMN_NAME_COUNTRY);
        int tempIdx = c.getColumnIndex(LocalDbContract.Weathers.COLUMN_NAME_TEMP);
        int humidIdx = c.getColumnIndex(LocalDbContract.Weathers.COLUMN_NAME_HUMIDITY);
        int pressIdx = c.getColumnIndex(LocalDbContract.Weathers.COLUMN_NAME_PRESSURE);
        int windIdx = c.getColumnIndex(LocalDbContract.Weathers.COLUMN_NAME_WIND);
        int cloudsIdx = c.getColumnIndex(LocalDbContract.Weathers.COLUMN_NAME_CLOUDS);
        int iconIdx = c.getColumnIndex(LocalDbContract.Weathers.COLUMN_NAME_ICON_ID);
        int dateIdx = c.getColumnIndex(LocalDbContract.Weathers.COLUMN_NAME_DATE);

        while (c.moveToNext()) {
            WeatherData data = new WeatherData();
            data.setId(c.isNull(idIdx) ? -1 : c.getInt(idIdx));
            data.setCityName(c.getString(cityIdx));
            data.setCountry(c.getString(countryIdx));
            data.setTemp(getDoubleValue(c, tempIdx));
            data.setHumidity(getDoubleValue(c, humidIdx));
            data.setPressure(getDoubleValue(c, pressIdx));
            data.setWind(getDoubleValue(c, windIdx));
            data.setClouds(getDoubleValue(c, cloudsIdx));
            data.setWeatherIconId(c.getString(iconIdx));
            if (forecast) {
                data.setDate(c.isNull(dateIdx) ? null : new Date(c.getLong(dateIdx)));
            }
            dataList.add(data);
        }

        c.close();

        result.setData(dataList);
        result.setDataStatus(getDataStatus(forecast, cityId));

        Log.i(TAG, "Returning result for query for weather data. Forecast: " + forecast
                + ". CityId: " + cityId + ". Data size: " + dataList.size());
        return result;
    }

    // Возвращает значение типа Double или null из курсора c по индексу index.
    private Double getDoubleValue(Cursor c, int index) {
        return c.isNull(index) ? null : c.getDouble(index);
    }

    // Возвращает статус данных. forecast - данные по прогнозу погоде, cityId - идентификатор города,
    // если данные по прогнозу погоды.
    private DataStatus getDataStatus(boolean forecast, int cityId) {
        SQLiteDatabase db = mSQLiteDatabase;

        String[] projection = {
                LocalDbContract.Meta.COLUMN_REFRESHING, LocalDbContract.Meta.COLUMN_LAST_UPDATE
        };

        String[] selectionArgs = new String[]{"" + getDataId(forecast, cityId)};

        Cursor c = db.query(LocalDbContract.Meta.TABLE_NAME, projection, LocalDbContract.Meta.COLUMN_DATA_ID + " = ?",
                selectionArgs, null, null, null);
        int refreshIdx = c.getColumnIndex(LocalDbContract.Meta.COLUMN_REFRESHING);
        int lastUpdIdx = c.getColumnIndex(LocalDbContract.Meta.COLUMN_LAST_UPDATE);

        DataStatus dataStatus = null;
        if (c.moveToFirst()) {
            dataStatus = new DataStatus();
            dataStatus.setRefreshing(!c.isNull(refreshIdx) && c.getInt(refreshIdx) > 0);
            dataStatus.setLastUpdate(c.isNull(lastUpdIdx) ? null : new Date(c.getLong(lastUpdIdx)));
        }

        c.close();
        return dataStatus;
    }

    @Override
    public List<Integer> getCitiesIds() {
        List<Integer> ids = new ArrayList<>();

        SQLiteDatabase db = mSQLiteDatabase;

        String[] projection = {
                LocalDbContract.Weathers.COLUMN_NAME_CITY_ID,
        };

        String selection = LocalDbContract.Weathers.COLUMN_NAME_IS_FORECAST + " = ?";
        String[] selectionArgs = new String[]{"" + 0};

        Cursor c = db.query(LocalDbContract.Weathers.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        int idIdx = c.getColumnIndex(LocalDbContract.Weathers.COLUMN_NAME_CITY_ID);

        while (c.moveToNext()) {
            if (!c.isNull(idIdx)) {
                ids.add(c.getInt(idIdx));
            }
        }

        c.close();

        return ids;
    }

    @Override
    public void updateCurrentWeatherData(List<WeatherData> dataList) {
        updateWeatherData(dataList, false, 0);
    }

    // Обновляет данные по погоде из списка. forecast - данные по прогнозу погоды для города с cityId,
    // иначе, данные по текущей погоде.
    private void updateWeatherData(List<WeatherData> dataList, boolean forecast, int cityId) {
        SQLiteDatabase db = mSQLiteDatabase;

        String selection = LocalDbContract.Weathers.COLUMN_NAME_CITY_ID + " = ? and "
                + LocalDbContract.Weathers.COLUMN_NAME_IS_FORECAST + " = ?";

        db.beginTransaction();

        if (forecast) {
            db.delete(LocalDbContract.Weathers.TABLE_NAME, selection, new String[]{"" + cityId, "" + 1});
        }

        for (WeatherData item : dataList) {
            ContentValues values = convertToContentValues(item, forecast);
            if (forecast) {
               // values.put(LocalDbContract.Weathers.COLUMN_NAME_CITY_ID, item.getId());
                db.insert(LocalDbContract.Weathers.TABLE_NAME, null, values);
            } else {
                String[] selectionArgs = {String.valueOf(item.getId()), String.valueOf(0)};
                db.update(
                        LocalDbContract.Weathers.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    // Конвертирует данные о погоде в данные для БД. forecast - данные по прогнозу погоды,
    // иначе по текущей погоде.
    private ContentValues convertToContentValues(WeatherData item, boolean forecast){
        ContentValues values = new ContentValues();
        values.put(LocalDbContract.Weathers.COLUMN_NAME_CITY_NAME, item.getCityName());
        values.put(LocalDbContract.Weathers.COLUMN_NAME_COUNTRY, item.getCountry());
        values.put(LocalDbContract.Weathers.COLUMN_NAME_TEMP, item.getTemp());
        values.put(LocalDbContract.Weathers.COLUMN_NAME_HUMIDITY, item.getHumidity());
        values.put(LocalDbContract.Weathers.COLUMN_NAME_WIND, item.getWind());
        values.put(LocalDbContract.Weathers.COLUMN_NAME_PRESSURE, item.getPressure());
        values.put(LocalDbContract.Weathers.COLUMN_NAME_CLOUDS, item.getClouds());
        values.put(LocalDbContract.Weathers.COLUMN_NAME_ICON_ID, item.getWeatherIconId());
        values.put(LocalDbContract.Weathers.COLUMN_NAME_IS_FORECAST, forecast);
        values.put(LocalDbContract.Weathers.COLUMN_NAME_DATE, item.getDate() == null ? null : item.getDate().getTime());
        values.put(LocalDbContract.Weathers.COLUMN_NAME_CITY_ID, item.getId());
        return values;
    }

    @Override
    public DataStatus getCurrentWeatherDataStatus() {
        return getDataStatus(false, 0);
    }

    @Override
    public void updateCurrentWeatherDataStatus(DataStatus status) {
        updateWeatherDataStatus(status, false, 0);
    }

    // Обновляет статус данных по погоде. forecast - данные по прогнозу погоды для города с cityId.
    // Иначе, данные по текущей погоде.
    private void updateWeatherDataStatus(DataStatus status, boolean forecast, int cityId) {
        SQLiteDatabase db = mSQLiteDatabase;

        String selection = LocalDbContract.Meta.COLUMN_DATA_ID + " = ?";
        String[] selectionArgs = {"" + getDataId(forecast, cityId)};

        ContentValues values = new ContentValues();
        values.put(LocalDbContract.Meta.COLUMN_REFRESHING, status.isRefreshing());
        if (status.getLastUpdate() != null) {
            values.put(LocalDbContract.Meta.COLUMN_LAST_UPDATE, status.getLastUpdate().getTime());
        }
        db.update(
                LocalDbContract.Meta.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    // Возвращает идентификатор данных. Если forecast==true, то идентификатор -
    //  id города, по которому данные предоставляют прогноз погоды.
    // Иначе -1.
    private int getDataId(boolean forecast, int cityId) {
        return forecast ? cityId : LocalDbHelper.CURRENT_WEATHER_DATA_ID;
    }

    @Override
    public void updateWeatherForecast(int cityId, List<WeatherData> dataList) {
        Log.i(TAG, "Updating weather forecast. CityId: " + cityId + ". Size: " + dataList.size());
        updateWeatherData(dataList, true, cityId);
    }

    @Override
    public QueryListResult<WeatherData> getWeatherForecast(int cityId) {
        return getWeathers(true, cityId);
    }

    @Override
    public DataStatus getWeatherForecastDataStatus(int cityId) {
        return getDataStatus(true, cityId);
    }

    @Override
    public void updateForecastDataStatus(DataStatus status, int cityId) {
        updateWeatherDataStatus(status, true, cityId);
    }

    @Override
    public void addCityWithCurrentWeather(WeatherData data) {
        SQLiteDatabase db = mSQLiteDatabase;

        db.beginTransaction();
        int id = data.getId(); // Id города.
        if (!getCitiesIds().contains(id)){ // Проверяем, что города еще нет в базе.
            // Добавляем в базу данные о текущей погоде.
            db.insert(LocalDbContract.Weathers.TABLE_NAME, null, convertToContentValues(data, false));

            // Добавляем мета данные о новых данных.
            ContentValues cvMeta = new ContentValues();
            cvMeta.put(LocalDbContract.Meta.COLUMN_DATA_ID, id);
            cvMeta.put(LocalDbContract.Meta.COLUMN_REFRESHING, 0);
            db.insert(LocalDbContract.Meta.TABLE_NAME, null, cvMeta);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }
}
