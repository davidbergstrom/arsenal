package com.edit.reach.model;

/**
 * Created by Joakim on 2014-10-06.
 * A listener for routes. The listener gets notified when the route has been initialized.
 */
public interface RouteListener {

    /**
     * What the listener should do when the route has been initialized.
     */
    public void onInitialization();
}
