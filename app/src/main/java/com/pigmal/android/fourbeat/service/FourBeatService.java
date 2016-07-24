package com.pigmal.android.fourbeat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.pigmal.android.hardware.fourbeat.FourBeatManager;
import com.pigmal.android.hardware.fourbeat.Protocol;

public class FourBeatService extends Service {
    private static final String TAG = "FourBeatService";
    IFourBeatServiceListener mListener;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "FourBeatService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (IFourBeatService.class.getName().equals(intent.getAction())) {
            return mServiceInterface;
        }
        return null;
    }

    private IFourBeatService.Stub mServiceInterface = new IFourBeatService.Stub() {
        @Override
        public void registerCallback(IFourBeatServiceListener listener)
                throws RemoteException {
            mListener = listener;
            //runStubThread();
            openAccessory();
        }

        @Override
        public void unregisterCallback(IFourBeatServiceListener listener)
                throws RemoteException {
            mListener = null;
            mFourBeatManager.unregisterHandler();
        }
    };

    // debug
    @SuppressWarnings("unused")
    private void runStubThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mListener != null) {
                        try {
                            mListener.onButtonStateChange(0, 0);
                            SystemClock.sleep(2000);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } else {
                        break;
                    }
                }
            }
        }).start();
    }

    private FourBeatManager mFourBeatManager;

    private boolean openAccessory() {
        mFourBeatManager = FourBeatManager.getInstance(this);
        mFourBeatManager.registerHandler(mHandler);
        if (mFourBeatManager.open()) {
            Log.v(TAG, "accessory is ready");
            return true;
        } else {
            Log.v(TAG, "no accessory");
            return false;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Protocol.BUTTON_STATUS_CHANGE:
                    Object[] data = (Object[]) msg.obj;
                    int id = ((Protocol.BUTTON_ID) data[0]).ordinal();
                    int state = ((Protocol.BUTTON_STATE) data[1]).ordinal();
                    try {
                        if (mListener != null) {
                            mListener.onButtonStateChange(id, state);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }

        }
    };
}
