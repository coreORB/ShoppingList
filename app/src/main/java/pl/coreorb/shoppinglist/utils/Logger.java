package pl.coreorb.shoppinglist.utils;

import android.util.Log;

import pl.coreorb.shoppinglist.BuildConfig;

/**
 * Class for adjusting log output.
 */
public class Logger {

    /**
     * LEVEL 0 - output nothing
     * LEVEL 1 - output only wtf
     * LEVEL 2 - output wtf and e
     * LEVEL 3 - output wtf, e and w
     * LEVEL 4 - output wtf, e, w and i
     * LEVEL 5 - output wtf, e, w, i and d
     * LEVEL 6 - output wtf, e, w, i, d and v
     */

    private static final int DEBUG_LEVEL = 6;
    private static final int RELEASE_LEVEL = 2;

    public static void v(String tag, String msg) {
        if (BuildConfig.DEBUG && DEBUG_LEVEL >= 6 || !BuildConfig.DEBUG && RELEASE_LEVEL >= 6)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if ((BuildConfig.DEBUG && DEBUG_LEVEL >= 5) || (!BuildConfig.DEBUG && RELEASE_LEVEL >= 5))
            Log.d(tag, msg);
    }

    @SuppressWarnings("unused")
    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG && DEBUG_LEVEL >= 4 || !BuildConfig.DEBUG && RELEASE_LEVEL >= 4)
            Log.i(tag, msg);
    }

    @SuppressWarnings("unused")
    public static void w(String tag, String msg) {
        if (BuildConfig.DEBUG && DEBUG_LEVEL >= 3 || !BuildConfig.DEBUG && RELEASE_LEVEL >= 3)
            Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG && DEBUG_LEVEL >= 2 || !BuildConfig.DEBUG && RELEASE_LEVEL >= 2)
            Log.e(tag, msg);
    }

    @SuppressWarnings("unused")
    public static void wtf(String tag, String msg) {
        if (BuildConfig.DEBUG && DEBUG_LEVEL >= 1 || !BuildConfig.DEBUG && RELEASE_LEVEL >= 1)
            Log.wtf(tag, msg);
    }

}
