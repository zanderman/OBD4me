package com.github.zanderman.obd4me.activities;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.zanderman.obd.classes.OBDAdapter;
import com.github.zanderman.obd.classes.OBDManager;
import com.github.zanderman.obd.interfaces.BluetoothCallbackInterface;
import com.github.zanderman.obd4me.R;
import com.github.zanderman.obd4me.adapters.DeviceListAdapter;
import com.github.zanderman.obd4me.interfaces.ListableInterface;
import com.github.zanderman.obd4me.services.DeviceInteractionService;

import java.util.ArrayList;
import java.util.List;


/**
 * Class:
 *      MainActivity
 *
 * Description:
 *      Facilitates Bluetooth scanning and user selection of OBD adapter device.
 *
 *      Scanning is done via pressing a "scan" button. Devices can be filtered by name via a textbox.
 *      As devices are discovered, they are placed into 'cards' and populated in a scrollable
 *      ListView. Open device selection from that list the background service attempts connection.
 *      The user is advanced to the HUD activity upon successful OBD adapter connection.
 *
 * Author:
 *      Alexander DeRieux
 */
public class MainActivity extends AppCompatActivity
                            implements /*BluetoothCallbackInterface,*/ View.OnClickListener, ServiceConnection, ListableInterface {
    /**
     * Members.
     */
    String keyScan;
    DeviceInteractionService service;

    /**
     * Flags
     */
    boolean scan_status;
    boolean bound; /* Flag denoting current binding status with background service. */
    boolean flip_flop;

    /**
     * Shared Preferences.
     */
    SharedPreferences sharedPreferences;

    /**
     * UI Elements.
     **/
    EditText nameEditText;
    ListView deviceListView;
    Button scanButton;
    ProgressBar progressBar;

    /**
     * ListView elements.
     */
    DeviceListAdapter deviceListAdapter;


    /*
     * Initialize this activity's broadcast receiver.
     */
    private BroadcastReceiver actionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {

                /*
                 * Bluetooth Discovery has Started.
                 */
                case DeviceInteractionService.BLUETOOTH_ACTION_DISCOVERY_STARTED:
                    break;

                /*
                 * Bluetooth Discovery has Completed.
                 */
                case DeviceInteractionService.BLUETOOTH_ACTION_DISCOVERY_FINISHED:

                    Log.d("MainActivity","FINISHED");

                    /**
                     * Stop progress bar and reset scanning flag.
                     *
                     * Protect against secondary broadcast.
                     */
                    if ( flip_flop )
                        flip_flop = !flip_flop;
                    else {
                        if ( scan_status )
                            scan_status = !scan_status;
                        progressBar.setVisibility(View.GONE); /* Eliminate spinning progress bar. */

                        // Reset the string key.
                        keyScan = "";
                    }

                    break;

                /*
                 * Bluetooth Discovery has Found a Device.
                 */
                case DeviceInteractionService.BLUETOOTH_ACTION_DISCOVERY_FOUND:

                    // Obtain access to the Bluetooth Device that was sent through the intent.
                    listAdd( intent.getParcelableExtra(DeviceInteractionService.BLUETOOTH_ITEM_DEVICE) );
                    break;

                /*
                 * A device was selected from the list view.
                 */
                case DeviceListAdapter.ACTION_ITEM_SELECTED:
                    service.setDevice(new OBDAdapter((BluetoothDevice) intent.getParcelableExtra(DeviceInteractionService.BLUETOOTH_ITEM_DEVICE)) );
                    if ( service.connectDevice() ) {
                        Intent activityIntent = new Intent( getApplicationContext(), HUDActivity.class);
                        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(activityIntent);
                    }
                    else
                        Toast.makeText(context, "Could not connect...", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };



    /**
     * Method:
     *      onCreate( Bundle )
     *
     * Description:
     *      Sets up all activity elements upon creation.
     *
     * @param savedInstanceState    Previous saved information for the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Init activity members.
         */
        keyScan = null;

        /**
         * Init Flags.
         */
        scan_status = false;
        flip_flop = false;

        /**
         * Initialize UI elements.
         **/
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        deviceListView = (ListView) findViewById(R.id.deviceListView);
        scanButton = (Button) findViewById(R.id.scanButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        /**
         * Initialize ListView and container of elements.
         */
        deviceListAdapter = new DeviceListAdapter(this, R.layout.obd_item);
        deviceListView.setAdapter(deviceListAdapter);

        /**
         * Start the background service.
         */
        this.createService();

        /**
         * Set Button click listeners.
         */
        scanButton.setOnClickListener(this);
    }


    /**
     * Method:
     *      onResume(  )
     *
     * Description:
     *      Called on the resume section of the activity lifecycle.
     *
     *      Checks if background service needs to be re-bound and registers the activity's broadcast
     *      receiver.
     */
    @Override
    protected void onResume() {
        super.onResume();

        /**
         * Bind with background service.
         */
        if ( serviceStarted() )
            this.bindWithService();
        else
            Log.d("MainActivity","Service not started.");

        /*
         * Register broadcast receiver.
         */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DeviceInteractionService.BLUETOOTH_ACTION_DISCOVERY_FOUND);
        intentFilter.addAction(DeviceInteractionService.BLUETOOTH_ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(DeviceInteractionService.BLUETOOTH_ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(DeviceListAdapter.ACTION_ITEM_SELECTED);
        this.registerReceiver(this.actionReceiver, intentFilter);
    }

    /**
     * Method:
     *      onPause(  )
     *
     * Description:
     *      Called on the pause section of the activity lifecycle.
     *
     *      Stops Bluetooth scanning if possible and unregisters the activity's broadcast receiver.
     */
    @Override
    protected void onPause() {
        super.onPause();

        /*
         * Cancel scanning on activity pause.
         */
        if (scan_status)
        {
            // Reset the string key.
            this.keyScan = "";

            // End scanning.
            this.service.stopScan();

            // Hide the spinning progress bar.
            this.progressBar.setVisibility(View.GONE);

            // Reset the scanning status flag.
            scan_status = !scan_status;
        }
        this.unregisterReceiver(actionReceiver); /* Unregister activity from broadcast receiver. */
    }


    /**
     * Method:
     *      listAdd( Object )
     *
     * Description:
     *      Inserts a specific object into the Bluetooth scanning device list.
     *
     * @param o     Object to be added to the ListView.
     */
    @Override
    public void listAdd(Object o) {

        /**
         * Ensure that the object being added is a Bluetooth device.
         */
        if ( o instanceof BluetoothDevice ) {

            /**
             * Create and add device to ListView if it doesn't already exist.
             */
            BluetoothDevice device = (BluetoothDevice) o;
            Log.d("BT", "Found: " + device.getName());
            if ( (!this.deviceListAdapter.contains(device)) && ( device.getName() != null ) && ( (this.keyScan.equals("")) || (device.getName().toLowerCase().contains(this.keyScan.toLowerCase())) )) {
                this.deviceListAdapter.add(device);
                this.deviceListAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Method:
     *      listAdd( Object )
     *
     * Description:
     *      Does nothing. Required for compilation.
     *
     * @param o     Object to be removed from the ListView.
     */
    @Override
    public void listRemove(Object o) {

    }


    /**
     * Method:
     *      onClick( View )
     *
     * Description:
     *      Called when the user selects something on-screen.
     *
     *      MainActivity operations limited to scanning for Bluetooth devices.
     *
     * @param v     The view which was clicked.
     */
    @Override
    public void onClick(View v) {

        /**
         * Determine what was clicked.
         */
        switch (v.getId()) {

            /**
             * Scanning Button.
             */
            case R.id.scanButton:

                /*
                 * Start scanning.
                 */
                if (!scan_status) {
                    // Clear contents of the adapter to assist in filtering results.
                    this.deviceListAdapter.clear();
                    this.deviceListAdapter.notifyDataSetChanged();

                    // Save the desired string.
                    this.keyScan = this.nameEditText.getText().toString();

                    this.flip_flop = true;

                    // Begin scanning.
                    this.service.startScan();

                    // Make the spinning progress bar visible.
                    this.progressBar.setVisibility(View.VISIBLE);
                }

                /*
                 * End scanning.
                 */
                else {
                    // Reset the string key.
                    this.keyScan = "";

                    flip_flop = false;

                    // End scanning.
                    this.service.stopScan();

                    // Hide the spinning progress bar.
                    this.progressBar.setVisibility(View.GONE);
                }

                // Flip-flop the scanning status flag.
                this.scan_status = !this.scan_status;
        }
    }

    /**
     * Method:
     *      createService(  )
     *
     * Description:
     *      Helper method that creates a new custom device interaction background service.
     */
    public void createService() {
        if (!serviceStarted()) this.startService(new Intent(MainActivity.this, DeviceInteractionService.class));
    }

    /**
     * Method:
     *      bindWithService(  )
     *
     * Description:
     *      Helper method which binds with a custom device interaction background service.
     */
    public void bindWithService() {
        Intent intent = new Intent(MainActivity.this, DeviceInteractionService.class); /* Create new intent. */
        this.bindService(intent, this, Context.BIND_AUTO_CREATE); /* Bind with the service. */
        this.bound = true; /* Change bound status flag value. */
    }


    /**
     * Method:
     *      serviceStarted(  )
     *
     * Description:
     *      Helper method that checks the status of the custom device background service.
     *
     * @return boolean  Status of service start completion.
     */
    public boolean serviceStarted() {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return ( this.sharedPreferences.contains("service_started") && this.sharedPreferences.getBoolean("service_started", false) );
    }

    /**
     * Method:
     *      onServiceConnected( ComponentName, IBinder )
     *
     * Description:
     *      Called automatically when a service has been connected with this activity.
     *
     *      Sets this activity's service reference.
     *
     * @param name      Name of the service application component.
     * @param service   Binder for the service that was connected.
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        DeviceInteractionService.LocalBinder binder = (DeviceInteractionService.LocalBinder) service;
        this.service = binder.getServiceInstance();
        this.service.setBluetoothCallbacks(this);

        Log.d("MainActivity", "Service connected.");
    }


    /**
     * Method:
     *      onServiceDisconnected( ComponentName )
     *
     * Description:
     *      Called automatically when a service has become disconnected from this activity.
     *
     *      Does nothing. Method presence required for compilation.
     *
     * @param name      Name of the service application component.
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d("MainActivity","Service disconnected.");
    }

}
