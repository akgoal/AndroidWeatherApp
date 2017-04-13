package com.deakishin.weatherapp.gui.mainscreen;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.deakishin.weatherapp.R;
import com.deakishin.weatherapp.model.rest.RestMethodsContract;
import com.deakishin.weatherapp.model.services.RestService;
import com.deakishin.weatherapp.model.services.RestServiceHelper;

import java.util.Date;

/**
 * Диалог добавления нового города.
 * Если город отправлен удачно, родительской активности отправляется оповещение
 * (при условии, что последняя реализует соответствующий интерфейс).
 */
public class AddCityDialogFragment extends DialogFragment {

    private final String TAG = getClass().getSimpleName();

    // Виджеты.
    private EditText mNameEditText;
    private TextView mStatusTextView;
    private ProgressBar mProgressBar;

    // Помощник для запуска службы, чтобы добавить город в фоновом потоке.
    private RestServiceHelper mHelper;

    // Флаги статуса выполнения операций.
    private boolean mRefreshing, mError, mCityNotFound;

    /**
     * Интерфейс, который должна реализовать родительская активность для получения
     * уведомления об успехе добавления города.
     */
    public interface OnSuccessCallback {
        /**
         * Вызывается, когда город успешно добавлен.
         */
        void onSuccess();
    }

    public AddCityDialogFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_city, null);

        mNameEditText = (EditText) v.findViewById(R.id.dialog_add_city_editText);
        mProgressBar = (ProgressBar) v.findViewById(R.id.dialog_add_city_progressBar);
        mStatusTextView = (TextView) v.findViewById(R.id.dialog_add_city_status_textView);

        mNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!isEditTextEmpty(mNameEditText)) {
                        addCity(mNameEditText.getText().toString());
                    }
                    return true;
                }
                return false;
            }
        });

        Dialog dialog = new AlertDialog.Builder(getActivity()).setView(v)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (!isEditTextEmpty(mNameEditText)) {
                            addCity(mNameEditText.getText().toString());
                        }
                    }
                });
            }
        });
        // Показываем клавиатуру.
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    // Возвращает true, если поле ввода пусто.
    private boolean isEditTextEmpty(EditText editText) {
        return editText.getText() == null
                || editText.getText().toString().length() < 1;
    }

    // Обновляет виджеты статуса.
    private void updateStatusViews() {
        mProgressBar.setVisibility(mRefreshing ? View.VISIBLE : View.GONE);

        mStatusTextView.setVisibility(mError || mCityNotFound ? View.VISIBLE : View.GONE);
        if (mError) {
            mStatusTextView.setText(R.string.add_city_error);
        } else {
            mStatusTextView.setText(R.string.no_city_found);
        }

        mNameEditText.setEnabled(!mRefreshing);
    }

    // Инициирует добавление города по его названию.
    private void addCity(String cityName) {
        mRefreshing = true;
        mError = mCityNotFound = false;
        updateStatusViews();
        mHelper.addCity(cityName);
    }

    // Оповещает активность об успехе.
    private void returnResult() {
        Activity act = getActivity();
        if (act != null && act instanceof OnSuccessCallback) {
            ((OnSuccessCallback) act).onSuccess();
        }
        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        mHelper = new RestServiceHelper(getActivity(), RETURN_ACTION);
        Log.i(TAG, "Registering broadcast receiver. Intent filter: " + mFilter.getAction(0));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, mFilter);

        updateStatusViews();
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
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
            int result = extras.getInt(RestService.Extras.RESULT_EXTRA);
            int methodId = extras.getInt(RestService.Extras.METHOD_EXTRA);

            switch (methodId) {
                case RestMethodsContract.Methods.ADD_CITY:
                    mRefreshing = mError = mCityNotFound = false;
                    if (result < 0) {
                        // Произошла ошибка
                        mError = true;
                    } else {
                        if (result == 0) {
                            // Город не найден.
                            mCityNotFound = true;
                        } else {
                            // Успех.
                            returnResult();
                        }
                    }
                    updateStatusViews();
                    return;
                default:
                    return;
            }
        }
    };
}
