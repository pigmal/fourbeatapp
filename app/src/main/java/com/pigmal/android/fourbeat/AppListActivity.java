package com.pigmal.android.fourbeat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class AppListActivity extends Activity {
    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list_activity);

        // get Launcher intents
        List<AppInfo> appList = new ArrayList<AppInfo>();
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);

        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            Drawable icon = info.loadIcon(pm);
            String packageName = info.activityInfo.packageName;
            String activityName = info.activityInfo.name;
            String appName = info.activityInfo.loadLabel(pm).toString();

            appList.add(new AppInfo(icon, appName, packageName, activityName));
        }

        ListView listView = (ListView) findViewById(R.id.listView1);
        listView.setAdapter(new AppListAdapter(this, R.layout.launcher_app_list_item, appList));

        findViewById(R.id.btnOK).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}