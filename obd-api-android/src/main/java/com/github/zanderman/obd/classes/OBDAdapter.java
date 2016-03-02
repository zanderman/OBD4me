package com.github.zanderman.obd.classes;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

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
public class OBDAdapter {

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
                this.socket = this.device.createRfcommSocketToServiceRecord(this.uuid);

                // Connect to the socket.
                socket.connect();

                // Connection worked out correctly.
                return (true);
            }
        } catch (Exception e) {
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
