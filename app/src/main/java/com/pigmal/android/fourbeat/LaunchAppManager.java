package com.pigmal.android.fourbeat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import junit.framework.Assert;

public class LaunchAppManager {
    static final String TAG = "LaunchAppManager";

    private final String LAUNCH_APP_PREFS = "launch_app_pref";
    private static final int NUM_MAX_APPS = 8;
    private static final String MAIN_PREFIX = "main";
    private static final String PACKAGE_POSTFIX = "_package";
    private static final String ACTIVITY_POSTFIX = "_activity";

    enum Filter {MAIN}

    ;
    private static LaunchAppManager mSelf;
    private Context mContext;

    private LaunchAppManager(Context context) {
        mContext = context;
    }

    public static LaunchAppManager getInstance(Context context) {
        if (mSelf == null) {
            mSelf = new LaunchAppManager(context);
        }
        return mSelf;
    }

    public String getPackagaByIndex(Filter filter, int index) {
        String prefix = getPrefix(filter);
        Assert.assertTrue(index < NUM_MAX_APPS);

        SharedPreferences pref = mContext.getSharedPreferences(LAUNCH_APP_PREFS, Activity.MODE_PRIVATE);
        return pref.getString(prefix + index + PACKAGE_POSTFIX, "");
    }

    public String getActivityByIndex(Filter filter, int index) {
        String prefix = getPrefix(filter);
        Assert.assertTrue(index < NUM_MAX_APPS);

        SharedPreferences pref = mContext.getSharedPreferences(LAUNCH_APP_PREFS, Activity.MODE_PRIVATE);
        return pref.getString(prefix + index + ACTIVITY_POSTFIX, "");
    }

    public String[] getLaunchActivities(Filter filter) {
        String prefix = getPrefix(filter);
        String[] ret = new String[NUM_MAX_APPS];

        SharedPreferences pref = mContext.getSharedPreferences(LAUNCH_APP_PREFS, Activity.MODE_PRIVATE);

        for (int i = 0; i < NUM_MAX_APPS; i++) {
            ret[i] = pref.getString(prefix + i + ACTIVITY_POSTFIX, "");
        }

        return ret;
    }

    public String[] getLaunchPackages(Filter filter) {
        String prefix = getPrefix(filter);
        String[] ret = new String[NUM_MAX_APPS];

        SharedPreferences pref = mContext.getSharedPreferences(LAUNCH_APP_PREFS, Activity.MODE_PRIVATE);

        for (int i = 0; i < NUM_MAX_APPS; i++) {
            ret[i] = pref.getString(prefix + i + PACKAGE_POSTFIX, "");
        }

        return ret;
    }

    /**
     * Set the app on the launcher
     *
     * @param packageName
     * @param activityName
     * @return
     */
    public boolean setOnLauncher(Filter filter, String packageName, String activityName) {
        SharedPreferences pref = mContext.getSharedPreferences(LAUNCH_APP_PREFS, Activity.MODE_PRIVATE);
        String prefix = getPrefix(filter);

        for (int i = 0; i < NUM_MAX_APPS; i++) {
            String value = pref.getString(prefix + i + PACKAGE_POSTFIX, "");
            if (value.equals("")) {
                Editor editor = pref.edit();
                editor.putString(prefix + i + PACKAGE_POSTFIX, packageName);
                editor.putString(prefix + i + ACTIVITY_POSTFIX, activityName);
                editor.apply();
                return true;
            }
        }
        return false;
    }

    /**
     * Remove the app from the launcher app
     *
     * @param filter
     * @param activityName
     * @return
     */
    public boolean removeFromLauncher(Filter filter, String activityName) {
        SharedPreferences pref = mContext.getSharedPreferences(LAUNCH_APP_PREFS, Activity.MODE_PRIVATE);
        String prefix = getPrefix(filter);

        for (int i = 0; i < NUM_MAX_APPS; i++) {
            String value = pref.getString(prefix + i + ACTIVITY_POSTFIX, "");
            if (value.equals(activityName)) {
                Editor editor = pref.edit();
                editor.putString(prefix + i + PACKAGE_POSTFIX, "");
                editor.putString(prefix + i + ACTIVITY_POSTFIX, "");
                editor.apply();
                return true;
            }
        }
        return false;
    }

    /**
     * Get preference prefix String
     *
     * @param filter
     * @return
     */
    private String getPrefix(Filter filter) {
        String ret = "";
        switch (filter) {
            case MAIN:
                ret = MAIN_PREFIX;
                break;
            default:
                break;
        }
        return ret;
    }

    public void debugPrint() {
        SharedPreferences pref = mContext.getSharedPreferences(LAUNCH_APP_PREFS, Activity.MODE_PRIVATE);
        String prefix = getPrefix(Filter.MAIN);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < NUM_MAX_APPS; i++) {
            sb.append(i);
            sb.append(":");
            sb.append(pref.getString(prefix + i + PACKAGE_POSTFIX, ""));
            sb.append("|");
            sb.append(pref.getString(prefix + i + ACTIVITY_POSTFIX, ""));
            sb.append("\n");
        }
        Log.v(TAG, sb.toString());
    }
}