package com.deakishin.weatherapp.model.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.deakishin.weatherapp.model.rest.RestMethodsContract;
import com.deakishin.weatherapp.model.rest.RestProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Служба для выполнения фоновой работы с Rest-сервисом.
 * По окончании выполнения операции выполняется рассылка с индикатором успеха/неудачи операции.
 */
public class RestService extends Service {

    private final String TAG = getClass().getSimpleName();

    // Инициализирована ли служба.
    private boolean mInitialized = false;

    // Карта фоновых операций. Нужна для того, чтобы завершить службу по окончании всех операций.
    private final Map<String, AsyncServiceTask> mTasks = new HashMap<>();

    /**
     * Ключи для дополнений к интентам.
     */
    public static class Extras {
        public static final String METHOD_EXTRA = "METHOD_EXTRA";

        public static final String RESULT_ACTION_EXTRA = "RESULT_ACTION_EXTRA";

        public static final String RESULT_EXTRA = "RESULT_EXTRA";

        public static final String CITY_ID_EXTRA = "CITY_ID_EXTRA";

        public static final String CITY_NAME_EXTRA = "CITY_NAME_EXTRA";
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Синхронизируем по карте операций, чтобы служба не успела завершить работу,
        // до того как будет добавлена новая операция.
        synchronized (mTasks) {
            if (!mInitialized) {
                init();
                mInitialized = true;
            }

            Bundle extras = intent.getExtras();
            String taskId = getTaskIdentifier(extras);
            int methodId = extras.getInt(Extras.METHOD_EXTRA);
            String resultAction = extras.getString(Extras.RESULT_ACTION_EXTRA);
            if (mTasks.containsKey(taskId)) {
                // Если операция уже выполняется, то вместо повторного ее запуска,
                // добавляем к ней нового "слушателя"
                mTasks.get(taskId).addResultAction(resultAction);
            } else {
                // Запускаем операцию
                AsyncServiceTask task = new AsyncServiceTask(taskId, methodId, resultAction, extras);
                task.execute();
            }
        }
        return START_STICKY;
    }

    // Настраивает службу.
    private void init() {
    }

    /**
     * Builds a string identifier for this method call.
     * The identifier will contain data about:
     * What processor was the method called on
     * What method was called
     * What parameters were passed
     * This should be enough data to identify a task to detect if a similar task is already running.
     */
    /* Строит идентификатор операции по объекту Bundle, который был получен при запуске службы.
     * Операция идентифицируется по:
     * - методу, который она должна выполнить,
     * - переданным параметрам в метод. */
    private String getTaskIdentifier(Bundle extras) {
        String[] keys = extras.keySet().toArray(new String[0]);
        java.util.Arrays.sort(keys);
        StringBuilder identifier = new StringBuilder();

        for (int keyIndex = 0; keyIndex < keys.length; keyIndex++) {
            String key = keys[keyIndex];

            // Для одной операции могут быть разные "слушатели".
            if (key.equals(Extras.RESULT_ACTION_EXTRA)) {
                continue;
            }

            identifier.append("{");
            identifier.append(key);
            identifier.append(":");
            Object extra = extras.get(key);
            identifier.append(extra == null ? "" : extra.toString());
            identifier.append("}");
        }

        return identifier.toString();
    }

    /**
     * Класс для выполнения операции в отдельном потоке.
     */
    private class AsyncServiceTask extends AsyncTask<Void, Void, Integer> {
        // Идентификатор метода.
        private final int mMethodId;
        // Список "слушателей". По завершении операции для каждого слушателя отправляется
        // широковещательное сообщение.
        private final List<String> mResultActions = new ArrayList<String>();
        // Идентификатор операции.
        private final String mTaskId;
        // Дополнения, в которых могут лежат параметры для методов.
        private final Bundle mExtras;

        AsyncServiceTask(String taskId, int methodId, String resultAction, Bundle extras) {
            mMethodId = methodId;
            addResultAction(resultAction);
            mTaskId = taskId;
            mExtras = extras;
        }

        /**
         * Добавляет нового "слушателя" для операции.
         *
         * @param resultAction Action для интента, который будет использован
         *                     для рассылки результата.
         */
        void addResultAction(String resultAction) {
            if (!mResultActions.contains(resultAction)) {
                mResultActions.add(resultAction);
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Log.i(TAG, "Starting background task. MethodId=" + mMethodId + ". TaskId=" + mTaskId);
            RestProcessor restProcessor = new RestProcessor(RestService.this);
            switch (mMethodId) {
                case RestMethodsContract.Methods.REFRESH_ALL_WEATHERS:
                    return restProcessor.refreshAllWeathers() ? 1 : 0;
                case RestMethodsContract.Methods.REFRESH_FORECAST:
                    if (mExtras == null || !mExtras.containsKey(Extras.CITY_ID_EXTRA)) {
                        return 0;
                    }
                    return restProcessor.refreshForecast(mExtras.getInt(Extras.CITY_ID_EXTRA)) ? 1 : 0;
                case RestMethodsContract.Methods.ADD_CITY:
                    if (mExtras == null || !mExtras.containsKey(Extras.CITY_NAME_EXTRA)) {
                        return 0;
                    }
                    return restProcessor.addCityWithCurrentWeather(mExtras.getString(Extras.CITY_NAME_EXTRA));
                default:
                    return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            synchronized (mTasks) {
                Log.i(TAG, "Finishing background task. MethodId=" + mMethodId + ". Result=" + result);

                // Отправляем широковещательные сообщения всем "слушателям".
                for (String resultAction : mResultActions) {
                    Intent intent = new Intent(resultAction);
                    intent.putExtra(Extras.METHOD_EXTRA, mMethodId);
                    intent.putExtra(Extras.RESULT_EXTRA, result);
                    Log.i(TAG, "Sending broadcast. Result action: " + resultAction);
                    LocalBroadcastManager.getInstance(RestService.this).sendBroadcast(intent);
                }

                mTasks.remove(mTaskId);
                if (mTasks.isEmpty()) {
                    // Останавливаем службу, если операций больше нет.
                    stopSelf();
                }
            }
        }
    }

}
