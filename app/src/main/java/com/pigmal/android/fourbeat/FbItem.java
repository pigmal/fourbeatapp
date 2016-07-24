package com.pigmal.android.fourbeat;

import android.app.Activity;

public class FbItem {
    public enum Actions {ACTIVITY, PACKAGE, UNKNOWN}

    ;

    private String mName;
    private int mIconResource;
    private Class<? extends Activity> mActivityClass;
    private String mPackageName;
    private String mParameter;

    private Actions mActionType;

    public FbItem(Class<? extends Activity> activity, String appName, String param, int iconRes) {
        if (activity != null) {
            if (appName == null) {
                mName = activity.getSimpleName();
            } else {
                mName = appName;
            }
            mIconResource = iconRes;
            mActivityClass = activity;
            mParameter = param;
            mActionType = Actions.ACTIVITY;
        } else {
            mActionType = Actions.UNKNOWN;
            mIconResource = R.drawable.no_icon_image;
        }
    }

    public String getParameter() {
        return mParameter;
    }

    public void setParameter(String mParameter) {
        this.mParameter = mParameter;
    }

    public FbItem(String packageName, String appName, int iconRes) {
        if (packageName != null && !packageName.equals("")) {
            mName = appName;
            mPackageName = packageName;
            mIconResource = iconRes;
            mActionType = Actions.PACKAGE;
        } else {
            mActionType = Actions.UNKNOWN;
        }
    }

    public String getName() {
        return mName;
    }

    public int getIconResource() {
        return mIconResource;
    }

    public Class<? extends Activity> getActivityClass() {
        return mActivityClass;
    }

    public Actions getActionType() {
        return mActionType;
    }

    public String getPackageName() {
        return mPackageName;
    }
}
