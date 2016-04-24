package com.github.zanderman.obd.interfaces;

/**
 * Interface:
 *      CommunicationCallbackInterface
 *
 * Description:
 *      Public interface for all Bluetooth communication callback method specified by the OBD API.
 */
public interface CommunicationCallbackInterface {

    /**
     * Communication Callbacks.
     */
    public void receive( String packet ); /* Incoming data reception. */
    public void transmit( String packet ); /* Outgoing data transmission. */
}
