package com.github.zanderman.obd4me.services;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.zanderman.obd.classes.OBDAdapter;
import com.github.zanderman.obd.classes.OBDManager;
import com.github.zanderman.obd.interfaces.BluetoothCallbackInterface;
import com.github.zanderman.obd.interfaces.CommunicationCallbackInterface;
import com.github.zanderman.obd4me.activities.MainActivity;

/**
 * Created by zanderman on 3/21/16.
 */
public class DeviceInteractionService extends Service
        implements BluetoothCallbackInterface, CommunicationCallbackInterface {

    /**
     * Broadcast Actions.
     */
    public static final String BLUETOOTH_ACTION_DISCOVERY_STARTED = "Discovery Started";
    public static final String BLUETOOTH_ACTION_DISCOVERY_FINISHED = "Discovery Finished";
    public static final String BLUETOOTH_ACTION_DISCOVERY_FOUND = "Discovery Found";
    public static final String BLUETOOTH_ITEM_DEVICE = "Bluetooth Device";
    public static final String COMMUNICATION_ACTION_RECEIVE = "Communication Receive";
    public static final String COMMUNICATION_ACTION_TRANSMIT = "Communication Transmit";

    /**
     * Public Service Members
     */
    public IBinder binder;

    /**
     * Private Service Members.
     */
    private Activity bluetoothActivity;
    private Activity communicationActivity;
    private OBDAdapter device;
    private OBDManager manager;


    /**
     * Constructor:
     *      DeviceInteractionService( )
     *
     * Description:
     *      ...
     */
    public DeviceInteractionService() {
        super();

        /**
         * Initialize Members.
         */
        this.bluetoothActivity = null;
        this.communicationActivity = null;
        this.binder = new LocalBinder();
        this.device = null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * Initialize OBD members.
         */
        manager = new OBDManager();
        manager.init(this, this);

        Log.d("Service", "onCreate.");
    }


    /**
     * Class:
     *      LocalBinder
     *
     * Description:
     *      Helper class to give a binder reference to a specific DeviceInteractionService instance.
     *
     * Author:
     *      Alexander DeRieux
     */
    public class LocalBinder extends Binder {
        public DeviceInteractionService getServiceInstance() {
            return DeviceInteractionService.this;
        }
    }



    /**
     * Method:
     *      onBind( Intent )
     *
     * Description:
     *      ...
     *
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {

        /**
         * Update Shared Preferences on Bind action.
         */
        SharedPreferences server_status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=server_status.edit();
        editor.putBoolean("service_bind", false); /* Stores key/value pairs. */
        editor.commit();

        // Return access to this service's binder.
        return this.binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        /**
         * Update Shared Preferences on Unbind action.
         */
        SharedPreferences server_status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=server_status.edit();
        editor.putBoolean("service_bind", false); /* Stores key/value pairs. */
        editor.commit();

        return super.onUnbind(intent);
    }

    // NOTE: When service is first started (before binding), it runs this command.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        /**
         * Updated Shared Preferences on Start service action.
         */
        SharedPreferences server_status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = server_status.edit();
        editor.putBoolean("service_started", true);
        editor.commit();

        /**
         * Set the OBD manager's broadcast receiver to this service.
         */
        this.manager.registerBroadcastReceiver(this);

        Log.d("Service", "Started.");

        // Denote this service as a lingering one.
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        /**
         * Unregister the OBD manager's broadcast receiver.
         */
        this.manager.unregisterBroadcastReceiver(this);

        Log.d("Service","Destroyed");
    }


    /**
     * Method:
     *      setDevice( OBDAdapter )
     *
     * Description:
     *      Set the device that this service will interact with.
     *
     * @param device    OBD device to be interacted with.
     */
    public void setDevice( OBDAdapter device ) {
        this.device = device;
    }

    public boolean connectDevice( ) {

        boolean flag = this.device.connect();

        SharedPreferences server_status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = server_status.edit();
        editor.putBoolean("device_status", flag);
        editor.putString("device_name", this.device.name);
        editor.putString("device_address", this.device.address);
        editor.commit();

        return ( flag );
    }

    public boolean disconnectDevice( ) {

        boolean flag = this.device.disconnect();

        SharedPreferences server_status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = server_status.edit();
        editor.putBoolean("device_status", flag);
        editor.putString("device_name", null);
        editor.putString("device_address", null);
        editor.commit();

        return ( flag );
    }

    public void startScan() {
        this.manager.startScan();
    }

    public void stopScan() {
        this.manager.stopScan();
    }

    /**
     * Method:
     *      setBluetoothCallbacks( Activity )
     *
     * Description:
     *      ...
     *
     * @param activity
     */
    public void setBluetoothCallbacks( Activity activity ){

        /**
         * Ensure the given activity implements Bluetooth callback methods.
         */
        if ( activity instanceof BluetoothCallbackInterface )
            this.bluetoothActivity = activity;
        else
            this.bluetoothActivity = null;
    }

    /**
     * Method:
     *      setCommunicationCallbacks( Activity )
     *
     * Description:
     *      ...
     *
     * @param activity
     */
    public void setCommunicationCallbacks( Activity activity ) {
        /**
         * Ensure the given activity implements Bluetooth communication methods.
         */
        if ( activity instanceof CommunicationCallbackInterface )
            this.communicationActivity = activity;
        else
            this.communicationActivity = null;
    }




    @Override
    public void bluetoothError(String message) {

    }

    @Override
    public void discoveryStarted() {

        /**
         * Create an intent to notify of Bluetooth discovery started.
         */
        Intent intent = new Intent();
        intent.setAction(this.BLUETOOTH_ACTION_DISCOVERY_STARTED);

        Log.d("Service", "Discovery Started.");

        // Send the intent through a broadcast.
        sendBroadcast(intent);
    }

    @Override
    public void discoveryFinished() {
        /**
         * Create an intent to notify of Bluetooth discovery completion.
         */
        Intent intent = new Intent();
        intent.setAction(this.BLUETOOTH_ACTION_DISCOVERY_FINISHED);

        Log.d("Service","Discovery Finished.");

        // Send the intent through a broadcast.
        sendBroadcast(intent);
    }

    @Override
    public void discoveryFound(BluetoothDevice device) {

        /**
         * Create an intent with the Bluetooth device inside of it.
         */
        Intent intent = new Intent();
        intent.putExtra(this.BLUETOOTH_ITEM_DEVICE, device);
        intent.setAction(this.BLUETOOTH_ACTION_DISCOVERY_FOUND);

        Log.d("Service", "Discovery Found.");

        // Send the intent through a broadcast to the Bluetooth callbacks activity.
        sendBroadcast(intent);
    }

    @Override
    public void receive(String packet) {

    }

    @Override
    public void transmit(String packet) {

    }

    public void post( String packet ) {
        Log.d("Service", "device: " + this.device.name);
        this.device.send( packet );
    }

    public String get() {
        return this.device.receive();
    }
}
