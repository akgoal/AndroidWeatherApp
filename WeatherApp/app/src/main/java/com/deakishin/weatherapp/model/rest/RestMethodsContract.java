package com.deakishin.weatherapp.model.rest;

/** Контракт для Rest-методов. */
public class RestMethodsContract {
    /** Rest- методы. */
    public static class Methods{
        /** Обновить все данные по текущей погоде. */
        public static final int REFRESH_ALL_WEATHERS = 1;

        /** Обновить прогноз погоды для конкретного города. */
        public static final int REFRESH_FORECAST = 2;

        /** Добавить новый город с его текущей погодой по его названию. */
        public static final int ADD_CITY = 3;
    }
}
