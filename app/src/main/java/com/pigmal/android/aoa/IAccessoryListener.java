package com.pigmal.android.aoa;

public interface IAccessoryListener {

    /**
     * Called when receiving packet from USB accessory
     */
    void onReceive(byte[] data);
}
