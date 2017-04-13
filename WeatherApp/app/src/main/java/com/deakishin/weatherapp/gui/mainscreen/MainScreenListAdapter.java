package com.deakishin.weatherapp.gui.mainscreen;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.deakishin.weatherapp.R;
import com.deakishin.weatherapp.gui.AnimatedExpandableListView;
import com.deakishin.weatherapp.gui.WeatherDataListAdapter;
import com.deakishin.weatherapp.model.assets.Assets;
import com.deakishin.weatherapp.model.entities.WeatherData;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для анимированного списка главного экрана.
 * Каждый элемент списка выводит информацию о текущей погоде в том или ином городе.
 * Также в каждом элементе присутствует кнопка прогноза погоды.
 */
public class MainScreenListAdapter extends WeatherDataListAdapter {

    // Контекст приложения.
    private Context mContext;

    public MainScreenListAdapter(Context context, LayoutInflater layoutInflater, ForecastClickedCallback forecastClickedCallback) {
        super(context, layoutInflater, forecastClickedCallback);
        mContext = context;
    }

    @Override
    protected boolean isForecastButtonVisible() {
        return true;
    }

    @Override
    protected String getGroupTitle(WeatherData weather) {
        return weather.getCityName() == null ? mContext.getString(R.string.no_data)
                : mContext.getString(R.string.templ_name, weather.getCityName());
    }
}
