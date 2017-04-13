package com.deakishin.weatherapp.model.localdb.impl;

import android.provider.BaseColumns;

/**
 * Контракт для работы с БД.
 */
class LocalDbContract {
    public LocalDbContract() {
    }

    /**
     * Контракт для таблицы с текущей погодой для каждого города.
     */
    static abstract class Weathers implements BaseColumns {
        static final String TABLE_NAME = "weathers";
        static final String COLUMN_NAME_CITY_ID = "city_id";
        static final String COLUMN_NAME_CITY_NAME = "city";
        static final String COLUMN_NAME_COUNTRY = "country";
        static final String COLUMN_NAME_TEMP = "temp";
        static final String COLUMN_NAME_WIND = "wind";
        static final String COLUMN_NAME_CLOUDS = "clouds";
        static final String COLUMN_NAME_HUMIDITY = "humidity";
        static final String COLUMN_NAME_PRESSURE = "pressure";
        static final String COLUMN_NAME_ICON_ID = "icon_id";

        static final String COLUMN_NAME_IS_FORECAST = "forecast";
        static final String COLUMN_NAME_DATE = "date";
    }

    /**
     * Контракт для таблицы с метаданными о данных:
     * последнее обновление данных, их идентификатор и флаг обновления.
     */
    static abstract class Meta implements BaseColumns {
        static final String TABLE_NAME = "meta";
        static final String COLUMN_DATA_ID = "data_id";
        static final String COLUMN_REFRESHING = "refreshing";
        static final String COLUMN_LAST_UPDATE = "last_update";
    }
}
