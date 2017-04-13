package com.deakishin.weatherapp.gui.forecastscreen;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;

import com.deakishin.weatherapp.R;
import com.deakishin.weatherapp.gui.WeatherDataListAdapter;
import com.deakishin.weatherapp.gui.mainscreen.MainScreenListAdapter;
import com.deakishin.weatherapp.model.entities.WeatherData;

import java.util.Date;

/**
 * Адаптер для анимированного списка элементов на экране прогноза погоды.
 * В качестве названия элеента выводится его время,
 * а кнопка прогнозы погоды, разумеется, отсутствует.
 */
class ForecastScreenListAdapter extends WeatherDataListAdapter {

    // Контекст приложения.
    private Context mContext;

    ForecastScreenListAdapter(Context context, LayoutInflater layoutInflater, ForecastClickedCallback forecastClickedCallback) {
        super(context, layoutInflater, forecastClickedCallback);
        mContext = context;
    }

    @Override
    protected boolean isForecastButtonVisible() {
        return false;
    }

    @Override
    protected String getGroupTitle(WeatherData weather) {
        return weather.getDate() == null ? mContext.getString(R.string.no_data)
                : mContext.getString(R.string.forecast_templ_date, formatDate(weather.getDate()));
    }

    // Форматирует дату в строку.
    private String formatDate(Date date) {
        return DateFormat.getDateFormat(mContext).format(date) + ", "
                + DateFormat.getTimeFormat(mContext).format(date);
    }
}
