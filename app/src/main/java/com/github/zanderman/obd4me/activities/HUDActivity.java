package com.github.zanderman.obd4me.activities;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.zanderman.obd.classes.OBDAdapter;
import com.github.zanderman.obd4me.R;



/**
 * Class:
 *      HUDActivity
 *
 * Description:
 *      ...
 *
 * Author:
 *      Alexander DeRieux
 */
public class HUDActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Public members.
     */
    OBDAdapter device;
    TextView tempNameTextView;
    TextView tempAddressTextView;
    Button disconnectButton;
    Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hud);

        /**
         * Initialize UI objects.
         */
        tempNameTextView = (TextView) findViewById(R.id.tempNameTextView);
        tempAddressTextView = (TextView) findViewById(R.id.tempAddressTextView);
        disconnectButton = (Button) findViewById(R.id.disconnectButton);
        connectButton = (Button) findViewById(R.id.connectButton);

        // Create OBDAdapter from the passed BluetoothDevice.
        this.device = new OBDAdapter((BluetoothDevice) getIntent().getParcelableExtra("device"));

        // print device name.
        tempNameTextView.setText(device.name);
        tempAddressTextView.setText(device.address);

        /**
         * Set 'Disconnect' listener.
         */
        disconnectButton.setOnClickListener(this);
        connectButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        /**
         * Determine which button was pressed.
         */
        switch (v.getId()) {

            // Disconnection was selected.
            case R.id.disconnectButton:

                // Disconnect the OBDAdapter object.
                this.device.disconnect();

                // Complete this activity.
                finish();

                break;

            // Connection was selected.
            case R.id.connectButton:
                if (this.device.connect())
                    Log.d("HUD","Connected");
                else
                    Log.d("HUD","Failed...");
        }
    }
}
