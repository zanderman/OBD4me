package com.github.zanderman.obd.interfaces;

import android.bluetooth.BluetoothDevice;

/**
 * Created by zanderieux on 2/8/16.
 */
public interface BluetoothCallbackInterface {

    /*
        Declare all BT callbacks here.
        NOTE: can be made generic by adding <T> at top.
     */


    //  --------------------
    // | Standard Callbacks |
    //  --------------------
    public void bluetoothError(String message);



    //  ---------------------
    // | Discovery Callbacks |
    //  ---------------------

    // Callback method for when BT discovery has begun.
    public void discoveryStarted();

    // Callback method for when BT discovery has ended.
    public void discoveryFinished();

    // Callback for when a desired device has been found during BT discovery.
    public void discoveryFound(BluetoothDevice device);
}
