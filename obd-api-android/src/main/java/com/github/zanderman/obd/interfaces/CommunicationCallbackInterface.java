package com.github.zanderman.obd.interfaces;

/**
 * Interface:
 *      CommunicationCallbackInterface
 *
 * Description:
 *      ...
 */
public interface CommunicationCallbackInterface {

    /**
     * Communication Callbacks.
     */
    public void receive( String packet ); /* Incoming data reception. */
    public void transmit( String packet ); /* Outgoing data transmission. */
}
