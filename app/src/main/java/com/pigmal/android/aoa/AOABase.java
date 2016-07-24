package com.pigmal.android.aoa;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AOABase {
    private static final String TAG = "AOABase";

    private static final String ACTION_USB_PERMISSION = "com.pigmal.android.aoa.action.USB_PERMISSION";

    private Context mContext;

    // USB manager to access to USB accessory.
    // This class is NOT default UsbManager (android.hardware.usb.UsbManager).
    // Ref : http://tools.oesf.biz/android-4.0.4_r1.0/xref/frameworks/base/libs/usb/src/com/android/future/usb/UsbManager.java
    private UsbManager mUsbManager;

    // File descriptor to connect to USB accessory.
    // This is used for checking whether connection is already open or not.
    private ParcelFileDescriptor mFileDescriptor;

    // USB accessory which is connected to this application
    private UsbAccessory mAccessory;

    // Stream to send packet to USB accessory
    private OutputStream mOutputStream;

    // Stream to receive packet from USB accessory
    private InputStream mInputStream;

    // flag to check if receiving thread is running
    private boolean mReceiveThreadRunning;

    // flag to check whether request event for permission is been sending to USB manager or not.
    private boolean mIsPermissionRequestPending;

    public AOABase(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        mContext = context;

        mUsbManager = UsbManager.getInstance(mContext);
        if (mUsbManager == null) {
            throw new IllegalStateException("mUsbManager cannot be null");
        }
    }

    //--------------------------------------------------------------------------
    // mUsbReceiver Handling
    //--------------------------------------------------------------------------

    /**
     * Initialize this class.
     * Need to call this just when want to use this class again after release().
     */
    public void init() {

        if (mReceiveThreadRunning != false
                || mFileDescriptor != null
                || mAccessory != null
                || mOutputStream != null
                || mInputStream != null) {
            Log.w(TAG, "Previous close might failed");
        }

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        mContext.registerReceiver(mUsbReceiver, filter);
    }


    /**
     * Receiver/event handler for broadcast intent.
     */
    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(TAG, "onReceive() for " + action);

            UsbAccessory accessory = UsbManager.getAccessory(intent);
            if (accessory == null) {
                Log.d(TAG, "accessory is null");
                return;
            }

            // When permission handling event
            if (ACTION_USB_PERMISSION.equals(action)) {
                // When permission is granted
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    prepareAccessory(accessory);
                } else {
                    Log.d(TAG, "permission denied for accessory " + accessory);
                }
                synchronized (mUsbReceiver) {
                    mIsPermissionRequestPending = false;
                }
            }
            // When USB accessory is detached from phone.
            else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                if (accessory.equals(mAccessory)) {
                    if (mOpening) {
                        close();
                    } else {
                        Log.v(TAG, "May not occur");
                    }
                }
            }
        }
    };

    //--------------------------------------------------------------------------
    // Public method to handle accessory
    //--------------------------------------------------------------------------
    private boolean mOpening;

    public boolean open() {
        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory == null) {
            Log.d(TAG, "accessory is null");
            //throw new IllegalStateException();
            return false;
        }

        // If permission is granted already, open accessory
        if (mUsbManager.hasPermission(accessory)) {
            if (prepareAccessory(accessory)) {
                mOpening = true;
            }
        } else { // Otherwise, request permission.
            synchronized (mUsbReceiver) {
                if (!mIsPermissionRequestPending) {
                    requestPermission(accessory);
                    mIsPermissionRequestPending = true;
                }
            }
        }
        return mOpening;
    }

    public void close() {
        if (mOpening) {
            closeAccessory();
            mContext.unregisterReceiver(mUsbReceiver);
            mOpening = false;
        } else {
            Log.w(TAG, "Trying to close non opened accessory");
        }
    }

    public boolean isOpening() {
        return mOpening;
    }

    private void requestPermission(UsbAccessory accessory) {
        PendingIntent permissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
        if (permissionIntent == null) {
            throw new IllegalStateException("permissionIntent cannot be null");
        }

        //Send PendingIntent to request permission to user.
        mUsbManager.requestPermission(accessory, permissionIntent);
    }

    //--------------------------------------------------------------------------
    // Accessory Handling
    //--------------------------------------------------------------------------

    /**
     * Open Accessory and prepare for the input and output stream,
     * start receive thread.
     *
     * @param accessory
     */
    private boolean prepareAccessory(UsbAccessory accessory) {
        if (mFileDescriptor != null) {
            if (mOpening) {
                Log.d(TAG, "mFileDescriptor is not null. Maybe already open.");
                return true;
            } else {
                throw new IllegalStateException("consistency error of fd and open flag");
            }
        }

        if (accessory == null) {
            throw new IllegalArgumentException("UsbAccessory cannot be null");
        }

        boolean ret = false;
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor == null) {
            Log.e(TAG, "accessory open fail");
        } else {
            mAccessory = accessory;

            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mOutputStream = new FileOutputStream(fd);
            mInputStream = new FileInputStream(fd);

            // Ready to use input and output stream
            Thread thread = new Thread(new AccessoryReceiver(), "AccessoryListen");
            thread.start();

            mOpening = true;
            ret = true;
        }
        return ret;
    }

    /**
     * Close accessory
     */
    private void closeAccessory() {
        Log.v(TAG, "closeAccessory()");
        if (mFileDescriptor != null) {
            try {
                mFileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mReceiveThreadRunning = false;
        mFileDescriptor = null;
        mAccessory = null;
        mOutputStream = null;
        mInputStream = null;
        mListener = null;
    }

    //--------------------------------------------------------------------------
    // Listener Handling
    //--------------------------------------------------------------------------
    private IAccessoryListener mListener;

    /**
     * Set listener to receive event/packet from USB accessory.
     *
     * @param listener set null if unsubscribe this class.
     */
    public void setListener(IAccessoryListener listener) {
        mListener = listener;
    }

    //--------------------------------------------------------------------------
    // Packet read/write
    //--------------------------------------------------------------------------
    private static final int MAX_BUF_SIZE = 1024;

    /**
     * Thread to wait/receive data from USB accessory
     */
    class AccessoryReceiver implements Runnable {
        @Override
        public void run() {
            Log.v(TAG, "receiving thread start");
            int length = 0;
            byte[] buffer = new byte[MAX_BUF_SIZE];

            mReceiveThreadRunning = true;
            while (length >= 0 && mReceiveThreadRunning) {
                try {
                    if (mInputStream == null) {
                        Log.w(TAG, "mInputStream is null");
                        break;
                    }
                    length = mInputStream.read(buffer);

                } catch (IOException e) {
                    Log.e(TAG, "read failed", e);
                    break;
                }

                byte[] data = new byte[length];
                System.arraycopy(buffer, 0, data, 0, length);
                if (mListener != null) {
                    mListener.onReceive(data);
                }
            }
            mInputStream = null;
            mReceiveThreadRunning = false;
            Log.v(TAG, "receiving thread is finishing");
        }
    }

    ;

    /**
     * Write data to USB accessory
     */
    public void write(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null");
        }

        if (mOutputStream != null) {
            try {
                mOutputStream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.w(TAG, "output stream is null");
        }
    }
}