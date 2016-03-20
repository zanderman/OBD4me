package com.github.zanderman.obd.classes;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.Serializable;
import java.util.UUID;

/**
 * Class:
 *      OBDAdapter
 *
 * Description:
 *      Primary object representation for a physical OBD-II adapter.
 *
 * Author:
 *      Alexander DeRieux
 */
@SuppressWarnings("serial")
public class OBDAdapter implements Serializable {

    /**
     * Private Members
     */
    private BluetoothAdapter adapter;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private UUID uuid;

    /**
     * Public Members
     */
    public String name;     // String name listed in a BT scan.
    public String address;  // MAC address.

    /**
     * Constructor:
     *      OBDAdapter( BluetoothDevice )
     *
     * Description:
     *      Creates new OBDAdapter object by maintaining reference
     *      to a specific BluetoothDevice.
     *
     * @param   device  BluetoothDevice object.
     */
    public OBDAdapter(BluetoothDevice device) {
        super();

        // Configure the adapter.
        this.configure(device);
    }

    /**
     * Method:
     *      configure( BluetoothDevice )
     *
     * Description:
     *      Helper method to setup configuration of this OBD Adapter object.
     *
     * @param   device      BluetoothDevice object.
     * @return  boolean     Configuration success status.
     */
    private boolean configure(BluetoothDevice device) {

        // Set the parameters of the device.
        this.name = device.getName();
        this.address = device.getAddress();
        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.device = device;
        this.socket = null;

        // Generate a random UUID for the device to connect with.
        this.uuid = UUID.randomUUID();

        // Return configuration success.
        return (true);
    }


    /**
     * Method:
     *      connect( )
     *
     * Description:
     *      Connect to Bluetooth OBD adapter using its MAC address.
     *
     * @return  boolean     Connection success status.
     */
    public boolean connect() {

        try {
            // Bluetooth NOT supported.
            if (this.adapter == null) {
                return (false);
            }
            // Bluetooth is supported.
            else {
                // Gain access to the actual device.
                this.device = this.adapter.getRemoteDevice(this.address);

                // Create a communications socket with the device.
                this.socket =(BluetoothSocket) this.device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(this.device,1);

                /*
                 * Bluetooth connection is very intensive,
                 * placing it within a separate thread unloads
                 * the main app of the processing needed.
                 */
                Thread connectionThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socket.connect();
                            Log.d("connectionThread", "Connected");
                        } catch (Exception c) {
                            Log.d("connectionThread",c.toString());
                            try {
                                socket.close();
                                Log.d("connectionThread", "Disconnected");
                            } catch (Exception d) {
                                Log.d("connectionThread",d.toString());
                            }
                        }
                    }
                });
                connectionThread.start();

                // Connection worked out correctly.
                return (true);
            }
        } catch (Exception e) {
            Log.d("adapter", e.toString());

            // Disconnect from the device.
            this.disconnect();

            // Connection failure.
            return (false);
        }
    }


    public boolean disconnect() {

        try {
            // Bluetooth NOT supported.
            if (this.adapter == null) {
                return (false);
            }
            // Bluetooth is supported.
            else {

                // Close connect to the Bluetooth socket.
                this.socket.close();
                Log.d("disconnectionThread", "Disconnected");

                // Disconnection worked out correctly.
                return (true);
            }
        } catch (Exception e) {
            Log.d("adapter", e.toString());

            // Disconnect failure.
            return (false);
        }
    }


    /**
     * Method:
     *      equals( Object )
     *
     * Description:
     *      Compares name and MAC address of current adapter with another.
     *
     * @param   o           Object to be compared with.
     * @return  boolean     Equality status.
     */
    @Override
    public boolean equals(Object o) {

        super.equals(o);

        /**
         * Null object reference.
         */
        if (o == null)
            return false;

        /**
         * Object is not an instance of OBDAdapter.
         */
        if (!OBDAdapter.class.isAssignableFrom(o.getClass()))
            return false;

        // Cast to OBDAdapter.
        OBDAdapter other = (OBDAdapter) o;

        /**
         * Check object name and MAC address.
         */
        if (!this.name.equals(other.name) || !this.address.equals(other.address))
            return false;

        /**
         * Object passes all tests for equality.
         */
        else
            return true;
    }
}
