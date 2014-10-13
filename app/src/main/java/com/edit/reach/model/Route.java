package com.edit.reach.model;

import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import com.edit.reach.model.interfaces.IMilestone;
import com.edit.reach.model.interfaces.RouteListener;
import com.edit.reach.system.Remote;
import com.edit.reach.system.ResponseHandler;
import com.edit.reach.utils.NavigationUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.Integer;
import java.lang.String;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Joakim Berntsson on 2014-10-01.
 * Route class containing information for one route
 */
public class Route {
    private List<Leg> legs;
    private Circle endPointCircle, startPointCircle, pointer; // Should pointer be an individual class (following a route)?
    private LatLng origin, destination;
    private String originAddress, destinationAddress;
    private long distanceInKm, durationInSeconds;
    private boolean initialized;
    private List<RouteListener> listeners;
    private List<IMilestone> milestones, prelMilestones;
    private List<Pause> pauses;
    private String DEBUG_TAG = "Route";

    /** Handler for receiving a route as JSON Object */
    private ResponseHandler routeHandler = new ResponseHandler() {
        @Override
        public void onGetSuccess(JSONObject json) {
            try {
                JSONArray routeArray = json.getJSONArray("routes");
                JSONObject route = routeArray.getJSONObject(0);
                distanceInKm = 0;
                durationInSeconds = 0;
                JSONArray arrayLegs = route.getJSONArray("legs");
                for(int i = 0; i < arrayLegs.length(); i++) {
                    JSONObject legJSON = arrayLegs.getJSONObject(0);
                    Leg newLeg = new Leg(legJSON);
                    distanceInKm += newLeg.distance / 1000;
                    durationInSeconds += newLeg.duration;
                    legs.add(newLeg);
                }
                Log.d(DEBUG_TAG, "Has been initialized.");
                alertListeners(); // Notify the observers that the route has been initialized
                initialized = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onGetFail() {

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

                URL url = NavigationUtil.makeURL(destinationAddress);
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

                URL url = NavigationUtil.makeURL(origin, destination, new ArrayList<IMilestone>(), true);
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
        URL url = NavigationUtil.makeURL(origin, destination, new ArrayList<IMilestone>(), true);
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
        URL url = NavigationUtil.makeURL(origin);
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
        URL url = NavigationUtil.makeURL(destinationAddress);
        Remote.get(url, destinationHandler);
    }

    /**
     * Returns the approximated duration of the route.
     * @return number of seconds the route will take
     */
    public long getDuration(){
        return durationInSeconds;
    }

    /**
     * Returns the distance of the route.
     * @return the number of kilometres the route is
     */
    public long getDistance(){
        return distanceInKm;
    }

    /**
     * Adds a rest pause the specified number of seconds into the route
     * @param secondsIntoRoute, in seconds
     */
    public void addPause(long secondsIntoRoute) {
        long realSecondsIntoRoute = 0;
        long lastRealSecondsIntoRoute = 0;
        LatLng pauseLocation = null;
        LatLng lastPauseLocation = null;
        outerLoop:
        for(Leg leg : legs){
            for(Step step : leg.steps){
                realSecondsIntoRoute += step.duration;
                //Log.d(DEBUG_TAG, "Real: "+realSecondsIntoRoute);
                if(realSecondsIntoRoute >= secondsIntoRoute){
                    if(realSecondsIntoRoute - secondsIntoRoute > lastRealSecondsIntoRoute - secondsIntoRoute){
                        pauseLocation = step.startLocation;

                    }else{
                        pauseLocation = lastPauseLocation;
                    }
                    break outerLoop;
                }
                lastRealSecondsIntoRoute = realSecondsIntoRoute;
                lastPauseLocation = step.startLocation;
            }
        }

        if(pauseLocation != null){
            pauses.add(new Pause(pauseLocation));

            for(RouteListener l : listeners){
                // TODO Change onPauseAdded to give a pause instead of latlng
                l.onPauseAdded(pauseLocation);
            }
        }
        Log.d(DEBUG_TAG, "Pause added " + realSecondsIntoRoute + " seconds into the route. Because: " +secondsIntoRoute);
    }

    /**
     * Adds a fuel pause the specified number of kilometres into the route
     * @param kmIntoRoute, in km
     */
    public void addPause(double kmIntoRoute){
        double realKmIntoRoute = 0;
        LatLng pauseLocation = null;

        outerLoop:
        for(Leg leg : legs){
            for(Step step : leg.steps){
                realKmIntoRoute += ((double)step.distance)/1000;
                if(realKmIntoRoute >= kmIntoRoute){
                    pauseLocation = step.startLocation;
                    break outerLoop;
                }
            }
        }

        if(pauseLocation != null){
            pauses.add(new Pause(pauseLocation));

            for(RouteListener l : listeners){
                l.onPauseAdded(pauseLocation);
            }
        }
        Log.d(DEBUG_TAG, "Pause added " + realKmIntoRoute + " km into the route.");
    }

    public List<LatLng> getPauses(){
        List<LatLng> pausesLocations = new ArrayList<LatLng>();
        for(Pause p : pauses){
            pausesLocations.add(p.center);
        }
        return pausesLocations;
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
        Log.d(DEBUG_TAG, "Drawing pauses "+pauses.size());
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
        URL url = NavigationUtil.makeURL(this.origin, destination, new ArrayList<IMilestone>(), true);
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
     * Get the origin of the route
     * @return the latitude longitude of the route
     */
    public LatLng getOrigin(){
        return origin;
    }

    /**
     * Add a milestone to the route
     * @param milestone, the milestone to add
     */
    public void addMilestone(IMilestone milestone){
        milestones.add(milestone);
        // Recalculate the route
        initialized = false;
        URL url = NavigationUtil.makeURL(origin, destination, milestones, true);
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
        URL url = NavigationUtil.makeURL(origin, destination, milestones, true);
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
        URL url = NavigationUtil.makeURL(origin, destination, milestones, true);
        Remote.get(url, routeHandler);
    }

    /**
     * Remove all of the routes milestones
     */
    public void removeAllMilestones(){
        milestones.clear();
        // Recalculate the route
        initialized = false;
        URL url = NavigationUtil.makeURL(origin, destination, milestones, true);
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
                .center(legs.get(legs.size()-1).endLocation)
                .radius(10)
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE));

        this.pointer = map.addCircle(new CircleOptions().center(legs.get(0).startLocation).fillColor(Color.GREEN).radius(8));
        Log.d(DEBUG_TAG, "Drawing route in navigation mode.");
    }

    /**
     * Draw this route in an overview on the map provided
     * @param map, the map to draw on
     */
    public void drawOverview(GoogleMap map){
        this.erase();
        Log.d("Route", "Drawing overview!");
        drawPauses(map);
        for(Leg leg : legs){
            leg.draw(map);
        }
        // Add an end point
        this.endPointCircle = map.addCircle(new CircleOptions()
                .center(legs.get(legs.size()-1).endLocation)
                .radius(20)
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE));

        this.startPointCircle = map.addCircle(new CircleOptions()
                .center(legs.get(0).startLocation)
                .radius(20)
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
        if(pointer != null){
            pointer.remove();
        }
        if(startPointCircle != null){
            startPointCircle.remove();
        }
        Log.d(DEBUG_TAG, "Erased route.");
    }

    /**
     * Go to the provided location on the route.
     * @param map, the map to use
     * @param location, the location to move to
     * @return the estimated location on the route
     */
    public LatLng goTo(GoogleMap map, LatLng location){
        Log.d(DEBUG_TAG, "Move pointer to "+location.toString()+".");

        LatLng nearestLocation = legs.get(0).steps.get(0).subSteps.get(0);
        Step stepToRedraw = legs.get(0).steps.get(0);

        // TODO: Remake recursive!
        // TODO: This only works if the sub steps are in a relatively straight line
        outerLoop:
        for(Iterator<Leg> iteratorLeg = legs.iterator(); iteratorLeg.hasNext(); ){
            Leg leg = iteratorLeg.next();
            for(Iterator<Step> iteratorStep = leg.steps.iterator(); iteratorStep.hasNext(); ){
                Step step = iteratorStep.next();
                for(int i = 0; i < step.subSteps.size() - 1; i++){
                    LatLng subStep = step.subSteps.get(i);
                    LatLng subStepTwo = step.subSteps.get(i + 1);

                    double distance1 = NavigationUtil.getDistance(subStep, location);
                    double distance2 = NavigationUtil.getDistance(location, subStepTwo);

                    if(distance2 <= distance1){
                        nearestLocation = subStepTwo;
                        step.subSteps.remove(i);
                    }else{
                        stepToRedraw = step;
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
            if(stepToRedraw != null){
                stepToRedraw.draw(map);
            }
            if(pointer != null && !pointer.getCenter().equals(nearestLocation)){
                pointer.setCenter(nearestLocation);
                // Calculate new bearing and rotate the camera
                Step newStep = legs.get(0).steps.get(0);
                float bearing = (float) NavigationUtil.finalBearing(newStep.startLocation.latitude, newStep.startLocation.longitude,
		                newStep.endLocation.latitude, newStep.endLocation.longitude);
                CameraPosition lastPosition = map.getCameraPosition();
                CameraPosition currentPlace = new CameraPosition.Builder().target(nearestLocation).bearing(bearing)
                        .tilt(lastPosition.tilt).zoom(lastPosition.zoom).build();

                map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
            }

        }
        return nearestLocation;
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

    private void alertListeners(){
        for(RouteListener listener : listeners){
            listener.onInitialization();
        }
    }

    /**
     * Class that represents a leg
     */
    private class Leg{
        List<Step> steps;
        long distance;	// Metres
        long duration;	// Seconds
        LatLng startLocation, endLocation;

        Leg(JSONObject legJSON){
            Log.d(DEBUG_TAG, "Creating leg.");
            steps = new ArrayList<Step>();
            try {
                distance = legJSON.getJSONObject("distance").getLong("value");
                duration = legJSON.getJSONObject("duration").getLong("value");
                JSONObject startPosition = legJSON.getJSONObject("start_location");
                this.startLocation = new LatLng(startPosition.getDouble("lat"), startPosition.getDouble("lng"));
                JSONObject endPosition = legJSON.getJSONObject("end_location");
                this.endLocation = new LatLng(endPosition.getDouble("lat"), endPosition.getDouble("lng"));

                JSONArray stepsArray = legJSON.getJSONArray("steps");
                for(int i = 0; i < stepsArray.length(); i++){
                    Step step = new Step(stepsArray.getJSONObject(i));
                    steps.add(step);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /**
         * Draw this leg on the provided map
         * @param map, the map to draw on
         */
        void draw(GoogleMap map){
            for(Step step : steps){
                step.draw(map);
            }
        }

        /**
         * Erase the leg from all of its maps
         */
        void erase(){
            for(Step step : steps){
                step.erase();
            }
        }
    }

    /**
     * Class that represent every step of the directions. It stores distance, duration, location and instructions
     */
    private class Step{
        int distance, duration;
        LatLng startLocation, endLocation;
        String instructions;
        private Polyline polyline;
        List<LatLng> subSteps;

        Step(JSONObject stepJSON){
            Log.d(DEBUG_TAG, "Creating step.");
            try {
                distance = Integer.decode(stepJSON.getJSONObject("distance").getString("value"));
                JSONObject startLocation = stepJSON.getJSONObject("start_location");
                duration = Integer.decode(stepJSON.getJSONObject("duration").getString("value"));
                this.startLocation = new LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng"));
                JSONObject endLocation = stepJSON.getJSONObject("end_location");
                this.endLocation = new LatLng(endLocation.getDouble("lat"), endLocation.getDouble("lng"));
                try {
                    instructions = URLDecoder.decode(Html.fromHtml(stepJSON.getString("html_instructions")).toString(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                JSONObject polyline = stepJSON.getJSONObject("polyline");
                String encodedString = polyline.getString("points");
                subSteps = NavigationUtil.decodePoly(encodedString);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * Draw the step on the provided map
         * @param map, the map to draw on
         */
        void draw(GoogleMap map){
            this.erase();
            polyline = map.addPolyline(new PolylineOptions().addAll(subSteps).width(12).color(Color.parseColor("#4411EE")));
        }

        /**
         * Erase the step from all of its maps
         */
        void erase(){
            if(polyline != null){
                polyline.remove();
            }
        }
    }

    /**
     * Class that represents a pause.
     */
    private class Pause{
        private LatLng center;
        private Circle circle;

        Pause(LatLng center){
            this.center = center;
        }

        /**
         * Draw the pause as a circle
         * @param map, the map to draw it on
         */
        void draw(GoogleMap map){
            Log.d("Pause", "Drawing Pause at "+center.toString());
            this.erase();
            this.circle = map.addCircle(new CircleOptions().center(center).fillColor(Color.RED).radius(4000));
            //this.circle = map.addCircle(new CircleOptions().center(center).radius(100).fillColor(Color.RED).strokeColor(Color.RED).visible(true));
        }

        /**
         * Erase the pause from all of the maps it has been drawn on
         */
        void erase(){
            if(circle != null){
                circle.remove();
            }
        }
    }
}