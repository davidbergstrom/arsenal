package com.edit.reach.model.interfaces;

import com.edit.reach.model.Pause;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Joakim on 2014-10-06.
 * A listener for routes. The listener gets notified when the route has been initialized.
 */
public interface RouteListener {

    /**
     * What the listener should do when the route has been initialized.
     */
    public void onInitialization();

    /**
     * What the listener should do when a pause has been added to the route.
     */
    public void onPauseAdded(Pause pause);
}
