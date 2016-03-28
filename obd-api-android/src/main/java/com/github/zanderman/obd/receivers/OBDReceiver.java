package com.github.zanderman.obd.receivers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.zanderman.obd.R;
import com.github.zanderman.obd.interfaces.BluetoothCallbackInterface;
import com.github.zanderman.obd.interfaces.CommunicationCallbackInterface;

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
    final CommunicationCallbackInterface communicationCallbackInterface;

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
        this.communicationCallbackInterface = null;
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
        this.communicationCallbackInterface = null;
    }



    /**
     * Constructor:
     *      ...
     *
     * Description:
     *      ...
     *
     * @param communicationCallbackInterface    Primary callback interface used.
     */
    public OBDReceiver(final CommunicationCallbackInterface communicationCallbackInterface) {
        super();

        /**
         * Initialize members.
         */
        this.communicationCallbackInterface = communicationCallbackInterface;
        this.bluetoothCallbackInterface= null;
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
                if ( this.bluetoothCallbackInterface != null )
                    this.bluetoothCallbackInterface.discoveryStarted();
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                if ( this.bluetoothCallbackInterface != null )
                    this.bluetoothCallbackInterface.discoveryFinished();
            case BluetoothDevice.ACTION_FOUND:
                if ( this.bluetoothCallbackInterface != null )
                    this.bluetoothCallbackInterface.discoveryFound((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            case OBDReceiver.COMMUNICATION_RECEIVE:
                if ( this.communicationCallbackInterface != null )
                    this.communicationCallbackInterface.receive( intent.getStringExtra(context.getString(R.string.IncomingData)) );
            case OBDReceiver.COMMUNICATION_TRANSMIT:
                if ( this.communicationCallbackInterface != null )
                    this.communicationCallbackInterface.transmit( context.getString(R.string.OutgoingData) );
        }
    }
}
