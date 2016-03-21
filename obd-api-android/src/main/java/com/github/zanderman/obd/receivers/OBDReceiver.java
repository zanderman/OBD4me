package com.github.zanderman.obd.receivers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.zanderman.obd.interfaces.BluetoothCallbackInterface;

/**
 * Class:
 *      OBDReceiver
 *
 * Description:
 *      Primary broadcast receiver representation for all OBD Bluetooth actions.
 */
public class OBDReceiver extends BroadcastReceiver {

    /**
     * Private members.
     */
    final BluetoothCallbackInterface bluetoothCallbackInterface;

    /**
     * Custom Broadcasts.
     */
    public static final String COMMUNICATION_RECEIVE = "com.github.zanderman.obd.custom.intent.communication.receive";
    public static final String COMMUNICATION_TRANSMIT = "com.github.zanderman.obd.custom.intent.communication.transmit";


    /**
     * Default Constructor:
     *      ...
     *
     * Description:
     *      ...
     */
    public OBDReceiver() {
        super();

        this.bluetoothCallbackInterface = null;
    }

    /**
     * Constructor:
     *      ...
     *
     * Description:
     *      ...
     *
     * @param bluetoothCallbackInterface    Primary callback interface used.
     */
    public OBDReceiver(final BluetoothCallbackInterface bluetoothCallbackInterface) {
        super();

        /**
         * Initialize members.
         */
        this.bluetoothCallbackInterface = bluetoothCallbackInterface;
    }

    /**
     * Method:
     *      ...
     *
     * Description:
     *      ...
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        /**
         * Check for different actions.
         */
        switch ( intent.getAction() )
        {
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                bluetoothCallbackInterface.discoveryStarted();
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                bluetoothCallbackInterface.discoveryFinished();
            case BluetoothDevice.ACTION_FOUND:
                bluetoothCallbackInterface.discoveryFound((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            case OBDReceiver.COMMUNICATION_RECEIVE:
                // add action
            case OBDReceiver.COMMUNICATION_TRANSMIT:
                // add action
        }
    }
}
