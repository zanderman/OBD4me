package com.github.zanderman.obd4me.activities;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
    TextView tempTextView;
    Button disconnectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hud);

        /**
         * Initialize UI objects.
         */
        tempTextView = (TextView) findViewById(R.id.tempTextView);
        disconnectButton = (Button) findViewById(R.id.disconnectButton);

        // Create OBDAdapter from the passed BluetoothDevice.
        this.device = new OBDAdapter((BluetoothDevice) getIntent().getParcelableExtra("device"));

        // print device name.
        tempTextView.setText(device.name);

        /**
         * Set 'Disconnect' listener.
         */
        disconnectButton.setOnClickListener(this);
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
                // this.device.disconnect();

                // Complete this activity.
                finish();

                break;
        }
    }
}
