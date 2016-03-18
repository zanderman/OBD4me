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
                            implements BluetoothCallbackInterface, View.OnClickListener {

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
         * Initialize OBD members.
         */
        obdManager = new OBDManager();
        obdManager.init(this, this);

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
        this.obdManager.registerBroadcastReceiver(this);
    }

    /**
     *
     */
    @Override
    protected void onPause() {
        super.onPause();

        // De-register the Bluetooth actions broadcast receiver.
        this.obdManager.unregisterBroadcastReceiver(this);
    }

    /**
     * TODO: save listview content and state on rotation.
     *      - http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
     */


    /**
     *
     * @param message
     */
    @Override
    public void bluetoothError(String message) {
        Log.d("BT", "Error.");
    }


    /**
     *
     */
    @Override
    public void discoveryStarted() {
        Log.d("BT", "Discovery Started.");
    }

    /**
     *
     */
    @Override
    public void discoveryFinished() {
        Log.d("BT", "Discovery Finished.");

        // Reset the string key.
        this.keyScan = null;

        /**
         * Stop progress bar and reset scanning flag.
         */
        progressBar.setVisibility(View.GONE);
        scan_status = !scan_status;
    }


    /**
     *
     * @param device
     */
    @Override
    public void discoveryFound(BluetoothDevice device) {

        /**
         * Create and add device to ListView if it doesn't already exist.
         */
        Log.d("BT", "Name: " + device);
        if (!this.deviceListAdapter.contains(device)) {
            if ( (this.keyScan == null) || (this.keyScan.equals("")) || (device.getName().equals(this.keyScan)) ) {
                this.deviceListAdapter.add(device);
                this.deviceListAdapter.notifyDataSetChanged();
            }
        }
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
                if (!scan_status){
                    // Save the desired string.
                    this.keyScan = this.nameEditText.getText().toString();

                    // Begin scanning.
                    this.obdManager.startScan();

                    // Make the spinning progress bar visible.
                    this.progressBar.setVisibility(View.VISIBLE);
                }

                /**
                 * End scanning.
                 */
                else {
                    // Reset the string key.
                    this.keyScan = null;

                    // End scanning.
                    this.obdManager.stopScan();

                    // Hide the spinning progress bar.
                    this.progressBar.setVisibility(View.GONE);
                }

                // Flip-flop the scanning status flag.
                this.scan_status = !this.scan_status;
        }
    }
}
