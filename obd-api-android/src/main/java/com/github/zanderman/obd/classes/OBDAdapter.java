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
    private final int TIMEOUT = 50; /* Total number of read-iterations before timing-out. */
    private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); /* Required UUID for OBD Bluetooth connection. */

    /**
     * Private Members
     */
    private BluetoothAdapter adapter;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
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
    final byte delimiter = ((byte) '>');/* ASCII code for newline character. */
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
                            socket = device.createRfcommSocketToServiceRecord(uuid);

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
                if ( this.socket.isConnected() && status == Status.CONNECTED )
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
     *      Transmits a string to the Bluetooth device currently connected.
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
            message.replace("\n", "").replace("\r", ""); /* Remove any newline and carriage return characters. */
            message = message + "\r\n"; /* Add required line terminator to 'cleaned' message string. */

            // Send the data-bytes.
            this.outputStream = socket.getOutputStream();
            this.outputStream.write(message.getBytes());
            this.outputStream.flush();

            /*
             * Slight delay (in [ms]) in sending to account for emptying of buffer.
             */
            try{ Thread.sleep(100); }catch(InterruptedException e){ }

            // Transmit successful.
            return ( true );
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

        /*
         * Initialize all required reception variables.
         */
        buffer = new byte[1024]; /* Buffer for read-in message. */
        bufferIndex = 0;
        receivedMessage = "";
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
                while ( worker_continue && (timeoutCounter < TIMEOUT) ) {

                    try {

                        // Obtain the number of bytes currently available.
                        inputStream = socket.getInputStream();
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

                                // If byte is carriage return, don't add it.
                                if (packet[i] == ((byte) '\r'))
                                    continue;

                                // If byte is ending character, don't add it.
                                // Also, quit the loop.
                                if (packet[i] == delimiter)
                                    break;

                                /*
                                 * Add byte to message if its not the last character.
                                 */
                                if ((!receivedMessage.equals("")) || (packet[i] != delimiter))
                                    receivedMessage = receivedMessage + ((char)packet[i]);
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
            worker_continue = false;
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
