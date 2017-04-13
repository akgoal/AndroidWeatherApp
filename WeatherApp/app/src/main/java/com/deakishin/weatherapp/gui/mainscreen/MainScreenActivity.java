package com.deakishin.weatherapp.gui.mainscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.deakishin.weatherapp.R;
import com.deakishin.weatherapp.gui.AnimatedExpandableListView;
import com.deakishin.weatherapp.gui.forecastscreen.ForecastScreenActivity;
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
 * Активность главного экрана приложения.
 * Для выполнения операций синхронизации с сервером запускается служба с помощью
 * {@link RestServiceHelper}.
 */
public class MainScreenActivity extends AppCompatActivity implements AddCityDialogFragment.OnSuccessCallback{

    private final String TAG = getClass().getSimpleName();

    // Ключи для диалогов
    private static final String DIALOG_ADD_CITY = "dialogAddCity";

    // Виджеты.
    private AnimatedExpandableListView mListView;
    private TextView mStatusTextView;

    // Адаптер списка городов с текущей погодой.
    private MainScreenListAdapter mListAdapter;

    // Помощник для работы с Rest через службу.
    private RestServiceHelper mHelper;
    // Локальная база данных.
    private LocalDb mLocalDb;

    // Флаги статуса фоновых операций связи с сервером.
    private boolean mRefreshing = false, mError = false;

    // Дата последнего обновления базы.
    private Date mLastUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLocalDb = LocalDbImpl.getInstance(this);

        mListView = (AnimatedExpandableListView) findViewById(R.id.main_screen_listView);
        mStatusTextView = (TextView) findViewById(R.id.main_screen_status_textView);

        mListAdapter = new MainScreenListAdapter(this, getLayoutInflater(),
                new MainScreenListAdapter.ForecastClickedCallback() {
                    @Override
                    public void onForecastClicked(int cityId, String cityName) {
                        Intent intent = new Intent(MainScreenActivity.this, ForecastScreenActivity.class);
                        intent.putExtra(ForecastScreenActivity.Extras.CITY_ID_EXTRA, cityId);
                        intent.putExtra(ForecastScreenActivity.Extras.CITY_NAME_EXTRA, cityName);
                        startActivity(intent);
                    }
                });
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_screen_refresh:
                refresh();
                return true;
            case R.id.menu_main_screen_add_city:
                showAddCityDialog();
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

        mHelper.runService(RestMethodsContract.Methods.REFRESH_ALL_WEATHERS);
    }

    // Обновляет данные с локальной БД.
    private void updateData() {
        Log.i(TAG, "Trying to update data");
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

    @Override
    public void onSuccess() {
        // Город успешно добавлен.
        updateData();
    }

    /**
     * Фоновый загрузчик данных из БД.
     */
    private class LocalDataLoader extends AsyncTask<Void, Void, QueryListResult<WeatherData>> {

        @Override
        protected QueryListResult<WeatherData> doInBackground(Void... params) {
            Log.i(TAG, "Loading data from the local DB.");
            return mLocalDb.getCurrentWeatherData();
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
                case RestMethodsContract.Methods.REFRESH_ALL_WEATHERS:
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

    // Показывает диалог добавления города.
    private void showAddCityDialog() {
        new AddCityDialogFragment().show(getSupportFragmentManager(), DIALOG_ADD_CITY);
    }
}
