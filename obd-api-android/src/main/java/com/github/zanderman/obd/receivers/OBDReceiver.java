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

        // Get current action.
        String action = intent.getAction();

        /**
         * Check for different actions.
         */
        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            // Call specific callback method.
            bluetoothCallbackInterface.discoveryStarted();
        }
        else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            // Call specific callback method.
            bluetoothCallbackInterface.discoveryFinished();
        }
        else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Call specific callback method.
            bluetoothCallbackInterface.discoveryFound((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
        }
    }
}
