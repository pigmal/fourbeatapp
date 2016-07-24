package com.pigmal.android.fourbeat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.pigmal.android.fourbeat.service.IFourBeatService;
import com.pigmal.android.fourbeat.service.IFourBeatServiceListener;
import com.pigmal.android.hardware.fourbeat.Protocol;

public class PreferenceEntryActivity extends FourBeatBaseActivity implements OnClickListener {
    protected static final String TAG = "ServiceSample";
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference_entry_activity);

        findViewById(R.id.button_green).setOnClickListener(this);
        findViewById(R.id.button_start_preference).setOnClickListener(this);

        mActivity = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindFourBeat();
    }

    @Override
    protected void onPause() {
        unbindFourBeat();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onFourBeatConnected() {
    }

    @Override
    protected void onFourBeatStateChange(int id, int state) {
        handleButton(Protocol.BUTTON_ID.values()[id], Protocol.BUTTON_STATE.values()[state]);
    }

    /**
     * should be called in UI thread
     *
     * @param button
     * @param change
     */
    void handleButton(Protocol.BUTTON_ID button, Protocol.BUTTON_STATE change) {
        if (change == Protocol.BUTTON_STATE.ON) {
            switch (button) {
                case RED:
                    break;
                case BLUE:
                    break;
                case YELLOW:
                    break;
                case GREEN:
                    finish();
                    break;
                default:
                    break;
            }
        } else {
            // Nothing done now
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_start_preference:
                startActivity(new Intent(this, AppListActivity.class));
                break;
            case R.id.button_green:
                handleButton(Protocol.BUTTON_ID.GREEN, Protocol.BUTTON_STATE.ON);
                break;
            case R.id.button_connect:
                bindFourBeat();
                break;
            default:
                break;
        }
    }
}
