package com.deakishin.weatherapp.gui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.deakishin.weatherapp.R;
import com.deakishin.weatherapp.model.assets.Assets;
import com.deakishin.weatherapp.model.entities.WeatherData;

import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер для анимированного списка, выводящего элементы по погоде.
 * Для каждого элемента выводится:
 * - название, температура и иконка погоды в заголовке,
 * - остальные характеристики погоды и кнопка прогноза погоды в раскрывающемся теле.
 * Класс абстрактный и может быть настроен через реализацию методов, определяющих:
 * - показывать ли кнопку прогноза погоды,
 * - какое название для элемента стоит выводить.
 */
public abstract class WeatherDataListAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

    private final String TAG = getClass().getSimpleName();

    // Заполнитель представлений.
    private LayoutInflater mLayoutInflater;

    // Контекст приложения.
    private Context mContext;

    // Объект для работы с "сырыми" ресурсами проекта.
    private Assets mAssets;

    // Данные для вывода.
    private List<WeatherData> mData = new ArrayList<>();

    /**
     * Интерфейс обработчика нажатия на кнопку прогноза погоды.
     */
    public interface ForecastClickedCallback {
        /**
         * Вызывается, когда нажата кнопка прогноза погоды для конкретного города.
         *
         * @param cityId   Идентификатор города.
         * @param cityName Название города.
         */
        void onForecastClicked(int cityId, String cityName);
    }

    private ForecastClickedCallback mForecastClickedCallback;

    public WeatherDataListAdapter(Context context, LayoutInflater layoutInflater, ForecastClickedCallback forecastClickedCallback) {
        mContext = context;
        mLayoutInflater = layoutInflater;
        mAssets = Assets.getInstance(mContext);
        mForecastClickedCallback = forecastClickedCallback;
    }

    /**
     * Sets data to display.
     *
     * @param data List of cities with weather data to display.
     */
    public void setData(List<WeatherData> data) {
        if (data == null) {
            mData.clear();
        } else {
            mData = data;
        }
        notifyDataSetChanged();
        Log.i(TAG, "Data is set. Length: " + mData.size());
    }

    @Override
    public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.main_screen_list_item_child, parent, false);
        }

        if (isPhantom(groupPosition)) {
            convertView.setVisibility(View.INVISIBLE);
            return convertView;
        }
        convertView.setVisibility(View.VISIBLE);

        WeatherData weather = (WeatherData) getGroup(groupPosition);

        TextView humidTextView = (TextView) convertView.findViewById(R.id.main_screen_list_item_humidity_textView);
        String humidText = weather.getHumidity() == null ? mContext.getString(R.string.no_data)
                : mContext.getString(R.string.templ_humidity, weather.getHumidity());
        humidTextView.setText(humidText);

        TextView windTextView = (TextView) convertView.findViewById(R.id.main_screen_list_item_wind_textView);
        String windText = weather.getWind() == null ? mContext.getString(R.string.no_data)
                : mContext.getString(R.string.templ_wind, weather.getWind());
        windTextView.setText(windText);

        TextView pressureTextView = (TextView) convertView.findViewById(R.id.main_screen_list_item_pressure_textView);
        String pressureText = weather.getPressure() == null ? mContext.getString(R.string.no_data)
                : mContext.getString(R.string.templ_pressure, weather.getPressure());
        pressureTextView.setText(pressureText);

        TextView cloudsTextView = (TextView) convertView.findViewById(R.id.main_screen_list_item_clouds_textView);
        String cloudsText = weather.getClouds() == null ? mContext.getString(R.string.no_data)
                : mContext.getString(R.string.templ_clouds, weather.getClouds());
        cloudsTextView.setText(cloudsText);

        Button forecastButton = (Button) convertView.findViewById(R.id.main_screen_list_item_forecast_button);
        forecastButton.setOnClickListener(new OnForecastClickedListener(weather));
        forecastButton.setVisibility(isForecastButtonVisible() ? View.VISIBLE : View.GONE);

        return convertView;
    }

    /**
     * @return True, если нужно показать кнопку прогноза погоды,
     * иначе false.
     */
    protected abstract boolean isForecastButtonVisible();

    // Слушатель нажатия на кнопку прогноза погоды для конкретного элемента списка.
    private class OnForecastClickedListener implements View.OnClickListener {

        private WeatherData mData;

        OnForecastClickedListener(WeatherData data) {
            mData = data;
        }

        @Override
        public void onClick(View v) {
            if (mData != null && mForecastClickedCallback != null) {
                mForecastClickedCallback.onForecastClicked(mData.getId(), mData.getCityName());
            }
        }
    }

    @Override
    public int getRealChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public int getGroupCount() {
        return mData.size() + 1;
    }

    // В конце списка добавлен фантомный элемент для улучшения анимации.
    private boolean isPhantom(int groupPosition) {
        return groupPosition == getGroupCount() - 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        if (isPhantom(groupPosition)) return null;
        return mData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.main_screen_list_item_header, parent, false);
        }

        if (isPhantom(groupPosition)) {
            convertView.setVisibility(View.INVISIBLE);
            return convertView;
        }
        convertView.setVisibility(View.VISIBLE);

        WeatherData weather = (WeatherData) getGroup(groupPosition);

        TextView nameTextView = (TextView) convertView.findViewById(R.id.main_screen_list_item_name_textView);
        nameTextView.setText(getGroupTitle(weather));

        TextView tempTextView = (TextView) convertView.findViewById(R.id.main_screen_list_item_temp_textView);
        String tempText = null;
        if (weather.getTemp() == null) {
            tempText = mContext.getString(R.string.no_data);
        } else {
            int tempTemplResId = weather.getTemp().compareTo(0d) >= 0 ? R.string.templ_temp_plus : R.string.templ_temp;
            tempText = mContext.getString(tempTemplResId, weather.getTemp());
        }
        tempTextView.setText(tempText);

        ImageView iconView = (ImageView) convertView.findViewById(R.id.main_screen_list_item_icon_textView);
        iconView.setImageBitmap(mAssets.getImage(weather.getWeatherIconId()));

        View bottomDivider = convertView.findViewById(R.id.main_screen_list_item_header_bottom_divider);
        bottomDivider.setVisibility(isExpanded ? View.INVISIBLE : View.VISIBLE);

        return convertView;
    }

    /**
     * @return Название, которое выводится в родительском элементе.
     */
    protected abstract String getGroupTitle(WeatherData weather);

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
