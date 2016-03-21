package com.github.zanderman.obd.interfaces;

import android.bluetooth.BluetoothDevice;

/**
 * Interface:
 *      BluetoothCallbackInterface
 *
 * Description:
 *      ...
 */
public interface BluetoothCallbackInterface {

    /*
        Declare all BT callbacks here.
        NOTE: can be made generic by adding <T> at top.
     */


    /**
     * Standard Bluetooth Callbacks.
     */
    public void bluetoothError( String message ); /* A bluetooth error has occurred. */


    /**
     * Bluetooth Discovery Callbacks.
     */
    public void discoveryStarted(); /* Callback method for when BT discovery has begun. */
    public void discoveryFinished(); /* Callback method for when BT discovery has ended. */
    public void discoveryFound( BluetoothDevice device ); /* Callback for when a desired device has been found during BT discovery. */
}
