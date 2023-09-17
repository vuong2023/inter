package app.revanced.integrations.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import app.revanced.integrations.settings.SettingsEnum;

public class LogHelper {

    private static final String LOG_PREFIX = "Extended: ";

    public static void printDebug(Class<?> clazz, @NonNull String message) {
        if (!SettingsEnum.ENABLE_DEBUG_LOGGING.getBoolean()) return;
        Log.d(LOG_PREFIX + (clazz != null ? clazz.getSimpleName() : ""), message);
    }

    public static void printException(Class<?> clazz, String message, Throwable ex) {
        Log.e(LOG_PREFIX + (clazz != null ? clazz.getSimpleName() : ""), message, ex);
    }

    public static void printException(Class<?> clazz, String message) {
        Log.e(LOG_PREFIX + (clazz != null ? clazz.getSimpleName() : ""), message);
    }

    public static void info(Class<?> clazz, String message) {
        Log.i(LOG_PREFIX + (clazz != null ? clazz.getSimpleName() : ""), message);
    }
}
