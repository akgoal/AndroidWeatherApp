package com.deakishin.weatherapp.model.assets;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс-синглетон для работы с ресурсами.
 */
public class Assets {

    private final String TAG = getClass().getSimpleName();

    // Менеджер, предоставляющий доступ к ресурсам.
    private AssetManager mAssetManager;

    // Карта ресурсов-изображений и их идентификаторов.
    private Map<String, Bitmap> mImages = new HashMap<>();

    private static Assets sAssets;

    /**
     * @param context Контекст приложения.
     * @return Объект для работы с ресурсами проекта.
     */
    public static Assets getInstance(Context context) {
        if (sAssets == null) {
            sAssets = new Assets(context.getApplicationContext());
        }
        return sAssets;
    }

    private Assets(Context context) {
        mAssetManager = context.getAssets();

        try {
            mImages.put("01d", loadPngImage("r01d"));
            mImages.put("01n", loadPngImage("r01n"));
            mImages.put("02d", loadPngImage("r02d"));
            mImages.put("02n", loadPngImage("r02n"));
            mImages.put("03d", loadPngImage("r03d"));
            mImages.put("03n", loadPngImage("r03n"));
            mImages.put("04d", loadPngImage("r04d"));
            mImages.put("04n", loadPngImage("r04n"));
            mImages.put("09d", loadPngImage("r09d"));
            mImages.put("09n", loadPngImage("r09n"));
            mImages.put("10d", loadPngImage("r10d"));
            mImages.put("10n", loadPngImage("r10n"));
            mImages.put("11d", loadPngImage("r11d"));
            mImages.put("11n", loadPngImage("r11n"));
            mImages.put("13d", loadPngImage("r13d"));
            mImages.put("13n", loadPngImage("r13n"));
            mImages.put("50d", loadPngImage("r50d"));
            mImages.put("50n", loadPngImage("r50n"));
        } catch (IOException e) {
            Log.e(TAG, "Unable to load asset files: " + e);
        }
    }

    // Загружает ресурс изображения.
    private Bitmap loadPngImage(String filename) throws IOException {
        InputStream is = null;
        try {
            is = mAssetManager.open(filename + ".png");
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            throw e;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * Возвращает изображение по его идентификатору.
     *
     * @param imageId Идентификатор изображения.
     * @return Bitmap изображение или null, если изображение не найдено.
     */
    public Bitmap getImage(String imageId) {
        Bitmap img = null;
        if (mImages.containsKey(imageId)) {
            img = mImages.get(imageId);
        }
        Log.i(TAG, "Attempted to get asset image for " + imageId + ". Success: " + (img != null));
        return img;
    }
}
