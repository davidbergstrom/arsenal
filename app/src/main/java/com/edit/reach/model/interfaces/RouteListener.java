package com.edit.reach.model.interfaces;

import com.edit.reach.model.Leg;
import com.edit.reach.model.Pause;
import com.edit.reach.model.Step;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Joakim on 2014-10-06.
 * A listener for routes. The listener gets notified when the route has been initialized.
 */
public interface RouteListener {

    /**
     * What the listener should do when the route has been initialized.
     * @param success, whether the initialization was successful or not
     */
    public void onInitialization(boolean success);

    /**
     * What the listener should do when a pause has been added to the route.
     */
    public void onPauseAdded(Pause pause);

    /**
     * What the listener should do when a leg has been completed.
     * @param finishedLeg, the leg that has been finished
     */
    public void onLegFinished(Leg finishedLeg);

    /**
     * What the listener should do when a step has been completed.
     * @param finishedStep, the step that has been finished
     */
    public void onStepFinished(Step finishedStep);
}
