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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.zanderman.obd.classes.OBDAdapter;
import com.github.zanderman.obd.interfaces.CommunicationCallbackInterface;
import com.github.zanderman.obd4me.R;
import com.github.zanderman.obd4me.adapters.DeviceListAdapter;
import com.github.zanderman.obd4me.services.DeviceInteractionService;


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
public class HUDActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener, ServiceConnection {

    /**
     * Public members.
     */
    TextView tempNameTextView;
    TextView tempAddressTextView;
    TextView loggerView;
    Button disconnectButton;
    Button transmitButton;
    DeviceInteractionService service;
    EditText commandEditText;

    /**
     * Shared Preferences.
     */
    SharedPreferences sharedPreferences;

    /**
     * Flags
     */
    boolean bound; /* Denotes current binding status with background service. */

    /**
     * Intent Filters.
     */
    IntentFilter intentFilter;


    BroadcastReceiver actionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hud);

        /**
         * Initialize UI objects.
         */
        tempNameTextView = (TextView) findViewById(R.id.tempNameTextView);
        tempAddressTextView = (TextView) findViewById(R.id.tempAddressTextView);
        loggerView = (TextView) findViewById(R.id.loggerView);
        disconnectButton = (Button) findViewById(R.id.disconnectButton);
        transmitButton = (Button) findViewById(R.id.transmitButton);
        commandEditText = (EditText) findViewById(R.id.editText);

        /**
         * Set 'Disconnect' listener.
         */
        disconnectButton.setOnClickListener(this);
        transmitButton.setOnClickListener(this);
        loggerView.setOnLongClickListener(this);

        /**
         * Set logger view as scrollable.
         */
        loggerView.setMovementMethod(new ScrollingMovementMethod());

        /**
         * Set intent filters.
         */
        this.intentFilter = new IntentFilter();
        this.intentFilter.addAction(DeviceInteractionService.COMMUNICATION_ACTION_RECEIVE);
        this.intentFilter.addAction(DeviceInteractionService.COMMUNICATION_ACTION_TRANSMIT);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if ( serviceStarted() )
            this.bindWithService();

        else
            finish();

        /*
         * Register broadcast receiver.
         */
        this.registerReceiver( this.actionReceiver, this.intentFilter );
    }


    @Override
    protected void onPause() {
        super.onPause();

        if ( this.bound ) {
            this.unbindService(this);
            this.bound = false;
        }

        this.unregisterReceiver(this.actionReceiver);

        Toast.makeText(this.getApplicationContext(),"Disconnected...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*
         * Ensure the bluetooth device is connected before starting the HUD.
         */
        if ( !deviceConnected() )
            finish();
        else {
            this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            tempNameTextView.setText(this.sharedPreferences.getString("device_name", ""));
            tempAddressTextView.setText(this.sharedPreferences.getString("device_address", ""));
        }
    }

    @Override
    public void onClick(View v) {

        /**
         * Determine which button was pressed.
         */
        switch (v.getId()) {

            // Disconnection was selected.
            case R.id.disconnectButton:

                // Complete this activity.
                finish();

                break;

            // Connection was selected.
            case R.id.transmitButton:

                // Outgoing string.
                String out = commandEditText.getText().toString();

                // Send the string.
                boolean result = this.service.post(out);

                /*
                 * Get incoming string if possible.
                 */
                if (result) {
                    String message = this.service.get();
                    if ( !message.equals("") )
                        loggerView.append(message + "\n");
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        /**
         * Determine which button was pressed.
         */
        switch (v.getId()) {

            /*
             * Clear data logger on long press.
             */
            case R.id.loggerView:
                loggerView.setText(null);
                return true;

            /*
             * Not recognized.
             */
            default:
                return false;
        }
    }


    public boolean deviceConnected() {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return ( this.sharedPreferences.contains("device_status") && this.sharedPreferences.getBoolean("device_status", false) );
    }


    /**
     * Method:
     *      bindWithService( )
     *
     * Description:
     *      ...
     */
    public void bindWithService() {
        Intent intent = new Intent(HUDActivity.this, DeviceInteractionService.class); /* Create new intent. */
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
        this.service = ((DeviceInteractionService.LocalBinder) service).getServiceInstance();
        this.service.setCommunicationCallbacks(this);

        Log.d("HUDActivity","Service connected.");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d("HUDActivity","Service disconnected.");
    }
}
