package com.deakishin.weatherapp.model.rest.restclient;

import android.util.Log;

import com.deakishin.weatherapp.model.entities.WeatherData;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

/**
 * Клиент для выполнения Rest-запросов на сервер.
 * Запросы выполняются синхронно.
 */
public class SyncRestClient {

    private final String TAG = getClass().getSimpleName();

    // Параметры Rest-методов.
    private static class REST_PARAMS {
        static final String WEATHER_ENDPOINT = "weather";
        static final String FORECAST_ENDPOINT = "forecast";
        static final String PARAM_ID = "id";
        static final String PARAM_NAME = "q";
        static final String PARAM_KEY = "APPID";
        static final String PARAM_UNITS = "units";
        static final String UNITS_METRIC = "metric";
        static final String KEY = "1748db036d440fe2a0dd73083d772978";
    }

    /**
     * Интерфейс обработчика ответа на запрос.
     */
    public interface ResponseHandler<T> {
        /**
         * Вызывается в случае успешного выполнения запроса.
         *
         * @param data Полученные с сервера данные.
         */
        void onSuccess(T data);

        /**
         * Вызывается в случае возникновения ошибки во время выполнения запроса.
         */
        void onError();
    }

    /**
     * Конвертер объекта Json в generic.
     */
    private interface JsonConverter<T> {
        T convert(JSONObject json) throws JSONException;
    }

    // Получает данные с сервера по точке входа, запросу и конвертеру ответа.
    // Ответ передается объекту ResponseHandler.
    private <T> void getData(String endpoint, RequestParams rp,
                             final JsonConverter<T> converter, final ResponseHandler<T> responseHandler) {
        HttpUtils.get(endpoint, rp, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.i(TAG, "Received result from REST API as a JSON object. Code=" + statusCode + ". Response:" + response.toString());
                    JSONObject json = new JSONObject(response.toString());
                    responseHandler.onSuccess(converter.convert(json));
                } catch (JSONException e) {
                    Log.e(TAG, "Error processing JSON object: " + e);
                    responseHandler.onError();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray array) {
                Log.i(TAG, "Got result from REST API as a JSON array. Code=" + statusCode + ". Response:" + array.toString());
                if (array.length() > 0)
                    try {
                        onSuccess(statusCode, headers, array.getJSONObject(0));
                    } catch (JSONException e) {
                        Log.e(TAG, "Error processing JSON array: " + e);
                        e.printStackTrace();
                        responseHandler.onError();
                    }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, "Failed to execute REST method. Code: " + statusCode + ". Response: " + errorResponse.toString());
                try {
                    if (errorResponse.getString("message").equals(WEATHER_ERROR_JSON_PARAM_CITY_NOT_FOUND)) {
                        onSuccess(statusCode, headers, errorResponse);
                    } else {
                        responseHandler.onError();
                    }
                } catch (JSONException e) {
                    responseHandler.onError();
                }
            }
        });
    }

    // Сообщение в ошибке, которое означает, что город не найден.
    private static final String WEATHER_ERROR_JSON_PARAM_CITY_NOT_FOUND = "city not found";

    // Параметры для распарсивания текущей погоды из JSON объекта.
    private static class WEATHER_JSON_PARAMS {
        static final String ID = "id";
        static final String CITY = "name";
        static final String WEATHER = "weather";
        static final String WEATHER_ICON = "icon";
        static final String MAIN = "main";
        static final String MAIN_TEMP = "temp";
        static final String MAIN_PRESSURE = "pressure";
        static final String MAIN_HUMIDITY = "humidity";
        static final String WIND = "wind";
        static final String WIND_SPEED = "speed";
        static final String CLOUDS = "clouds";
        static final String CLOUDS_ALL = "all";
        static final String SYS = "sys";
        static final String SYS_COUNTRY = "country";
    }

    /**
     * Выполняет запрос о текущей погоде для конкретного города.
     *
     * @param id              Идентификатор города.
     * @param responseHandler Обработчик ответа на запроса.
     */
    public void getCurrentWeather(long id, final ResponseHandler<WeatherData> responseHandler) {

        RequestParams rp = new RequestParams();
        rp.add(REST_PARAMS.PARAM_ID, Long.toString(id));
        rp.add(REST_PARAMS.PARAM_KEY, REST_PARAMS.KEY);
        rp.add(REST_PARAMS.PARAM_UNITS, REST_PARAMS.UNITS_METRIC);

        getData(REST_PARAMS.WEATHER_ENDPOINT, rp, new CurrentWeatherJsonConverter(), responseHandler);
    }

    /**
     * Конвертер JSON объекта, в котором содержатся данные о текущей погоде.
     */
    private class CurrentWeatherJsonConverter implements JsonConverter<WeatherData> {
        @Override
        public WeatherData convert(JSONObject json) throws JSONException {
            WeatherData data = new WeatherData();

            data.setId(json.getInt(WEATHER_JSON_PARAMS.ID));
            data.setCityName(json.getString(WEATHER_JSON_PARAMS.CITY));

            data.setCountry(json.getJSONObject(WEATHER_JSON_PARAMS.SYS).getString(WEATHER_JSON_PARAMS.SYS_COUNTRY));

            JSONObject mainJson = json.getJSONObject(WEATHER_JSON_PARAMS.MAIN);
            data.setTemp(mainJson.getDouble(WEATHER_JSON_PARAMS.MAIN_TEMP));
            data.setPressure(mainJson.getDouble(WEATHER_JSON_PARAMS.MAIN_PRESSURE));
            data.setHumidity(mainJson.getDouble(WEATHER_JSON_PARAMS.MAIN_HUMIDITY));

            data.setWind(json.getJSONObject(WEATHER_JSON_PARAMS.WIND).getDouble(WEATHER_JSON_PARAMS.WIND_SPEED));

            data.setClouds(json.getJSONObject(WEATHER_JSON_PARAMS.CLOUDS).getDouble(WEATHER_JSON_PARAMS.CLOUDS_ALL));

            data.setWeatherIconId(json.getJSONArray(WEATHER_JSON_PARAMS.WEATHER).getJSONObject(0).getString(WEATHER_JSON_PARAMS.WEATHER_ICON));

            return data;
        }
    }

    ;

    /**
     * Выполняет запрос о текущей погоде для конкретного города по его названию.
     * Если город с таким названием не найден, то возвращает null обработчику ответа.
     *
     * @param cityName        Название  города.
     * @param responseHandler Обработчик ответа на запроса.
     */
    public void getCurrentWeatherByName(String cityName, final ResponseHandler<WeatherData> responseHandler) {

        RequestParams rp = new RequestParams();
        rp.add(REST_PARAMS.PARAM_NAME, cityName);
        rp.add(REST_PARAMS.PARAM_KEY, REST_PARAMS.KEY);
        rp.add(REST_PARAMS.PARAM_UNITS, REST_PARAMS.UNITS_METRIC);

        getData(REST_PARAMS.WEATHER_ENDPOINT, rp, new CurrentWeatherJsonConverter() {
            @Override
            public WeatherData convert(JSONObject json) throws JSONException {
                if (!json.has(WEATHER_JSON_PARAMS.ID)) {
                    return null;
                }
                return super.convert(json);
            }
        }, responseHandler);
    }


    // Параметры для распарсивания прогноза погоды из JSON объекта.
    private static class FORECAST_JSON_PARAMS {
        static final String CITY = "city";
        static final String CITY_ID = "id";
        static final String CITY_NAME = "name";
        static final String CITY_COUNTRY = "country";
        static final String COUNT = "cnt";
        static final String LIST = "list";
        static final String LIST_MAIN = "main";
        static final String LIST_MAIN_TEMP = "temp";
        static final String LIST_MAIN_PRESSURE = "pressure";
        static final String LIST_MAIN_HUMIDITY = "humidity";
        static final String LIST_WIND = "wind";
        static final String LIST_WIND_SPEED = "speed";
        static final String LIST_WEATHER = "weather";
        static final String LIST_WEATHER_ICON = "icon";
        static final String LIST_CLOUDS = "clouds";
        static final String LIST_CLOUDS_ALL = "all";
        static final String LIST_DATE = "dt_txt";
    }

    /**
     * Выполняет запрос о прогнозе погоды для конкретного города для конкретного города.
     *
     * @param id              Идентификатор города.
     * @param responseHandler Обработчик ответа на запроса.
     */
    public void getForecast(long id, final ResponseHandler<List<WeatherData>> responseHandler) {

        RequestParams rp = new RequestParams();
        rp.add(REST_PARAMS.PARAM_ID, Long.toString(id));
        rp.add(REST_PARAMS.PARAM_KEY, REST_PARAMS.KEY);
        rp.add(REST_PARAMS.PARAM_UNITS, REST_PARAMS.UNITS_METRIC);

        getData(REST_PARAMS.FORECAST_ENDPOINT, rp, new JsonConverter<List<WeatherData>>() {
            @Override
            public List<WeatherData> convert(JSONObject json) throws JSONException {
                List<WeatherData> dataList = new ArrayList<>();

                JSONObject cityJson = json.getJSONObject(FORECAST_JSON_PARAMS.CITY);
                int cityId = cityJson.getInt(FORECAST_JSON_PARAMS.CITY_ID);
                String cityName = cityJson.getString(FORECAST_JSON_PARAMS.CITY_NAME);
                String country = cityJson.getString(FORECAST_JSON_PARAMS.CITY_COUNTRY);
                int count = json.getInt(FORECAST_JSON_PARAMS.COUNT);

                JSONArray array = json.getJSONArray(FORECAST_JSON_PARAMS.LIST);
                for (int i = 0; i < array.length(); i++) {
                    WeatherData data = new WeatherData();
                    data.setId(cityId);
                    data.setCityName(cityName);
                    data.setCountry(country);

                    JSONObject jsonObj = array.getJSONObject(i);

                    JSONObject mainJson = jsonObj.getJSONObject(FORECAST_JSON_PARAMS.LIST_MAIN);
                    data.setTemp(mainJson.getDouble(FORECAST_JSON_PARAMS.LIST_MAIN_TEMP));
                    data.setHumidity(mainJson.getDouble(FORECAST_JSON_PARAMS.LIST_MAIN_HUMIDITY));
                    data.setPressure(mainJson.getDouble(FORECAST_JSON_PARAMS.LIST_MAIN_PRESSURE));

                    data.setWeatherIconId(jsonObj.getJSONArray(FORECAST_JSON_PARAMS.LIST_WEATHER)
                            .getJSONObject(0).getString(FORECAST_JSON_PARAMS.LIST_WEATHER_ICON));

                    data.setClouds(jsonObj.getJSONObject(FORECAST_JSON_PARAMS.LIST_CLOUDS)
                            .getDouble(FORECAST_JSON_PARAMS.LIST_CLOUDS_ALL));

                    data.setWind(jsonObj.getJSONObject(FORECAST_JSON_PARAMS.LIST_WIND)
                            .getDouble(FORECAST_JSON_PARAMS.LIST_WIND_SPEED));

                    String dateStr = jsonObj.getString(FORECAST_JSON_PARAMS.LIST_DATE);
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                    try {
                        data.setDate(format.parse(dateStr));
                    } catch (ParseException e) {
                        Log.e(TAG, "Unable to parse date for the forecast: " + e);
                    }

                    dataList.add(data);
                }

                return dataList;
            }
        }, responseHandler);
    }
}
