package ru.avem.navitest.utils;

import android.util.Log;

import ru.avem.navitest.BuildConfig;


public class Logger {
    public static final String DEBUG_TAG ="DEBUG_TAG";
    private final String TAG;
    private final int priority;

    public static Logger withTag(String tag) {
        return new Logger(tag);
    }

    private Logger(String TAG) {
        this.TAG = TAG;
        this.priority = Log.INFO;
    }

    public <T> Logger log(T message) {
        if (BuildConfig.DEBUG) {
            Log.println(priority, TAG, message + "");
        }
        return this;
    }

    public void withCause(Exception cause) {
        if (BuildConfig.DEBUG) {
            Log.println(priority, TAG, Log.getStackTraceString(cause));
        }
    }
}
