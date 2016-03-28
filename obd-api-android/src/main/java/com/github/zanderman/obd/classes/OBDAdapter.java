package com.github.zanderman.obd.classes;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.github.zanderman.obd.receivers.OBDReceiver;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
     * Enumerated type for denoting connection status types.
     */
    private enum Status {
        DISCONNECTED,
        CONNECTED
    }

    /**
     * Constants
     */
    private final int TIMEOUT = 5; /* Total number of read-iterations before timing-out. */

    /**
     * Private Members
     */
    private BluetoothAdapter adapter;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private UUID uuid;
    private volatile Status status;
    private Thread receiveThread;

    /**
     * Public Members
     */
    public String name;     // String name listed in a BT scan.
    public String address;  // MAC address.


    /**
     * Shared Variables
     */
    volatile boolean worker_continue;
    byte [] buffer; /* Buffer for read-in message. */
    final byte delimiter = 10; /* ASCII code for newline character. */
    int bufferIndex;
    String receivedMessage;
    int timeoutCounter;


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
        this.status = Status.DISCONNECTED;

        // Generate a random UUID for the device to connect with.
        this.uuid = UUID.randomUUID();

        /*
         * Setup thread-shared variables.
         */
        this.worker_continue = true;

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

                /*
                 * Bluetooth connection is very intensive,
                 * placing it within a separate thread unloads
                 * the main app of the processing needed.
                 */
                Thread connectionThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Gain access to the actual device.
                            device = adapter.getRemoteDevice(address);

                            // Create a communications socket with the device.
                            socket =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);

                            // Attempt to connect to the device.
                            socket.connect();
                            inputStream = socket.getInputStream();
                            outputStream = socket.getOutputStream();

                            // Change status to connected.
                            status = Status.CONNECTED;
                            Log.d("connectionThread", "Connected");

                        } catch (Exception c) {
                            Log.d("connectionThread",c.toString());
                            try {
                                // Disconnect from the device on error.
                                socket.close();

                                // Change status to disconnected.
                                status = Status.DISCONNECTED;
                                Log.d("connectionThread", "Disconnected");

                            } catch (Exception d) {
                                Log.d("connectionThread",d.toString());
                            }
                        }
                    }
                });
                connectionThread.start();
                connectionThread.join(); /* Wait for connection thread to complete. */

                // Connection worked out correctly.
                if ( status == Status.CONNECTED )
                    return (true);

                // Connection failed.
                else
                    return (false);
            }
        } catch (Exception e) {
            Log.d("adapter", e.toString());

            // Disconnect from the device.
            this.disconnect();

            // Connection failure.
            return (false);
        }
    }


    /**
     * Method:
     *      disconnect( )
     *
     * Description:
     *      Disconnects from the Bluetooth device.
     *
     * @return boolean  Status of disconnection.
     */
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
                this.outputStream.close();
                this.inputStream.close();

                /*
                 * Reset worker continuation boolean.
                 */
                worker_continue = false;

                this.status = Status.DISCONNECTED;
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
     *      send( String )
     *
     * Description:
     *      ...
     *
     * @param message   String to be sent to the device.
     * @return boolean  Status of transmission completion.
     */
    public boolean send( String message ) {

        /*
         * Message is not valid.
         */
        if ( message == null )
            return ( false );

        /*
         * Message is valid.
         */
        try {
            /*
             * Ensure message contains a newline delimiter.
             */
            if ( !message.contains("\n") )
                message = message + "\n";

            // Send the data-bytes.
            this.outputStream.write( message.getBytes() );

            // Transmit successful.
            return ( true );

            // TODO: Listen for "OK"?
        } catch ( Exception e ) {
            return ( false );
        }
    }


    /**
     * Method:
     *      receive( )
     *
     * Description:
     *      Obtains incoming data from the Bluetooth device.
     *
     *      Note that the communication interface must be implemented
     *      to obtain the data outside of the helper background thread.
     *
     * @return String   Message obtained from reading the device.
     */
    public String receive() {

        buffer = new byte[1024]; /* Buffer for read-in message. */
        bufferIndex = 0;
        receivedMessage = null;
        timeoutCounter = 0;

        /*
         * Initialize receiver worker thread.
         */
        this.receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {

                /*
                 * Loop until stopped.
                 */
                while ( !worker_continue && (timeoutCounter < TIMEOUT) ) {

                    try {

                        // Obtain the number of bytes currently available.
                        int bytesAvailable = inputStream.available();

                        /*
                         * Incoming data is available for read.
                         */
                        if ( bytesAvailable > 0 ) {
                            /*
                             * Setup byte array and read-in the bytes.
                             */
                            byte[] packet = new byte[bytesAvailable];
                            inputStream.read(packet);

                            /*
                             * Check if any of the received bytes was a delimiter.
                             */
                            for ( int i = 0; i < bytesAvailable; i++ ) {

                                /*
                                 * Copy over the buffer into a String array.
                                 */
                                if ( packet[i] == delimiter ) {
                                    byte [] encodedBytes = new byte[ bufferIndex ];
                                    System.arraycopy(buffer, 0, encodedBytes, 0, encodedBytes.length);
                                    receivedMessage = new String( encodedBytes, "US-ASCII" );
                                    bufferIndex = 0; /* Reset the buffer index. */

                                    Log.d("ReceiveThread", "Got: " + receivedMessage);

                                    /*
                                     * Stop worker operation.
                                     */
                                    worker_continue = false;
                                }
                                else {
                                    buffer[bufferIndex] = packet[i];
                                    bufferIndex++;
                                }
                            }

                            // Reset the counter on a successful read.
                            timeoutCounter = 0;
                        }
                        else
                            timeoutCounter++;
                    } catch ( Exception e ) {

                        /*
                         * Stop worker operation.
                         */
                        worker_continue = false;

                        Log.d("OBDAdapter", "Exception caught in READ operation.");
                    }
                }

                /*
                 * Reset worker continuation boolean for next runthrough.
                 */
                worker_continue = true;
            }
        });

        /*
         * Start the worker thread.
         */
        this.receiveThread.start();

        /*
         * Wait until data is received and return result if possible.
         */
        try {
            this.receiveThread.join();
            return ( receivedMessage );
        } catch ( Exception e )
        {
            return ( null );
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
