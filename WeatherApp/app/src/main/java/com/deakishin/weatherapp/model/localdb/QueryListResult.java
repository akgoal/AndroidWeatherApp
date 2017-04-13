package com.deakishin.weatherapp.model.localdb;

import com.deakishin.weatherapp.model.entities.DataStatus;

import java.util.List;

/**
 * Результат запроса к БД. Содержит список элементов из базы, а также статус этих данных.
 */
public class QueryListResult<T> {
    private List<T> mData;
    private DataStatus mDataStatus;

    public QueryListResult() {
    }

    public QueryListResult(List<T> data, DataStatus dataStatus) {
        mData = data;
        mDataStatus = dataStatus;
    }

    public List<T> getData() {
        return mData;
    }

    public void setData(List<T> data) {
        mData = data;
    }

    public DataStatus getDataStatus() {
        return mDataStatus;
    }

    public void setDataStatus(DataStatus dataStatus) {
        mDataStatus = dataStatus;
    }
}
