package com.deakishin.weatherapp.model.services;

import android.content.Context;
import android.content.Intent;

import com.deakishin.weatherapp.model.rest.RestMethodsContract;

/**
 * Класс-помощник для запуска службы для выполнения фоновых операций,
 * связанных с вызовом REST-методов и записью результатов в БД.
 */
public class RestServiceHelper {

    private Context mContext;
    private String mResultAction;

    /**
     * Создает помощника.
     *
     * @param context      Контекст приложения.
     * @param resultAction Action для интента, который будет рассылаться
     *                     как широковещательное сообщение по завершении операции.
     */
    public RestServiceHelper(Context context, String resultAction) {
        mContext = context.getApplicationContext();
        mResultAction = resultAction;
    }

    /**
     * Обновляет данные в БД по текущей погоде с сервера.
     */
    public void refreshAllWeathers() {
        runService(RestMethodsContract.Methods.REFRESH_ALL_WEATHERS);
    }

    /**
     * Обновляет данные в БД по прогнозу погоды с сервера для конкретного города.
     *
     * @param cityId Идентификатор города.
     */
    public void refreshForecast(int cityId) {
        Intent intent = new Intent(mContext, RestService.class);
        intent.putExtra(RestService.Extras.METHOD_EXTRA, RestMethodsContract.Methods.REFRESH_FORECAST);
        intent.putExtra(RestService.Extras.RESULT_ACTION_EXTRA, mResultAction);
        intent.putExtra(RestService.Extras.CITY_ID_EXTRA, cityId);
        mContext.startService(intent);
    }

    /**
     * Добавляет новый город в базу по его названию.
     * Также в случае успеха сразу обновляется текущая погода этого города.
     *
     * @param cityName Название города.
     */
    public void addCity(String cityName) {
        Intent intent = new Intent(mContext, RestService.class);
        intent.putExtra(RestService.Extras.METHOD_EXTRA, RestMethodsContract.Methods.ADD_CITY);
        intent.putExtra(RestService.Extras.RESULT_ACTION_EXTRA, mResultAction);
        intent.putExtra(RestService.Extras.CITY_NAME_EXTRA, cityName);
        mContext.startService(intent);
    }

    /**
     * Запускает службу для выполнения конкретного метода.
     *
     * @param methodId Идентификатор REST-метода.
     */
    public void runService(int methodId) {
        Intent intent = new Intent(mContext, RestService.class);
        intent.putExtra(RestService.Extras.METHOD_EXTRA, methodId);
        intent.putExtra(RestService.Extras.RESULT_ACTION_EXTRA, mResultAction);
        mContext.startService(intent);
    }
}
