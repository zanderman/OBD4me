package com.github.zanderman.obd4me.activities;

import android.bluetooth.BluetoothDevice;
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

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
                            implements BluetoothCallbackInterface {

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
     * Flags
     */
    boolean scan_status;

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
    OBDManager obdManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
         * Initialize OBD members.
         */
        obdManager = new OBDManager();

        /**
         * Set Button click listeners.
         */
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * Start scanning.
                 */
                if (!scan_status){
                    obdManager.startScan();
                    progressBar.setVisibility(View.VISIBLE);
                }

                /**
                 * End scanning.
                 */
                else {
                    obdManager.stopScan();
                    progressBar.setVisibility(View.GONE);
                }

                // Flip-flop the scanning status flag.
                scan_status = !scan_status;
            }
        });
    }

    /**
     * Method:
     *      onStart( )
     *
     * Description:
     *      ...
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Initialize the OBD manager.
        obdManager.init(getApplicationContext(), this);
    }

    /**
     * TODO: save listview content and state on rotation.
     *      - http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
     */









    @Override
    public void bluetoothError(String message) {
        Log.d("BT", "Error.");
    }


    @Override
    public void discoveryStarted() {
        Log.d("BT", "Discovery Started.");
    }

    @Override
    public void discoveryFinished() {
        Log.d("BT", "Discovery Finished.");

        /**
         * Stop progress bar and reset scanning flag.
         */
        progressBar.setVisibility(View.GONE);
        scan_status = !scan_status;
    }

    @Override
    public void discoveryFound(BluetoothDevice device) {

        /**
         * TODO: Check if device name matches.
         */
        // ...

        /**
         * Create and add device to ListView if it doesn't already exist.
         */
        Log.d("MainActivity",device.getName());
        OBDAdapter entry = new OBDAdapter(device);
        if (!this.deviceListAdapter.contains(entry)) {
            this.deviceListAdapter.add(entry);
            this.deviceListAdapter.notifyDataSetChanged();
        }
    }
}
