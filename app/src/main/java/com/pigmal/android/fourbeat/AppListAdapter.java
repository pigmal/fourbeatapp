package com.pigmal.android.fourbeat;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pigmal.android.fourbeat.LaunchAppManager.Filter;

import java.util.List;

public class AppListAdapter extends ArrayAdapter<AppInfo> {
    static final String TAG = "LaunchModeAppListAdapter";

    Context mContext;
    int mLayoutResourceId;
    List<AppInfo> mAppInfoList;

    public AppListAdapter(Context context, int layoutResourceId, List<AppInfo> infos) {
        super(context, layoutResourceId, infos);
        this.mLayoutResourceId = layoutResourceId;
        this.mContext = context;
        this.mAppInfoList = infos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.imgIcon = (ImageView) row.findViewById(R.id.imgIcon);
            holder.txtName = (TextView) row.findViewById(R.id.txtName);
            holder.chkValid = (CheckBox) row.findViewById(R.id.chkValid);

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        final AppInfo info = mAppInfoList.get(position);
        holder.txtName.setText(info.getName());
        holder.imgIcon.setImageDrawable(info.getIcon());
        holder.chkValid.setOnCheckedChangeListener(null);
        holder.chkValid.setChecked(false);

        String[] activities = LaunchAppManager.getInstance(mContext).getLaunchActivities(Filter.MAIN);
        for (int i = 0; i < activities.length; i++) {
            if (activities[i].equals(info.getActivityName())) {
                holder.chkValid.setChecked(true);
                break;
            }
        }

        holder.chkValid.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LaunchAppManager apps = LaunchAppManager.getInstance(mContext);
                if (buttonView.isChecked()) {
                    if (apps.setOnLauncher(Filter.MAIN, info.getPackage(), info.getActivityName())) {
                    } else {
                        buttonView.setChecked(false);
                        Toast.makeText(mContext, "Launcher area is full", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (!apps.removeFromLauncher(Filter.MAIN, info.getActivityName())) {
                        Log.w(TAG, "No packageName to remove");
                    }
                }
            }

        });


        return row;
    }

    static class ViewHolder {
        ImageView imgIcon;
        TextView txtName;
        CheckBox chkValid;
    }
}