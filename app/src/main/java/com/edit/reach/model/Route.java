package com.edit.reach.model;

import android.graphics.Color;
import android.util.Log;
import com.edit.reach.app.R;
import com.edit.reach.model.interfaces.IMilestone;
import com.edit.reach.model.interfaces.MilestonesReceiver;
import com.edit.reach.model.interfaces.RouteListener;
import com.edit.reach.system.*;
import com.edit.reach.system.ResponseHandler;
import com.edit.reach.utils.NavigationUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.String;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Joakim Berntsson on 2014-10-01.
 * Route class containing information for one route
 */
public class Route {
    private List<Leg> legs;
    private Circle endPointCircle, startPointCircle;
    private GroundOverlay pointerWithBearing;
    private LatLng origin, destination;
    private String originAddress, destinationAddress;
    private boolean initialized;
    private List<RouteListener> listeners;
    private List<IMilestone> milestones;
    private List<Pause> pauses;
    private String DEBUG_TAG = "Route";

    /** Handler for receiving a route as JSON Object */
    private ResponseHandler routeHandler = new ResponseHandler() {
        @Override
        public void onGetSuccess(JSONObject json) {
            try {
                JSONArray routeArray = json.getJSONArray("routes");
                JSONObject route = routeArray.getJSONObject(0);
                JSONArray arrayLegs = route.getJSONArray("legs");
                for(int i = 0; i < arrayLegs.length(); i++) {
                    JSONObject legJSON = arrayLegs.getJSONObject(0);
                    Leg newLeg = new Leg(legJSON);
                    newLeg.setMilestone(getMilestone(newLeg.getEndLocation())); // Set the milestone to a milestone with the endlocation of the leg
                    legs.add(newLeg);
                }

                Log.d(DEBUG_TAG, "Has been initialized.");
                onInitialize(legs.size() > 0); // Notify the observers that the route has been initialized

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

                URL url = GoogleMapsEndpoints.makeURL(destinationAddress);
                Remote.get(url, destinationHandler);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onGetFail() {

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
        URL url = GoogleMapsEndpoints.makeURL(origin);
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
        URL url = GoogleMapsEndpoints.makeURL(destinationAddress);
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
    public void addPause(long secondsIntoRoute) {
        Log.d(DEBUG_TAG, "Adding pause...");
        long actualSecondsIntoRoute = 0;

        outerLoop:
        for(Leg leg : legs){
            for(Step step : leg.getSteps()){
                List<LatLng> subSteps = step.getSubSteps();
                double subDuration = ((double)step.getDuration()) / (subSteps.size()-1);
                actualSecondsIntoRoute += step.getDuration();
                if(actualSecondsIntoRoute >= secondsIntoRoute){
                    int subIndexTooFar = (int)((actualSecondsIntoRoute - secondsIntoRoute) / subDuration);
                    int subIndex = subSteps.size() - 1 - subIndexTooFar;

                    final LatLng pauseLocation = subSteps.get(subIndex);
                    final double addedTime = actualSecondsIntoRoute - subIndexTooFar * subDuration;

                    // Start a request for milestones at the pause location. Then create the pause with the location and milestones.
                    Ranking.getMilestones(new MilestonesReceiver() {
                        @Override
                        public void onMilestonesRecieved(ArrayList<IMilestone> milestones) {
                            Pause p = new Pause(pauseLocation, milestones);
                            pauses.add(p);

                            for(RouteListener l : listeners){
                                l.onPauseAdded(p);
                            }
                            Log.d(DEBUG_TAG, "Pause added " + addedTime + " seconds into the route.");
                        }

                        @Override
                        public void onMilestonesGetFailed() {

                        }
                    }, pauseLocation, NavigationUtil.RADIUS_IN_DEGREES*2);

                    break outerLoop;
                }
            }
        }
    }

    /**
     * Adds a fuel pause the specified number of kilometres into the route
     * @param kmIntoRoute, in km
     */
    public void addPause(double kmIntoRoute){
        Log.d(DEBUG_TAG, "Adding pause...");
        double metresIntoRoute = kmIntoRoute * 1000;
        double actualMetresIntoRoute = 0;

        outerLoop:
        for(Leg leg : legs){
            for(Step step : leg.getSteps()){
                List<LatLng> subSteps = step.getSubSteps();
                double subDistance = ((double)step.getDistance()) / (subSteps.size()-1);
                actualMetresIntoRoute += step.getDistance();
                if(actualMetresIntoRoute >= metresIntoRoute){
                    int subIndexTooFar = (int)((actualMetresIntoRoute - metresIntoRoute) / subDistance);
                    int subIndex = subSteps.size() - 1 - subIndexTooFar;

                    final LatLng pauseLocation = subSteps.get(subIndex);
                    final double addedDistance = actualMetresIntoRoute - subIndexTooFar * subDistance;

                    // Start a request for milestones at the pause location. Then create the pause with the location and milestones.
                    Ranking.getMilestones(new MilestonesReceiver() {
                        @Override
                        public void onMilestonesRecieved(ArrayList<IMilestone> milestones) {
                            Pause p = new Pause(pauseLocation, milestones);
                            pauses.add(p);

                            for(RouteListener l : listeners){
                                l.onPauseAdded(p);
                            }
                            Log.d(DEBUG_TAG, "Pause added " + addedDistance + " km into the route.");
                        }

                        @Override
                        public void onMilestonesGetFailed() {

                        }
                    }, pauseLocation, NavigationUtil.RADIUS_IN_DEGREES*2);

                    break outerLoop;
                }
            }
        }
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
     * Get the origin of the route
     * @return the latitude longitude of the route
     */
    public LatLng getOrigin(){
        return origin;
    }

    /**
     * Returns the origin address
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
        this.milestones.addAll(milestones);
        // Recalculate the route
        initialized = false;
        URL url = GoogleMapsEndpoints.makeURL(origin, destination, milestones, true);
        Remote.get(url, routeHandler);
    }

    /**
     * Remove the milestone from the route
     * @param milestone, the milestone to remove
     */
    public void removeMilestone(IMilestone milestone){
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
        // Add an end point
        this.endPointCircle = map.addCircle(new CircleOptions()
                .center(legs.get(legs.size()-1).getEndLocation())
                .radius(100)
                .fillColor(Color.BLUE));

        //this.pointer = map.addCircle(new CircleOptions().center(legs.get(0).startLocation).fillColor(Color.GREEN).radius(8));
        pointerWithBearing = map.addGroundOverlay(new GroundOverlayOptions()
                .position(legs.get(0).getStartLocation(), (float) 100)
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
                .radius(500)
                .strokeWidth(0)
                .fillColor(Color.BLUE));

        this.startPointCircle = map.addCircle(new CircleOptions()
                .center(legs.get(0).getStartLocation())
                .radius(500)
                .strokeColor(Color.BLUE)
                .fillColor(Color.YELLOW));

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
        }
        if(startPointCircle != null){
            startPointCircle.remove();
        }
        Log.d(DEBUG_TAG, "Erased route.");
    }

    /**
     * Get the legs of this route. A leg is a part of the route.
     * @return the legs
     */
    public List getLegs(){
        return legs;
    }

    /**
     * Go to the provided location on the route.
     * @param map, the map to use
     * @param location, the location to move to
     */
    public void goTo(GoogleMap map, LatLng location){
        /*LatLng firstLocation = legs.get(0).getStartLocation();

        for(int i = 0; i < legs.size(); i++){
            Leg leg = legs.get(i);
            List<Step> steps = leg.getSteps();

            for(int j = 0; j < steps.size(); j++){
                Step step = steps.get(j);
                List<LatLng> subSteps = step.getSubSteps();

                double distanceToStepStart = NavigationUtil.getDistance(location, step.getStartLocation());
                double distanceToStepEnd = NavigationUtil.getDistance(location, step.getEndLocation());

                double subDistance = step.getDistance() / (subSteps.size()-1);
            }
        }*/

        Log.d(DEBUG_TAG, "Move pointer to "+location.toString()+".");

        LatLng nearestLocation = legs.get(0).getSteps().get(0).getSubSteps().get(0);

        // TODO: Clean up.
        // TODO: This only works if the sub steps are in a relatively straight line.
        outerLoop:
        for(Iterator<Leg> iteratorLeg = legs.iterator(); iteratorLeg.hasNext(); ){
            Leg leg = iteratorLeg.next();
            for(Iterator<Step> iteratorStep = leg.getSteps().iterator(); iteratorStep.hasNext(); ){
                Step step = iteratorStep.next();
                List<LatLng> subSteps = step.getSubSteps();
                double distanceToStepEnd = NavigationUtil.getDistance(location, step.getEndLocation());

                for(int i = 0; i < subSteps.size() - 1; i++){
                    LatLng subStep = subSteps.get(i);
                    LatLng subStepTwo = subSteps.get(i + 1);

                    double distance1 = NavigationUtil.getDistance(subStep, location);
                    double distance2 = NavigationUtil.getDistance(location, subStepTwo);

                    if(distance2 <= distance1){
                        nearestLocation = subStepTwo;
                        subSteps.remove(i);

                    }else if(distance2 <= distanceToStepEnd){   // If the new point is also closer to current location then the end of step
                        step.draw(map);
                        break outerLoop; // Break the outer loop
                    }
                }
                step.erase();
                iteratorStep.remove();
            }
            leg.erase();
            iteratorLeg.remove();
        }

        if(legs.size() == 0){
            // Route finished!
            endPointCircle.remove();

        }else{
            if(NavigationUtil.getDistance(location, nearestLocation) > 0.1){
                // TODO Reinitialize the route with new start location.
            }
            if(pointerWithBearing != null && !pointerWithBearing.getPosition().equals(nearestLocation)){
                // Calculate new bearing and rotate the camera
                Step newStep = legs.get(0).getSteps().get(0);
                float bearing = NavigationUtil.finalBearing(newStep.getStartLocation(), newStep.getEndLocation());
                pointerWithBearing.setBearing(bearing);
                pointerWithBearing.setPosition(nearestLocation);

                CameraPosition lastPosition = map.getCameraPosition();
                CameraPosition currentPlace = new CameraPosition.Builder().target(nearestLocation).bearing(bearing)
                        .tilt(lastPosition.tilt).zoom(lastPosition.zoom).build();
                map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
            }

        }
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
        Log.d(DEBUG_TAG, "Has been initialized: "+success);
        initialized = success;
        for(RouteListener listener : listeners){
            listener.onInitialization(success);
        }
    }
}