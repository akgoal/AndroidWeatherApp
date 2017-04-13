package com.deakishin.weatherapp.model.localdb;

import com.deakishin.weatherapp.model.entities.DataStatus;
import com.deakishin.weatherapp.model.entities.WeatherData;

import java.util.List;

/**
 * Интерфейс для работы с локальной базой данных.
 */
public interface LocalDb {
    /**
     * @return Данные о текущей погоде для каждого города в БД.
     */
    QueryListResult<WeatherData> getCurrentWeatherData();

    /**
     * @return Идентификаторы городов в базе.
     */
    List<Integer> getCitiesIds();

    /**
     * Обновляет данные по текущей погоде.
     *
     * @param dataList Список объектов  с данными о текущей погоде.
     */
    void updateCurrentWeatherData(List<WeatherData> dataList);

    /**
     * @return Статус данных о текущей погоде.
     */
    DataStatus getCurrentWeatherDataStatus();

    /**
     * Обновляет статус данных по текущей погоде.
     *
     * @param status Статус данных.
     */
    void updateCurrentWeatherDataStatus(DataStatus status);

    /**
     * Обновляет прогноз погоды для города.
     * При этом старые данные стираются.
     *
     * @param cityId   Идентификатор города.
     * @param dataList Список объектов, содержащих данные о погоде.
     */
    void updateWeatherForecast(int cityId, List<WeatherData> dataList);

    /**
     * @param cityId Идентификатор города.
     * @return Данные о прогнозе погоде для конкретного города в БД.
     */
    QueryListResult<WeatherData> getWeatherForecast(int cityId);

    /**
     * @param cityId Идентификатор города.
     * @return Статус данных о прогнозе погоде для конкретного города.
     */
    DataStatus getWeatherForecastDataStatus(int cityId);

    /**
     * Обновляет статус данных по прогнозу погоды для конкретного города.
     *
     * @param status Статус данных.
     * @param cityId Идентификатор города.
     */
    void updateForecastDataStatus(DataStatus status, int cityId);

    /** Добавляет город с его текущей погодой. */
    void addCityWithCurrentWeather(WeatherData data);
}
