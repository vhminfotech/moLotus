package com.sms.moLotus.feature.chat;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LogHelper {

    private static final int MAX_LOG_LENGTH = 4000;

    public static void v(@NonNull String tag, @Nullable String message) {
        log(message, line -> Log.v(tag, line));
    }

    public static void d(@NonNull String tag, @Nullable String message) {
        log(message, line -> Log.d(tag, line));
    }

    public static void i(@NonNull String tag, @Nullable String message) {
        log(message, line -> Log.i(tag, line));
    }

    public static void w(@NonNull String tag, @Nullable String message) {
        log(message, line -> Log.w(tag, line));
    }

    public static void e(@NonNull String tag, @Nullable String message) {
        log(message, line -> Log.e(tag, line));
    }

    public static void v(@NonNull String tag, @Nullable String message, @Nullable Throwable throwable) {
        log(message, throwable, line -> Log.v(tag, line));
    }

    public static void d(@NonNull String tag, @Nullable String message, @Nullable Throwable throwable) {
        log(message, throwable, line -> Log.d(tag, line));
    }

    public static void i(@NonNull String tag, @Nullable String message, @Nullable Throwable throwable) {
        log(message, throwable, line -> Log.i(tag, line));
    }

    public static void w(@NonNull String tag, @Nullable String message, @Nullable Throwable throwable) {
        log(message, throwable, line -> Log.w(tag, line));
    }

    public static void e(@NonNull String tag, @Nullable String message, @Nullable Throwable throwable) {
        log(message, throwable, line -> Log.e(tag, line));
    }

    private static void log(@Nullable String message, @NonNull LogCB callback) {
        if (message == null) {
            callback.log("null");
            return;
        }
        // Split by line, then ensure each line can fit into Log's maximum length.
        for (int i = 0, length = message.length(); i < length; i++) {
            int newline = message.indexOf('\n', i);
            newline = newline != -1 ? newline : length;
            do {
                int end = Math.min(newline, i + MAX_LOG_LENGTH);
                callback.log(message.substring(i, end));
                i = end;
            } while (i < newline);
        }
    }

    private static void log(@Nullable String message, @Nullable Throwable throwable, @NonNull LogCB callback) {
        if (throwable == null) {
            log(message, callback);
            return;
        }
        if (message != null) {
            log(message + "\n" + Log.getStackTraceString(throwable), callback);
        } else {
            log(Log.getStackTraceString(throwable), callback);
        }
    }

    private interface LogCB {
        void log(@NonNull String message);
    }
}
