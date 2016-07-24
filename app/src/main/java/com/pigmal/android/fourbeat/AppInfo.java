package com.pigmal.android.fourbeat;

import android.graphics.drawable.Drawable;


public class AppInfo {
    private Drawable mIcon;
    private String mName;
    private String mPackage;
    private String mActivity;

    public AppInfo(Drawable icon, String appName, String packageName, String activityName) {
        mIcon = icon;
        mName = appName;
        mPackage = packageName;
        mActivity = activityName;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable mIcon) {
        this.mIcon = mIcon;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getPackage() {
        return mPackage;
    }

    public void setPackage(String mPackage) {
        this.mPackage = mPackage;
    }

    public String getActivityName() {
        return mActivity;
    }

    public void setActivity(String mActivity) {
        this.mActivity = mActivity;
    }
}