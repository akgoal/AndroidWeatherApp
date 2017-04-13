package com.deakishin.weatherapp.model.entities;

import java.util.Date;

/** Класс POJO для статуса хранимых данных. */
public class DataStatus {
    /* Дата последнего обновления. */
    private Date mLastUpdate;

    /* Происходит ли в данных момент обновление данных. */
    private boolean mRefreshing;

    public DataStatus() {
    }

    public DataStatus(boolean refreshing) {
        mRefreshing = refreshing;
    }

    public Date getLastUpdate() {
        return mLastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        mLastUpdate = lastUpdate;
    }

    public boolean isRefreshing() {
        return mRefreshing;
    }

    public void setRefreshing(boolean refreshing) {
        mRefreshing = refreshing;
    }

    @Override
    public String toString() {
        return "DataStatus{" +
                "mLastUpdate=" + (mLastUpdate == null ? "null" : mLastUpdate) +
                ", mRefreshing=" + mRefreshing +
                '}';
    }
}
