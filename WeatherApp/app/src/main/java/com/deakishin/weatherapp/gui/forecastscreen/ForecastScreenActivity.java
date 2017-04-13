package com.deakishin.weatherapp.gui.forecastscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.deakishin.weatherapp.R;
import com.deakishin.weatherapp.gui.AnimatedExpandableListView;
import com.deakishin.weatherapp.model.entities.DataStatus;
import com.deakishin.weatherapp.model.entities.WeatherData;
import com.deakishin.weatherapp.model.localdb.LocalDb;
import com.deakishin.weatherapp.model.localdb.QueryListResult;
import com.deakishin.weatherapp.model.localdb.impl.LocalDbImpl;
import com.deakishin.weatherapp.model.rest.RestMethodsContract;
import com.deakishin.weatherapp.model.services.RestService;
import com.deakishin.weatherapp.model.services.RestServiceHelper;

import java.util.Date;

/**
 * Активность для экрана прогноза погоды для конкретного города.
 * Для выполнения операций синхронизации с сервером запускается служба с помощью
 * {@link RestServiceHelper}.
 */

public class ForecastScreenActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    /**
     * Ключи для дополнений к интентам для запуска активности.
     */
    public abstract class Extras {
        public static final String CITY_ID_EXTRA = "cityId";
        public static final String CITY_NAME_EXTRA = "cityName";
    }


    // Виджеты.
    private AnimatedExpandableListView mListView;
    private TextView mStatusTextView;
    private TextView mNameTextView;

    // Адаптер списка городов с текущей погодой.
    private ForecastScreenListAdapter mListAdapter;

    // Помощник для работы с Rest через службу.
    private RestServiceHelper mHelper;
    // Локальная база данных.
    private LocalDb mLocalDb;

    // Флаги статуса фоновых операций связи с сервером.
    private boolean mRefreshing = false, mError = false;

    // Дата последнего обновления базы.
    private Date mLastUpdate;

    // Идентификатор и название города, для которого выводится прогноз погоды.
    private int mCityId = -1;
    private String mCityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast_screen);

        Bundle state = null;
        if (savedInstanceState != null) {
            state = savedInstanceState;
        } else if (getIntent() != null) {
            state = getIntent().getExtras();
        }
        if (state != null) {
            if (state.containsKey(Extras.CITY_ID_EXTRA)) {
                mCityId = state.getInt(Extras.CITY_ID_EXTRA);
            }
            if (state.containsKey(Extras.CITY_NAME_EXTRA)) {
                mCityName = state.getString(Extras.CITY_NAME_EXTRA);
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLocalDb = LocalDbImpl.getInstance(this);

        mListView = (AnimatedExpandableListView) findViewById(R.id.forecast_screen_listView);
        mStatusTextView = (TextView) findViewById(R.id.forecast_screen_status_textView);
        mNameTextView = (TextView) findViewById(R.id.forecast_screen_name_textView);
        mNameTextView.setText(mCityName == null ? getString(R.string.no_data)
                : getString(R.string.templ_name, mCityName));

        mListAdapter = new ForecastScreenListAdapter(this, getLayoutInflater(), null);
        mListView.setAdapter(mListAdapter);

        // Анимируем раскрытие/закрытие списка.
        mListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (mListView.isGroupExpanded(groupPosition)) {
                    mListView.collapseGroupWithAnimation(groupPosition);
                } else {
                    mListView.expandGroupWithAnimation(groupPosition);
                    int position = mListView.getPositionForView(v);
                    mListView.smoothScrollToPositionFromTop(position, 0);
                }
                return true;
            }
        });

        if (NavUtils.getParentActivityName(this) != null && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(Extras.CITY_ID_EXTRA, mCityId);
        savedInstanceState.putString(Extras.CITY_NAME_EXTRA, mCityName);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.forecast_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(this) != null) {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            case R.id.menu_forecast_screen_refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Запускает синхронизацию с сервером.
    private void refresh() {
        mError = false;
        mRefreshing = true;
        updateStatusViews();

        mHelper.refreshForecast(mCityId);
    }

    // Обновляет данные с локальной БД.
    private void updateData() {
        Log.i(TAG, "Trying to update forecast data from DB.");
        new LocalDataLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // Обновляет статус данных.
    private void updateStatusViews() {
        String status = mError ? getString(R.string.status_error) :
                (mRefreshing ? getString(R.string.status_refreshing) :
                        (getString(R.string.status_updated_when,
                                (mLastUpdate == null ? getString(R.string.no_data) :
                                        DateFormat.getDateFormat(this).format(mLastUpdate) + ", "
                                                + DateFormat.getTimeFormat(this).format(mLastUpdate)))));
        mStatusTextView.setText(status);
    }

    /**
     * Фоновый загрузчик данных из БД.
     */
    private class LocalDataLoader extends AsyncTask<Void, Void, QueryListResult<WeatherData>> {

        @Override
        protected QueryListResult<WeatherData> doInBackground(Void... params) {
            Log.i(TAG, "Loading data from the local DB.");
            return mLocalDb.getWeatherForecast(mCityId);
        }

        @Override
        protected void onPostExecute(QueryListResult<WeatherData> result) {
            mListAdapter.setData(result.getData());

            DataStatus dataStatus = result.getDataStatus();
            if (dataStatus != null) {
                mRefreshing = dataStatus.isRefreshing();
                mLastUpdate = dataStatus.getLastUpdate();
                updateStatusViews();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mHelper = new RestServiceHelper(this, RETURN_ACTION);
        Log.i(TAG, "Registering broadcast receiver. Intent filter: " + mFilter.getAction(0));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mFilter);

        updateData();
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

        super.onStop();
    }

    private final String RETURN_ACTION = getClass().getName() + ".ActionResult";
    private final IntentFilter mFilter = new IntentFilter(RETURN_ACTION);

    /**
     * Широковещательный приемник для получения результата выполнения запроса на сервер.
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received broadcast");
            Bundle extras = intent.getExtras();
            boolean success = extras.getInt(RestService.Extras.RESULT_EXTRA) > 0;
            int methodId = extras.getInt(RestService.Extras.METHOD_EXTRA);

            switch (methodId) {
                case RestMethodsContract.Methods.REFRESH_FORECAST:
                    mRefreshing = false;
                    if (success) {
                        mError = false;
                        mLastUpdate = new Date();
                        updateData();
                    } else {
                        mError = true;
                    }
                    updateStatusViews();
                    return;
                default:
                    return;
            }
        }
    };
}