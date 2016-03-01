package com.github.zanderman.obd4me.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.github.zanderman.obd4me.R;


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
     * TODO: put obd-api-android as an external module/library
     **/

    /**
     * UI Elements.
     **/
    EditText nameEditText;
    ListView deviceListView;
    Button scanButton;


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
    }
}
