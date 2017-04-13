package com.deakishin.weatherapp.model.entities;

import java.util.Date;

/**
 * Класс POJO для данных о погоде по одному городу.
 */
public class WeatherData {
    // Id города.
    private int mId;
    // Название города.
    private String mCityName;
    // Страна.
    private String mCountry;

    // Температура в Цельсия
    private Double mTemp;
    // Скорость ветра в м/с.
    private Double mWind;
    // Облачность в процентах.
    private Double mClouds;
    // Влажность в процентах.
    private Double mHumidity;
    // Атм. давление в гектопаскалях.
    private Double mPressure;

    // Идентификатор иконки погоды.
    private String mWeatherIconId;

    // Дата погоды.
    private Date mDate;

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getCityName() {
        return mCityName;
    }

    public void setCityName(String cityName) {
        mCityName = cityName;
    }

    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String country) {
        mCountry = country;
    }

    public Double getTemp() {
        return mTemp;
    }

    public void setTemp(Double temp) {
        mTemp = temp;
    }

    public Double getWind() {
        return mWind;
    }

    public void setWind(Double wind) {
        mWind = wind;
    }

    public Double getClouds() {
        return mClouds;
    }

    public void setClouds(Double clouds) {
        mClouds = clouds;
    }

    public Double getHumidity() {
        return mHumidity;
    }

    public void setHumidity(Double humidity) {
        mHumidity = humidity;
    }

    public Double getPressure() {
        return mPressure;
    }

    public void setPressure(Double pressure) {
        mPressure = pressure;
    }

    public String getWeatherIconId() {
        return mWeatherIconId;
    }

    public void setWeatherIconId(String weatherIconId) {
        mWeatherIconId = weatherIconId;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    @Override
    public String toString() {
        return "WeatherData{" +
                "mId=" + mId +
                ", mCityName='" + mCityName + '\'' +
                ", mCountry='" + mCountry + '\'' +
                ", mTemp=" + mTemp +
                ", mWind=" + mWind +
                ", mClouds=" + mClouds +
                ", mHumidity=" + mHumidity +
                ", mPressure=" + mPressure +
                ", mWeatherIconId='" + mWeatherIconId + '\'' +
                ", mDate=" + mDate +
                '}';
    }
}
