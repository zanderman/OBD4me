package com.github.zanderman.obd.interfaces;

import android.bluetooth.BluetoothDevice;

/**
 * Interface:
 *      BluetoothCallbackInterface
 *
 * Description:
 *      Public interface for all Bluetooth broadcast callback methods used by the OBD API.
 */
public interface BluetoothCallbackInterface {

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
