package com.deakishin.weatherapp.model.rest;

import android.content.Context;
import android.util.Log;

import com.deakishin.weatherapp.model.entities.DataStatus;
import com.deakishin.weatherapp.model.entities.WeatherData;
import com.deakishin.weatherapp.model.localdb.LocalDb;
import com.deakishin.weatherapp.model.localdb.impl.LocalDbImpl;
import com.deakishin.weatherapp.model.rest.restclient.SyncRestClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Класс для выполнения Rest-методов и записи результатов в локальную БД.
 * Все операции выполняются синхронно, поэтому подразумевается, что работа объекта класса выведена в отдельный поток.
 */
public class RestProcessor {

    private final String TAG = getClass().getSimpleName();

    // Объект для выполнения Rest-методов.
    private SyncRestClient mRestApi;
    // Объект для работы с локальной БД.
    private LocalDb mLocalDb;

    public RestProcessor(Context context) {
        mLocalDb = LocalDbImpl.getInstance(context.getApplicationContext());
        mRestApi = new SyncRestClient();
    }

    /**
     * Обновляет все данные по текущей погоде.
     *
     * @return True, если операция проведена успешно, false в противном случае.
     */
    public boolean refreshAllWeathers() {
        mLocalDb.updateCurrentWeatherDataStatus(new DataStatus(true));

        List<Integer> ids = mLocalDb.getCitiesIds();

        final List<WeatherData> items = new ArrayList<>();

        for (int id : ids) {
            mRestApi.getCurrentWeather(id, new SyncRestClient.ResponseHandler<WeatherData>() {
                @Override
                public void onSuccess(WeatherData data) {
                    Log.i(TAG, "Received weather object from Rest client: " + data.toString());
                    items.add(data);
                }

                @Override
                public void onError() {
                }
            });
        }

        boolean success;
        DataStatus dataStatus = new DataStatus(false);
        if (items.size() == ids.size()) {
            mLocalDb.updateCurrentWeatherData(items);
            success = true;
            dataStatus.setLastUpdate(new Date());
        } else {
            success = false;
        }
        mLocalDb.updateCurrentWeatherDataStatus(dataStatus);
        return success;
    }

    /**
     * Обновляет прогноз погоды для конкретного города.
     *
     * @param cityId Идентификатор города.
     * @return True, если операция проведена успешно, false в противном случае.
     */
    public boolean refreshForecast(int cityId) {
        mLocalDb.updateForecastDataStatus(new DataStatus(true), cityId);

        final List<WeatherData> forecastList = new ArrayList<>();
        mRestApi.getForecast(cityId, new SyncRestClient.ResponseHandler<List<WeatherData>>() {
            @Override
            public void onSuccess(List<WeatherData> dataList) {
                Log.i(TAG, "Received forecast list from Rest client: " + dataList.toString());
                forecastList.addAll(dataList);
            }

            @Override
            public void onError() {
                forecastList.clear();
            }
        });

        boolean success;
        DataStatus dataStatus = new DataStatus(false);
        if (!forecastList.isEmpty()) {
            mLocalDb.updateWeatherForecast(cityId, forecastList);
            success = true;
            dataStatus.setLastUpdate(new Date());
        } else {
            success = false;
        }
        mLocalDb.updateForecastDataStatus(dataStatus, cityId);
        return success;
    }

    /**
     * Добавляет новый город по его названию и обновляет его текущую погоду.
     *
     * @param cityName Название города.
     * @return Отрицательное число в случае ошибки, положительное число в случае успеха,
     * ноль в случае, если город с таким названием не найден.
     */
    public int addCityWithCurrentWeather(String cityName) {

        final List<WeatherData> dataList = new ArrayList<>();
        mRestApi.getCurrentWeatherByName(cityName, new SyncRestClient.ResponseHandler<WeatherData>() {
            @Override
            public void onSuccess(WeatherData data) {
                if (data == null) {
                    WeatherData emptyData = new WeatherData();
                    dataList.add(emptyData);
                } else {
                    dataList.add(data);
                }
            }

            @Override
            public void onError() {
                dataList.clear();
            }
        });

        int res;
        if (dataList.isEmpty()) {
            // Ошибка во время выполнения.
            res = -1;
        } else {
            WeatherData data = dataList.get(0);
            if (data.getCityName() == null) {
                // Город не найден.
                res = 0;
            } else {
                // Успех.
                res = 1;

                mLocalDb.addCityWithCurrentWeather(data);
            }
        }
        return res;
    }
}
