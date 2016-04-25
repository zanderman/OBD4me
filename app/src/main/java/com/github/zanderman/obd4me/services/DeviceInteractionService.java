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
 * Class:
 *      DeviceInteractionService
 *
 * Description:
 *      Custom background service which facilitates all Bluetooth device interactions.
 *      All connection/disconnection requests are managed through this service and carried out if
 *      possible.
 *
 *      This background service was designed to provide a level of separation between the application
 *      and the OBD API. The rest of the application does not need to know there's an API present,
 *      this service manages all API calls and provides an app-wide interface that delegates which
 *      API calls are being used for this specific application.
 *
 * Author:
 *      Alexander DeRieux
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
     *      DeviceInteractionService(  )
     *
     * Description:
     *      Creates a new service object with initialized members.
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


    /**
     * Method:
     *      onCreate(  )
     *
     * Description:
     *      Sets up all service elements upon creation.
     */
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
     *      Called automatically upon an activity attempting to bind with this service.
     *
     *      Sets this service's binding status within SharedPreferences.
     *
     * @param intent    Intent that was packaged with the bind request.
     * @return IBinder  Reference to this service's binder object.
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

    /**
     * Method:
     *      onUnbind( Intent )
     *
     * Description:
     *      Called automatically upon an activity attempting to unbind from this service.
     *
     *      Sets this service's binding status within SharedPreferences.
     *
     * @param intent    Intent that was packaged with the unbind request.
     * @return boolean  Status of service unbind.
     */
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
    /**
     * Method:
     *      onStartCommand( Intent, int, int )
     *
     * Description:
     *      Called automatically when service is first started (before binding).
     *
     *      Sets this service's started status within SharedPreferences.
     *      Also registers the OBDManager broadcast receiver with this service.
     *
     * @param intent    Intent that was packaged with start.
     * @return int      Desired persistence state.
     */
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


    /**
     * Method:
     *      onDestroy(  )
     *
     * Description:
     *      Called on the destroy section of the activity lifecycle.
     *
     *      Unregisters the broadcast receivers registered with this service.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        /**
         * Unregister the OBD manager's broadcast receiver.
         */
        this.manager.unregisterBroadcastReceiver(this);

        Log.d("Service", "Destroyed");
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
     *      connectDevice(  )
     *
     * Description:
     *      Initializes a connection with the pre-determined OBDAdapter object.
     *
     *      Stores connection and device information within SharedPreferences.
     *
     * @return boolean  Connection result.
     */
    public boolean connectDevice( ) {

        boolean flag = this.device.connect(); /* Connect to OBDAdapter. */

        /*
         * Store information within SharedPreferences.
         */
        SharedPreferences server_status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = server_status.edit();
        editor.putBoolean("device_status", flag);
        editor.putString("device_name", this.device.name);
        editor.putString("device_address", this.device.address);
        editor.commit();

        return ( flag ); /* Return device connection status. */
    }


    /**
     * Method:
     *      disconnectDevice(  )
     *
     * Description:
     *      Eliminates the connection with the pre-determined OBDAdapter object.
     *
     *      Stores disconnection and device information within SharedPreferences.
     *
     * @return boolean  Disconnection result.
     */
    public boolean disconnectDevice( ) {

        boolean flag = this.device.disconnect(); /* Disconnect from OBDAdapter. */

        /*
         * Store information within SharedPreferences.
         */
        SharedPreferences server_status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = server_status.edit();
        editor.putBoolean("device_status", flag);
        editor.putString("device_name", null);
        editor.putString("device_address", null);
        editor.commit();

        return ( flag ); /* Return device connection status. */
    }


    /**
     * Method:
     *      startScan(  )
     *
     * Description:
     *      Facilitates Bluetooth scanning via a call to the OBD API scanning method.
     */
    public void startScan() {
        this.manager.startScan();
    }


    /**
     * Method:
     *      stopScan(  )
     *
     * Description:
     *      Facilitates Bluetooth scan halt via a call to the OBD API scan stopping method.
     */
    public void stopScan() {
        this.manager.stopScan();
    }

    /**
     * Method:
     *      setBluetoothCallbacks( Activity )
     *
     * Description:
     *      Specifies the activity which implements the custom Bluetooth callback interface and
     *      marks it for future calls regarding Bluetooth actions.
     *
     * @param activity  Activity that implements set Bluetooth callbacks.
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
     *      Specifies the activity which implements the custom Communication callback interface and
     *      marks it for future calls regarding Communication actions.
     *
     * @param activity  Activity that implements set Communication callbacks.
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


    /**
     * Method:
     *      bluetoothError( String )
     *
     * Description:
     *      Allows processing of any Bluetooth errors.
     *
     *      Does nothing. Required for compilation.
     *
     * @param message   Message packaged with the Bluetooth error.
     */
    @Override
    public void bluetoothError(String message) {

    }

    /**
     * Method:
     *      discoveryStarted(  )
     *
     * Description:
     *      Callback for start of Bluetooth device discovery.
     */
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

    /**
     * Method:
     *      discoveryFinished(  )
     *
     * Description:
     *      Callback for ending of Bluetooth device discovery.
     */
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

    /**
     * Method:
     *      discoveryFound( BluetoothDevice )
     *
     * Description:
     *      Callback for discovery of Bluetooth devices.
     *
     * @param device    Bluetooth device that was found during scan.
     */
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


    /**
     * Method:
     *      receive(  )
     *
     * Description:
     *      Facilitates communication between the app and the OBD API.
     *
     *      Calls the OBD API 'receive' method and returns the raw result.
     *
     * @return String   Raw result of the 'receive' API call.
     */
    @Override
    public String receive(  ) {
        return this.device.receive();
    }


    /**
     * Method:
     *      transmit( String )
     *
     * Description:
     *      Facilitates communication between the app and the OBD API.
     *
     *      Calls the OBD API 'transmit' method and returns status of its completion.
     *
     * @param packet    Message to be sent to the OBD adapter.
     * @return boolean  Status of transmit completion.
     */
    @Override
    public boolean transmit(String packet) {
        return this.device.send( packet );
    }
}
