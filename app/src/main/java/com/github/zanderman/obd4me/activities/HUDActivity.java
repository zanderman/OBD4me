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
 *      Facilitates interaction between the user and a OBD adapter.
 *
 *      Simple transmit/receive actions are done through a textbox and button system.
 *      Received data is displayed in a scrollable text window that can be cleared via a long press.
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


    /*
     * Initialize the activity's broadcast receiver.
     */
    BroadcastReceiver actionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

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

        if ( serviceStarted() )
            this.bindWithService();

        else
            finish();

        /*
         * Register broadcast receiver.
         */
        this.registerReceiver( this.actionReceiver, this.intentFilter );
    }


    /**
     * Method:
     *      onPause(  )
     *
     * Description:
     *      Called on the pause section of the activity lifecycle.
     *
     *      Unbinds from the background service if possible and unregisters the activity's broadcast
     *      receiver.
     */
    @Override
    protected void onPause() {
        super.onPause();

        /*
         * Unbind from background service if possible.
         */
        if ( this.bound ) {
            this.unbindService(this);
            this.bound = false;
        }

        this.unregisterReceiver(this.actionReceiver); /* Unregister the activity's broadcast receiver. */
        Toast.makeText(this.getApplicationContext(),"Disconnected...", Toast.LENGTH_SHORT).show(); /* Inform user that the device is disconnected. */
    }


    /**
     * Method:
     *      onStart(  )
     *
     * Description:
     *      Called on the start section of the activity lifecycle.
     *
     *      Ensures that the OBD device is indeed connected before allowing HUD interaction.
     */
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

            /*
             * Set device information from SharedPreferences.
             */
            tempNameTextView.setText(this.sharedPreferences.getString("device_name", ""));
            tempAddressTextView.setText(this.sharedPreferences.getString("device_address", ""));
        }
    }


    /**
     * Method:
     *      onClick( View )
     *
     * Description:
     *      Called when the user selects something on-screen.
     *
     *      HUD operations limited to transmit/receive button presses.
     *
     * @param v     The view which was clicked.
     */
    @Override
    public void onClick(View v) {

        /**
         * Determine which button was pressed.
         */
        switch (v.getId()) {

            /*
             * Disconnection was selected.
             */
            case R.id.disconnectButton:

                // Complete this activity.
                finish();

                break;

            /*
             * Connection was selected.
             */
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


    /**
     * Method:
     *      onLongClick( View )
     *
     * Description:
     *      Called when the user selects something on-screen via a long press.
     *
     *      HUD operations limited to clearing the receive data log window.
     *
     * @param v         The view which was clicked.
     * @return boolean  Status of click processing.
     */
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


    /**
     * Method:
     *      deviceConnected(  )
     *
     * Description:
     *      Helper method to determine if a OBD adapter is truly connected.
     *
     * @return boolean  Status of device connection.
     */
    public boolean deviceConnected() {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return ( this.sharedPreferences.contains("device_status") && this.sharedPreferences.getBoolean("device_status", false) );
    }


    /**
     * Method:
     *      bindWithService(  )
     *
     * Description:
     *      Helper method that binds the HUD activity with the custom background service.
     */
    public void bindWithService() {
        Intent intent = new Intent(HUDActivity.this, DeviceInteractionService.class); /* Create new intent. */
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
        this.service = ((DeviceInteractionService.LocalBinder) service).getServiceInstance();
        this.service.setCommunicationCallbacks(this);

        Log.d("HUDActivity","Service connected.");
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
        Log.d("HUDActivity","Service disconnected.");
    }
}
