package com.pigmal.android.hardware.fourbeat;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pigmal.android.aoa.AOABase;
import com.pigmal.android.aoa.IAccessoryListener;
import com.pigmal.android.hardware.fourbeat.Protocol.BUTTON_ID;
import com.pigmal.android.hardware.fourbeat.Protocol.BUTTON_STATE;

import java.util.Arrays;

import static com.pigmal.android.hardware.fourbeat.Protocol.COMMAND_APP_CONNECT;
import static com.pigmal.android.hardware.fourbeat.Protocol.COMMAND_UPDATE_BUTTONS;

/**
 * This class hide all low level access to the hardware.
 */
public class FourBeatManager implements IAccessoryListener {
    static final String TAG = "FourBeatManager";

    private static FourBeatManager mInstance;
    private static AOABase mAOABase;
    private static Handler mHandler;

    static final int NUM_BUTTONS = Protocol.BUTTON_ID.values().length;
    private boolean[] previousStatus = new boolean[NUM_BUTTONS];

    private FourBeatManager() {
    }

    static public FourBeatManager getInstance(Context c) {
        if (mInstance == null) {
            Log.i(TAG, "Create new AOABase Instance");
            mInstance = new FourBeatManager();
            mAOABase = new AOABase(c);
            mAOABase.init();
            mAOABase.setListener(mInstance);
        }
        return mInstance;
    }

    //TODO use list to support multiple client
    public void registerHandler(Handler handler) {
        mHandler = handler;
    }

    public void unregisterHandler() {
        mHandler = null;
    }

    public boolean open() {
        if (mAOABase.isOpening()) {
            Log.v(TAG, "AOA open already");
            return true;
        } else if (mAOABase.open()) {
            Log.w(TAG, "open success");
            sendConnectMessage();
            return true;
        } else {
            Log.w(TAG, "open pending or failed");
            return false;
        }
    }

    public void close() {
        if (mAOABase.isOpening()) {
            mAOABase.close();
        } else {
            Log.w(TAG, "tried to close not opened accessory");
        }
    }

    @Override
    public void onReceive(byte[] data) {
        parsePacket(data);
    }

    private void parsePacket(byte[] data) {

        int length = data.length;
        if (length < 2) {
            return;
        }

        int packetCount = length / 2;
        byte[] onePacket = new byte[2];
        for (int i = 0; i < packetCount; i++) {
            onePacket[0] = data[i * 2];
            onePacket[1] = data[i * 2 + 1];

            if (onePacket[0] == COMMAND_UPDATE_BUTTONS) {
                handlePacket(onePacket[1]);
            }
        }
    }

    /**
     * Check the each button status and notify
     * when the status is changed
     *
     * @param input packet payload
     */
    private void handlePacket(byte input) {
        boolean[] statuses = new boolean[NUM_BUTTONS];

        for (int i = 0; i < NUM_BUTTONS; i++) {
            statuses[i] = (input & (0x01 << i)) != 0;
        }

        if (Arrays.equals(statuses, previousStatus)) {
            return;
        }

        for (int i = 0; i < previousStatus.length; i++) {
            if (previousStatus[i] != statuses[i]) {
                notifyChange(i, statuses[i]);
                previousStatus[i] = statuses[i];
            }
        }
    }

    /**
     * Notify the change
     *
     * @param id     : the button id
     * @param status : the button status
     */
    private void notifyChange(int id, boolean status) {
        Log.v(TAG, "button event (id, status) = " + id + ", " + status);
        BUTTON_ID button = Protocol.BUTTON_ID.values()[id];
        BUTTON_STATE change = status ? BUTTON_STATE.ON : Protocol.BUTTON_STATE.OFF;

        if (mHandler != null) {
            Object[] event = {button, change};
            Message m = Message.obtain(mHandler, Protocol.BUTTON_STATUS_CHANGE, event);
            mHandler.sendMessage(m);
        } else {
            Log.w(TAG, "No handler");
        }
    }

    private void sendConnectMessage() {
        byte[] commandPacket = new byte[2];
        commandPacket[0] = (byte) COMMAND_APP_CONNECT;
        commandPacket[1] = 0;
        Log.d(TAG, "sending connect message.");

        mAOABase.write(commandPacket);
    }

    // debug
    @SuppressWarnings("unused")
    private void printData(byte[] data) {

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            buf.append(data[i] + ", ");
        }
        Log.e(TAG, "data : " + buf);
    }

    // debug
    @SuppressWarnings("unused")
    private void printStatus(boolean statuses[]) {
        String status = statuses[0] + ", " + statuses[1] + ", " + statuses[2] + ", " + statuses[3];
        Log.e(TAG, "status : " + status);
    }
}
