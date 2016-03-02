package com.github.zanderman.obd4me.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.github.zanderman.obd.classes.OBDAdapter;
import com.github.zanderman.obd4me.R;
import com.github.zanderman.obd4me.adapters.DeviceListAdapter;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

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
     * UI Elements.
     **/
    EditText nameEditText;
    ListView deviceListView;
    Button scanButton;

    /**
     * Public members.
     */
    ArrayList<OBDAdapter> devices;
    DeviceListAdapter deviceListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Initialize UI elements.
         **/
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        deviceListView = (ListView) findViewById(R.id.deviceListView);
        scanButton = (Button) findViewById(R.id.scanButton);

        /**
         * Initialize members.
         */
        devices = new ArrayList<OBDAdapter>();
        deviceListAdapter = new DeviceListAdapter(this, 0, devices);
    }



    /**
     * TODO: save listview content and state on rotation.
     *      - http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
     */


    /**
     * Method:
     *      onSaveInstanceState( Bundle )
     *
     * Description:
     *      ...
     *
     * @param outState  Bundle containing everything desired for preservation.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        /**
         * TODO: put list information to bundle.
         */
    }


    /**
     * Method:
     *      onRestoreInstanceState( Bundle )
     *
     * Description:
     *      ...
     *
     * @param savedInstanceState    Preserved data.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        /**
         * TODO: Put data back into listview.
         */
    }
}
