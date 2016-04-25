package com.github.zanderman.obd4me.interfaces;

/**
 * Class:
 *      ListableInterface
 *
 * Description:
 *      Custom interface for interacting with list objects.
 *
 * Author:
 *      Alexander DeRieux
 */
public interface ListableInterface {

    /**
     * Methods.
     */
    public void listAdd( Object o ); /* Add an object to a list. */
    public void listRemove( Object o ); /* Remove an object from a list. */
}
