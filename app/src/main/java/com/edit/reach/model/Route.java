package com.edit.reach.model;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.util.Log;
import android.util.Property;
import com.edit.reach.app.R;
import com.edit.reach.model.interfaces.IMilestone;
import com.edit.reach.model.interfaces.MilestonesReceiver;
import com.edit.reach.model.interfaces.RouteListener;
import com.edit.reach.system.GoogleMapsEndpoints;
import com.edit.reach.system.Remote;
import com.edit.reach.system.ResponseHandler;
import com.edit.reach.utils.LatLngInterpolator;
import com.edit.reach.utils.NavigationUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Joakim Berntsson on 2014-10-01.
 * Route class containing information for one route
 */
public class Route {
    private List<RouteListener> listeners;
    private List<IMilestone> milestones;
    private List<Leg> legs;
    private List<Pause> pauses;
    private Circle endPointCircle, startPointCircle;
    private GroundOverlay pointerWithBearing;
    private LatLng origin, destination;
    private String originAddress, destinationAddress;
    private boolean initialized, demoMode;   // Specifies if the route should run in test mode
    private String DEBUG_TAG = "Route";

    /** Handler for receiving a route as JSON Object */
    private ResponseHandler routeHandler = new ResponseHandler() {
        @Override
        public void onGetSuccess(JSONObject json) {
            try {
                Log.d(DEBUG_TAG, "New routeHandler call!");
                JSONArray routeArray = json.getJSONArray("routes");
                JSONObject route = routeArray.getJSONObject(0);
                JSONArray arrayLegs = route.getJSONArray("legs");
                JSONArray wayPointOrder = route.getJSONArray("waypoint_order");
                List<Integer> milestoneOrder = new ArrayList<Integer>();
                for(int i = 0; i < wayPointOrder.length(); i++){
                    milestoneOrder.add(wayPointOrder.getInt(i));
                }
                for(int i = 0; i < arrayLegs.length(); i++) {
                    JSONObject legJSON = arrayLegs.getJSONObject(i);
                    Leg newLeg = new Leg(legJSON);
                    // Get the milestone associated
                    if(milestoneOrder.size() - 1 >= i){
                        // Set the milestone to a milestone with the endlocation of the leg
                        newLeg.setMilestone(milestones.get(milestoneOrder.get(i)));
                    }

                    legs.add(newLeg);
                }
                // Notify the observers that the route has been initialized
                onInitialize(legs.size() > 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onGetFail() {
            onInitialize(false); // Notify the observers that the route has been initialized
        }
    };

    /** Handler for receiving the origin address as a JSON Object */
    private ResponseHandler originHandler = new ResponseHandler() {
        @Override
        public void onGetSuccess(JSONObject json) {
            try {
                JSONArray originArray = json.getJSONArray("results");
                JSONObject address = originArray.getJSONObject(0);
                JSONObject geometry = address.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                origin = (new LatLng(location.getDouble("lat"), location.getDouble("lng")));
                Log.d(DEBUG_TAG, "Origin coordinate retrieved.");

                URL url = GoogleMapsEndpoints.makeURLGeocode(destinationAddress);
                Remote.get(url, destinationHandler);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onGetFail() {
            Log.d(DEBUG_TAG, "Receiving origin failed.");
        }
    };

    /** Handler for receiving the destination address as a JSON Object */
    private ResponseHandler destinationHandler = new ResponseHandler() {
        @Override
        public void onGetSuccess(JSONObject json) {
            try {
                JSONArray destinationArray = json.getJSONArray("results");
                JSONObject address = destinationArray.getJSONObject(0);
                JSONObject geometry = address.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                destination = (new LatLng(location.getDouble("lat"), location.getDouble("lng")));
                Log.d(DEBUG_TAG, "Destination coordinate retrieved.");

                URL url = GoogleMapsEndpoints.makeURL(origin, destination, new ArrayList<IMilestone>(), true);
                Remote.get(url, routeHandler);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onGetFail() {
            Log.d(DEBUG_TAG, "Receiving destination failed.");
        }
    };

    /**
     * Create an empty route.
     */
    public Route(){
        listeners = new ArrayList<RouteListener>();
        legs = new ArrayList<Leg>();
        milestones = new ArrayList<IMilestone>();
        pauses = new ArrayList<Pause>();
        initialized = false;
        demoMode = true;
    }

    /**
     * Create a route with the provided origin and destination.
     * @param origin, the start of the route
     * @param destination, the end of the route
     */
    public Route(LatLng origin, LatLng destination){
        this();
        this.origin = origin;
        this.destination = destination;
        URL url = GoogleMapsEndpoints.makeURL(origin, destination, new ArrayList<IMilestone>(), true);
        Remote.get(url, routeHandler);
    }

    /**
     * Create a route with the provided origin and destination.
     * @param origin, the start of the route
     * @param destination, the end of the route
     */
    public Route(String origin, String destination){
        this();
        this.originAddress = origin;
        this.destinationAddress = destination;
        URL url = GoogleMapsEndpoints.makeURLGeocode(origin);
        Remote.get(url, originHandler);
    }

    /**
     * Create a route with a coordinate origin and a string address.
     * @param origin, the coordinate of the start point
     * @param destination, the destination address
     */
    public Route(LatLng origin, String destination){
        this();
        this.origin = origin;
        this.destinationAddress = destination;
        URL url = GoogleMapsEndpoints.makeURLGeocode(destinationAddress);
        Remote.get(url, destinationHandler);
    }

    /**
     * Returns the approximated duration of the route.
     * @return number of seconds the route will take
     */
    public long getDuration(){
        float durationInSeconds = 0;
        for(Leg leg : legs){
            durationInSeconds += leg.getDuration();
        }
        return (long)durationInSeconds;
    }

    /**
     * Returns the distance of the route.
     * @return the number of metres the route is
     */
    public long getDistance(){
        float distanceInMetres = 0;
        for(Leg leg : legs){
            distanceInMetres += leg.getDistance();
        }
        return (long)distanceInMetres;
    }

    /**
     * Returns if demonstration mode is on.
     * @return true if demo is on, false otherwise
     */
    boolean isDemoMove(){
        return demoMode;
    }

    /**
     * Set the demo mode.
     * @param on the new state of demo mode
     */
    void setDemoMode(boolean on){
        demoMode = on;
    }

    /**
     * Get the milestone at the specified location
     * @param location, the location to find a milestone on.
     * @return the milestone
     */
    public IMilestone getMilestone(LatLng location){
        for(Pause pause : pauses){
            for(IMilestone milestone : pause.getMilestones()){
                if(milestone.getLocation().equals(location)){
                    return milestone;
                }
            }
        }
        return null;
    }

    /**
     * Adds a rest pause the specified number of seconds into the route
     * @param secondsIntoRoute, in seconds
     */
    public void addPause(long secondsIntoRoute, final Pause.PauseType typeOfPause) {
        long actualSecondsIntoRoute = 0;

        outerLoop:
        for(Leg leg : legs){
            for(Step step : leg.getSteps()){
                List<LatLng> subSteps = step.getSubSteps();
                float stepDuration = step.getDuration();
                double subDuration = ((double)stepDuration) / (subSteps.size()-1);
                actualSecondsIntoRoute += stepDuration;
                if(actualSecondsIntoRoute >= secondsIntoRoute){
                    int subIndexTooFar = (int)((actualSecondsIntoRoute - secondsIntoRoute) / subDuration);
                    int subIndex = subSteps.size() - 1 - subIndexTooFar;

                    final LatLng pauseLocation = subSteps.get(subIndex);
                    final double addedTime = actualSecondsIntoRoute - subIndexTooFar * subDuration;

                    // Start a request for milestones at the pause location. Then create the pause with the location and milestones.
                    Ranking.getMilestones(new MilestonesReceiver() {
                        @Override
                        public void onMilestonesRecieved(ArrayList<IMilestone> milestones) {
                            Pause p = new Pause(pauseLocation, milestones, typeOfPause);
                            pauses.add(p);

                            for(RouteListener l : listeners){
                                l.onPauseAdded(p);
                            }
                            Log.d(DEBUG_TAG, "Pause added " + addedTime + " seconds into the route.");
                        }

                        @Override
                        public void onMilestonesGetFailed() {
	                        Log.d(DEBUG_TAG, "onMilestonesGetFailed");
                        }
                    }, pauseLocation, NavigationUtil.RADIUS_IN_DEGREES*2);

                    break outerLoop;
                }
            }
        }
    }

    /**
     * Returns the coordinate on the route the number of seconds into route specified.
     * @param secondsIntoRoute how many seconds into the route to find the coordinate on
     * @return the coordinate, null if there was an overflow and no coordinate could be found
     */
    public LatLng getLocation(long secondsIntoRoute){
        long actualSecondsIntoRoute = 0;
        LatLng location = null;

        outerLoop:
        for(Leg leg : legs){
            for(Step step : leg.getSteps()){
                List<LatLng> subSteps = step.getSubSteps();
                float stepDuration = step.getDuration();
                double subDuration = ((double)stepDuration) / (subSteps.size()-1);
                actualSecondsIntoRoute += stepDuration;
                if(actualSecondsIntoRoute >= secondsIntoRoute){
                    int subIndexTooFar = (int)((actualSecondsIntoRoute - secondsIntoRoute) / subDuration);
                    int subIndex = subSteps.size() - 1 - subIndexTooFar;

                    location = subSteps.get(subIndex);

                    break outerLoop;
                }
            }
        }
        return location;
    }

    /**
     * Retrieve the pauses on this route.
     * @return the pauses
     */
    public List<Pause> getPauses(){
        return pauses;
    }

    /**
     * Remove all pausesLocations from this route.
     */
    public void removeAllPauses() {
        for(Pause p : pauses){
            p.erase();
        }
        pauses.clear();
    }

    /**
     * Draw all pauses.
     * @param map, the GoogleMap to draw on
     */
    public void drawPauses(GoogleMap map){
        Log.d(DEBUG_TAG, "Drawing pauses " + pauses.size());
        for(Pause pause : pauses){
            pause.draw(map);
        }
    }

    /**
     * Set a new destination to this route. This will cause the route to erase itself, reinitialize and
     * can't be used until done loading.
     * @param destination, the new destination
     */
    public void setDestination(LatLng destination){
        this.erase();
        this.destination = destination;
        initialized = false;
        URL url = GoogleMapsEndpoints.makeURL(this.origin, destination, new ArrayList<IMilestone>(), true);
        Remote.get(url, routeHandler);
    }

    /**
     * Get the destination of the route
     * @return the latitude longitude of the route
     */
    public LatLng getDestination(){
        return destination;
    }

    /**
     * Returns the destination address.
     * @return the address
     */
    public String getDestinationAddress(){
        return destinationAddress;
    }

    /**
     * Get the initial origin of the route
     * @return the latitude longitude of the route
     */
    public LatLng getOrigin(){
        return origin;
    }

    /**
     * Returns the initial origin address
     * @return the address
     */
    public String getOriginAddress(){
        return originAddress;
    }

    /**
     * Add a milestone to the route
     * @param milestone, the milestone to add
     */
    public void addMilestone(IMilestone milestone){
        Log.d(DEBUG_TAG, "Adding milestone");
        // New origin
        this.origin = legs.get(0).getSteps().get(0).getSubSteps().get(0);
        this.erase();
        legs.clear();
        pauses.clear();
        milestones.add(milestone);
        // Recalculate the route
        initialized = false;
        URL url = GoogleMapsEndpoints.makeURL(origin, destination, milestones, true);
        Remote.get(url, routeHandler);
    }

    /**
     * Add all the milestones to the route
     * @param milestones, the milestones to add
     */
    public void addMilestones(List<IMilestone> milestones){
        Log.d(DEBUG_TAG, "Adding milestones");
        if(milestones.size() > 0){
            // New origin
            this.origin = legs.get(0).getSteps().get(0).getSubSteps().get(0);
            this.erase();
            legs.clear();
            pauses.clear();
            this.milestones.addAll(milestones);
            // Recalculate the route
            initialized = false;
            URL url = GoogleMapsEndpoints.makeURL(origin, destination, this.milestones, true);
            Log.d(DEBUG_TAG, "URL: "+url);
            Remote.get(url, routeHandler);
        }
    }

    /**
     * Remove the milestone from the route
     * @param milestone, the milestone to remove
     */
    public void removeMilestone(IMilestone milestone){
        this.erase();
        milestones.remove(milestone);
        // Recalculate the route
        initialized = false;
        URL url = GoogleMapsEndpoints.makeURL(origin, destination, milestones, true);
        Remote.get(url, routeHandler);
    }

    /**
     * Remove all of the routes milestones
     */
    public void removeAllMilestones(){
        this.erase();
        milestones.clear();
        // Recalculate the route
        initialized = false;
        URL url = GoogleMapsEndpoints.makeURL(origin, destination, milestones, true);
        Remote.get(url, routeHandler);
    }

    /**
     * Draw this route in navigation view on the map provided
     * @param map, the map to draw on
     */
    public void drawNavigation(GoogleMap map){
        this.erase();
        for(Leg leg : legs){
            leg.draw(map);
        }
        for(Pause pause : pauses){
            pause.drawNavigation(map);
        }
        // Add an end point
        this.endPointCircle = map.addCircle(new CircleOptions()
                .center(legs.get(legs.size()-1).getEndLocation())
                .radius(100)
                .strokeWidth(0)
                .fillColor(0x99ff3333));

        //this.pointer = map.addCircle(new CircleOptions().center(legs.get(0).startLocation).fillColor(Color.GREEN).radius(8));
        pointerWithBearing = map.addGroundOverlay(new GroundOverlayOptions()
                .position(legs.get(0).getStartLocation(), (float) 40)
                .zIndex(2)
                .image(BitmapDescriptorFactory.fromResource(R.drawable.nav_arrow))
                .bearing((float)10));

        Log.d(DEBUG_TAG, "Drawing route in navigation mode.");
    }

    /**
     * Draw this route in an overview on the map provided
     * @param map, the map to draw on
     */
    public void drawOverview(GoogleMap map){
        this.erase();
        drawPauses(map);
        for(Leg leg : legs){
            leg.draw(map);
        }
        // Add an end point
        this.endPointCircle = map.addCircle(new CircleOptions()
                .center(legs.get(legs.size()-1).getEndLocation())
                .radius(2000)
                .strokeWidth(0)
                .fillColor(0x99ff3333));

        this.startPointCircle = map.addCircle(new CircleOptions()
                .center(legs.get(0).getStartLocation())
                .radius(2000)
                .strokeWidth(0)
                .fillColor(0x9999cc00));

        Log.d(DEBUG_TAG, "Drawing route in overview mode.");
    }

    /**
     * Remove this route from all of its maps
     */
    public void erase(){
        for(Leg leg : legs){
            leg.erase();
        }
        for(Pause pause : pauses){
            pause.erase();
        }
        if(endPointCircle != null){
            endPointCircle.remove();
            endPointCircle = null;
        }
        if(startPointCircle != null){
            startPointCircle.remove();
            startPointCircle = null;
        }
        if(pointerWithBearing != null){
            pointerWithBearing.setImage(BitmapDescriptorFactory.defaultMarker());
            pointerWithBearing.remove();
            pointerWithBearing = null;
        }
        Log.d(DEBUG_TAG, "Erased route.");
    }

    /**
     * Get the legs of this route. A leg is a part of the route.
     * @return the legs
     */
    public List<Leg> getLegs(){
        return legs;
    }

    /**
     * Go to the provided location on the route.
     * @param location, the location to move to
     */
    public void goTo(LatLng location){
        if(demoMode){
            location = getNextSubStep();
        }

        LatLng nearestLocation = legs.get(0).getSteps().get(0).getSubSteps().get(0);

        outerLoop:
        for(Iterator<Leg> iteratorLeg = legs.iterator(); iteratorLeg.hasNext(); ){
            Leg leg = iteratorLeg.next();
            for(Iterator<Step> iteratorStep = leg.getSteps().iterator(); iteratorStep.hasNext(); ){
                Step step = iteratorStep.next();
                List<LatLng> subSteps = step.getSubSteps();

                for(int i = 0; i < subSteps.size() - 1; i++){
                    LatLng subStep = subSteps.get(i);
                    LatLng subStepTwo = subSteps.get(i + 1);

                    double distance1 = NavigationUtil.getDistance(subStep, location);
                    double distance2 = NavigationUtil.getDistance(location, subStepTwo);

                    if(distance2 <= distance1){
                        nearestLocation = subStepTwo;
                        subSteps.remove(i);

                    }else{   // If the new point is also closer to current location then the end of step
                        //step.draw(map);
                        break outerLoop; // Break the outer loop
                    }
                }
                step.erase();
                iteratorStep.remove();
                onStepFinished(step);
            }
            leg.erase();
            iteratorLeg.remove();
            onLegFinished(leg);
        }

        if(legs.size() == 0){
            // TODO Route finished!
            onRouteFinished();

        }else{
            if(NavigationUtil.getDistance(location, nearestLocation) > 0.5){    // If the nearest location is more than 500 metres away from the the real location, then reinitialize route
                //this.origin = location;
                //URL url = GoogleMapsEndpoints.makeURL(location, destination, milestones, true);
                //Remote.get(url, routeHandler);
            }
            if(pointerWithBearing != null && !pointerWithBearing.getPosition().equals(nearestLocation)){
                // Calculate new bearing and rotate the camera
                LatLng nextLocation = getNextSubStep();
                float bearing = NavigationUtil.finalBearing(nextLocation, nearestLocation);
                pointerWithBearing.setBearing(bearing);

	            // pointerWithBearing.setPosition(nearestLocation);

                final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Linear();

                TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
                    @Override
                    public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                        return latLngInterpolator.interpolate(fraction, startValue, endValue);
                    }
                };

                Property<GroundOverlay, LatLng> property = Property.of(GroundOverlay.class, LatLng.class, "position");
                ObjectAnimator animator = ObjectAnimator.ofObject(pointerWithBearing, property, typeEvaluator, nearestLocation);
                animator.setDuration(NavigationUtil.UPDATE_INTERVAL_FAST);
                animator.start();
            }
        }
    }

    /**
     * Returns the bearing of the pointer.
     * @return the bearing
     */
    float getPointerBearing(){
        return pointerWithBearing.getBearing();
    }

    /**
     * Returns the position of the pointer.
     * @return the coordinate
     */
    LatLng getPointerLocation(){
        return pointerWithBearing.getPosition();
    }

    private LatLng getNextSubStep(){
        LatLng location;
        Leg currentLeg = legs.get(0);
        Step currentStep = currentLeg.getSteps().get(0);
        List<LatLng> currentSubSteps = currentStep.getSubSteps();
        if(currentSubSteps.size() < 2){
            if(currentLeg.getSteps().size() < 2){
                if(legs.size() < 2){
                    // Route finished
                    location = currentSubSteps.get(0);
                }else{
                    location = legs.get(1).getSteps().get(0).getSubSteps().get(0);
                }
            }else{
                location = currentLeg.getSteps().get(1).getSubSteps().get(0);
            }
        }else{
            location = currentSubSteps.get(1);
        }
        return location;
    }

    /**
     * Determines if the route has been initialized.
     * @return true if the route has been initialized, false otherwise.
     */
    public boolean isInitialized(){
        return initialized;
    }

    /**
     * Add the listener to the route
     * @param listener, the listener to add
     */
    public void addListener(RouteListener listener){
        listeners.add(listener);
    }

    /**
     * Remove all listeners from this route
     */
    public void removeListeners(){
        listeners.clear();
    }

    private void onInitialize(boolean success){
        Log.d(DEBUG_TAG, this.toString() + ", Has been initialized: " + success);
        initialized = success;
        for(RouteListener listener : listeners){
            listener.onInitialization(success);
        }
    }

    private void onLegFinished(Leg finishedLeg){
        for(RouteListener listener : listeners){
            listener.onLegFinished(finishedLeg);
        }
    }

    private void onStepFinished(Step finishedStep){
        for(RouteListener listener : listeners){
            listener.onStepFinished(finishedStep);
        }
    }

    private void onRouteFinished(){
        for(RouteListener listener : listeners){
            listener.onRouteFinished(this);
        }
    }
}