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
        this.binder = new Binder();
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
    @Nullable
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
        intent.setAction("discoveryStarted");

        // Send the intent through a broadcast.
        sendBroadcast(intent);
    }

    @Override
    public void discoveryFinished() {
        /**
         * Create an intent to notify of Bluetooth discovery completion.
         */
        Intent intent = new Intent();
        intent.setAction("discoveryFinished");

        // Send the intent through a broadcast.
        sendBroadcast(intent);
    }

    @Override
    public void discoveryFound(BluetoothDevice device) {

        /**
         * Create an intent with the Bluetooth device inside of it.
         */
        Intent intent = new Intent();
        intent.putExtra("device", device);
        intent.setAction("newDevice");

        // Send the intent through a broadcast to the Bluetooth callbacks activity.
        sendBroadcast(intent);
    }

    @Override
    public void receive(String packet) {

    }

    @Override
    public void transmit(String packet) {

    }
}
