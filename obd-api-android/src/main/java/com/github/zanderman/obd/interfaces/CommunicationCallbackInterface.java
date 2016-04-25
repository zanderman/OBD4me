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
    public String receive(  ); /* Incoming data reception. */
    public boolean transmit( String packet ); /* Outgoing data transmission. */
}
