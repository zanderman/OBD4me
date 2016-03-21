package com.github.zanderman.obd4me.activities;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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


public class MainActivity extends AppCompatActivity
                            implements /*BluetoothCallbackInterface,*/ View.OnClickListener, ServiceConnection, ListableInterface {

    /**
     * TODO: Create main activity interface
     *  - Scans for OBD devices
     *  - Opens activity for OBD device interaction.
     **/

    /**
     * TODO: add listview for scanned devices.
     *  - populate scanned devices in a list view.
     *  - Each item is clickable
     *      - stores obd device class information.
     *      - Starts next activity
     *          - passes OBD object to the next activity.
     */

    /**
     * TODO: delete item from list if it's not found in next scan.
     */

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

    /**
     * OBD members.
     */
//    OBDManager obdManager;

    /**
     *
     * @param savedInstanceState
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
     *
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Re-register the Bluetooth actions broadcast receiver.
//        this.obdManager.registerBroadcastReceiver(this);
    }

    /**
     *
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
//            this.obdManager.stopScan();

            // Hide the spinning progress bar.
            this.progressBar.setVisibility(View.GONE);

            // Reset the scanning status flag.
            scan_status = !scan_status;
        }

        // De-register the Bluetooth actions broadcast receiver.
//        this.obdManager.unregisterBroadcastReceiver(this);
    }

    /**
     * TODO: save listview content and state on rotation.
     *      - http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
     */


//    /**
//     *
//     * @param message
//     */
//    @Override
//    public void bluetoothError(String message) {
//        Log.d("BT", "Error.");
//    }


//    /**
//     *
//     */
//    @Override
//    public void discoveryStarted() {
//        Log.d("BT", "Discovery Started.");
//    }

//    /**
//     *
//     */
//    @Override
//    public void discoveryFinished() {
//        Log.d("BT", "Discovery Finished.");
//
//        // Reset the string key.
//        this.keyScan = "";
//
//        /**
//         * Stop progress bar and reset scanning flag.
//         */
//        progressBar.setVisibility(View.GONE);
//
//        /**
//         * Reset stats flag only if needed.
//         */
//        if (scan_status)
//            scan_status = !scan_status;
//    }


//    /**
//     *
//     * @param device
//     */
//    @Override
//    public void discoveryFound(BluetoothDevice device) {
//
//        /**
//         * Create and add device to ListView if it doesn't already exist.
//         */
//        Log.d("BT", "Found: " + device.getName());
//        if ( (!this.deviceListAdapter.contains(device)) && ( device.getName() != null ) && ( (this.keyScan.equals("")) || (device.getName().toLowerCase().contains(this.keyScan.toLowerCase())) )) {
//            this.deviceListAdapter.add(device);
//            this.deviceListAdapter.notifyDataSetChanged();
//        }
//    }


    /**
     * Method:
     *      listAdd( Object )
     *
     * Description:
     *      ...
     *
     * @param o     Object to be added to the listview.
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

    @Override
    public void listRemove(Object o) {

    }


    /**
     * Method:
     *      onClick( View )
     *
     * Description:
     *      Overridden method to handle click actions.
     *
     * @param v     View correlating to what was clicked.
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
                /**
                 * Start scanning.
                 */
                if (!scan_status) {
                    // Clear contents of the adapter to assist in filtering results.
                    this.deviceListAdapter.clear();
                    this.deviceListAdapter.notifyDataSetChanged();

                    // Save the desired string.
                    this.keyScan = this.nameEditText.getText().toString();

                    // Begin scanning.
//                    this.obdManager.startScan();

                    // Make the spinning progress bar visible.
                    this.progressBar.setVisibility(View.VISIBLE);
                }

                /**
                 * End scanning.
                 */
                else {
                    // Reset the string key.
                    this.keyScan = "";

                    // End scanning.
//                    this.obdManager.stopScan();

                    // Hide the spinning progress bar.
                    this.progressBar.setVisibility(View.GONE);
                }

                // Flip-flop the scanning status flag.
                this.scan_status = !this.scan_status;
        }
    }

    /**
     * Method:
     *      createService( )
     *
     * Description:
     *      ...
     */
    public void createService() {
        if (!serviceStarted()) this.startService(new Intent(MainActivity.this, DeviceInteractionService.class));
    }

    /**
     * Method:
     *      bindWithService( )
     *
     * Description:
     *      ...
     */
    public void bindWithService() {
        Intent intent = new Intent(MainActivity.this, DeviceInteractionService.class); /* Create new intent. */
        this.bindService(intent, this, Context.BIND_AUTO_CREATE); /* Bind with the service. */
        this.bound = true; /* Change bound status flag value. */
    }


    /**
     * Method:
     *      serviceStarted( )
     *
     * Description:
     *      ...
     *
     * @return
     */
    public boolean serviceStarted() {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return ( this.sharedPreferences.contains("service_started") && this.sharedPreferences.getBoolean("service_started", false) );
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
