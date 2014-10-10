package com.edit.reach.model;

import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import com.edit.reach.app.Remote;
import com.edit.reach.app.ResponseHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.Integer;import java.lang.Math;import java.lang.String;
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
    private boolean initialized = false;
    private List<RouteListener> listeners;
    private List<IMilestone> milestones, prelMilestones;
    private List<Pause> pauses;
    private ResponseHandler routeHandler = new ResponseHandler() {
        @Override
        public void onGetSuccess(JSONObject json) {
            try {
                JSONArray routeArray = json.getJSONArray("routes");
                JSONObject route = routeArray.getJSONObject(0);
                distanceInKm = route.getLong("distance") / 1000;
                durationInSeconds = route.getLong("duration");
                JSONArray arrayLegs = route.getJSONArray("legs");
                for(int i = 0; i < arrayLegs.length(); i++) {
                    JSONObject legJSON = arrayLegs.getJSONObject(0);
                    legs.add(new Leg(legJSON));
                }
                alertListeners(); // Notify the observers that the route has been initialized
                Log.d("Route", "Notified Observers");
                initialized = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onGetFail() {

        }
    };

    private ResponseHandler originHandler = new ResponseHandler() {
        @Override
        public void onGetSuccess(JSONObject json) {
            try {
                JSONArray originArray = json.getJSONArray("results");
                JSONObject address = originArray.getJSONObject(0);
                JSONObject location = address.getJSONObject("location");
                origin = (new LatLng(location.getDouble("lat"), location.getDouble("lng")));

                URL url = NavigationUtils.makeURL(destinationAddress);
                Remote.get(url, destinationHandler);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onGetFail() {

        }
    };

    private ResponseHandler destinationHandler = new ResponseHandler() {
        @Override
        public void onGetSuccess(JSONObject json) {
            try {
                JSONArray destinationArray = json.getJSONArray("results");
                JSONObject address = destinationArray.getJSONObject(0);
                JSONObject location = address.getJSONObject("location");
                destination = (new LatLng(location.getDouble("lat"), location.getDouble("lng")));

                URL url = NavigationUtils.makeURL(origin, destination, null, true);
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
        URL url = NavigationUtils.makeURL(origin, destination, null, true);
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
        URL url = NavigationUtils.makeURL(origin);
        Remote.get(url, originHandler);
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
    public void addPause(int secondsIntoRoute) {
        int realSecondsIntoRoute = 0;
        LatLng pauseLocation = null;

        outerLoop:
        for(Leg leg : legs){
            for(Step step : leg.steps){
                realSecondsIntoRoute += step.duration;
                if(realSecondsIntoRoute >= secondsIntoRoute){
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
        this.remove();
        this.destination = destination;
        initialized = false;
        URL url = NavigationUtils.makeURL(this.origin, destination, null, true);
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
    }

    /**
     * Add all the milestones to the route
     * @param milestones, the milestones to add
     */
    public void addMilestones(List<IMilestone> milestones){
        this.milestones.addAll(milestones);
        // Recalculate the route
    }

    /**
     * Remove the milestone from the route
     * @param milestone, the milestone to remove
     */
    public void removeMilestone(IMilestone milestone){
        milestones.remove(milestone);
        // Recalculate the route
    }

    /**
     * Remove all of the routes milestones
     */
    public void removeAllMilestones(){
        milestones.clear();
    }

    /**
     * Returns the Milestone at the specified coordinate.
     * @param latLng, the location to find a milestone on
     * @return the milestone, null if there is no milestones at that coordinate
     */
    public IMilestone getMilestone(LatLng latLng){
        for(IMilestone milestone : milestones){
            if(milestone.getLocation().equals(latLng)){
                return milestone;
            }
        }
        return null;
    }

    /**
     * Draw this route on the map provided
     * @param map, the map to draw the route on
     */
    public void draw(GoogleMap map){
        for(Leg leg : legs){
            leg.draw(map);
        }
        // Add an end point
        this.endPointCircle = map.addCircle(new CircleOptions()
                .center(legs.get(legs.size()-1).endLocation)
                .radius(10)
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE));
    }

    public void drawOverview(GoogleMap map){
        for(Leg leg : legs){
            leg.draw(map);
        }
        // Add an end point
        this.endPointCircle = map.addCircle(new CircleOptions()
                .center(legs.get(legs.size()-1).endLocation)
                .radius(10)
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE));

        this.startPointCircle = map.addCircle(new CircleOptions()
                .center(legs.get(0).startLocation)
                .radius(10)
                .strokeColor(Color.BLUE)
                .fillColor(Color.YELLOW));

    }

    /**
     * Remove this route from all of its maps
     */
    public void remove(){
        for(Leg leg : legs){
            leg.erase();
        }
        if(endPointCircle != null){
            endPointCircle.remove();
        }
        if(pointer != null){
            pointer.remove();
        }
    }

    /**
     * Go to the provided location on the route.
     * @param map, the map to use
     * @param location, the location to move to
     * @return the estimated location on the route
     */
    public LatLng goTo(GoogleMap map, LatLng location){
        LatLng nearestLocation = legs.get(0).steps.get(0).subSteps.get(0);
        Step stepToRedraw = legs.get(0).steps.get(0);

        // TODO: Remake recursive!
        // TODO: This only works if the substeps are in a relatively straight line
        outerLoop:
        for(Iterator<Leg> iteratorLeg = legs.iterator(); iteratorLeg.hasNext(); ){
            Leg leg = iteratorLeg.next();
            for(Iterator<Step> iteratorStep = leg.steps.iterator(); iteratorStep.hasNext(); ){
                Step step = iteratorStep.next();
                for(int i = 0; i < step.subSteps.size() - 1; i++){
                    LatLng subStep = step.subSteps.get(i);
                    LatLng subStepTwo = step.subSteps.get(i + 1);

                    double distance1 = NavigationUtils.getDistance(subStep, location);
                    double distance2 = NavigationUtils.getDistance(location, subStepTwo);

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
                stepToRedraw.redraw(map);
            }
            if(pointer != null && !pointer.getCenter().equals(nearestLocation)){
                pointer.remove();
                pointer = map.addCircle(new CircleOptions().center(nearestLocation).fillColor(Color.GREEN).radius(8));
                // Calculate new bearing and rotate the camera
                Step newStep = legs.get(0).steps.get(0);
                float bearing = (float) finalBearing(newStep.startLocation.latitude, newStep.startLocation.longitude,
                        newStep.endLocation.latitude, newStep.endLocation.longitude);
                CameraPosition lastPosition = map.getCameraPosition();
                CameraPosition currentPlace = new CameraPosition.Builder().target(nearestLocation).bearing(bearing)
                        .tilt(lastPosition.tilt).zoom(lastPosition.zoom).build();

                map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
            }else if(pointer == null){
                pointer = map.addCircle(new CircleOptions().center(nearestLocation).fillColor(Color.GREEN).radius(8));
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

    private static double finalBearing(double lat1, double long1, double lat2, double long2){
        double degToRad = Math.PI / 180.0;
        double phi1 = lat1 * degToRad;
        double phi2 = lat2 * degToRad;
        double lam1 = long1 * degToRad;
        double lam2 = long2 * degToRad;

        double bearing = Math.atan2(Math.sin(lam2-lam1)*Math.cos(phi2),
                Math.cos(phi1)*Math.sin(phi2) - Math.sin(phi1)*Math.cos(phi2)*Math.cos(lam2-lam1)) * 180/Math.PI;

        return (bearing + 180.0) % 360;
    }

    /**
     * Class that represents a leg
     */
    private class Leg{
        public List<Step> steps;
        public int distance;	// Metres
        public int duration;	// Seconds
        public LatLng startLocation;
        public LatLng endLocation;

        Leg(JSONObject legJSON){
            steps = new ArrayList<Step>();
            JSONObject startPosition;
            JSONObject endPosition;
            try {
                distance = Integer.decode(legJSON.getJSONObject("distance").getString("value"));
                duration = Integer.decode(legJSON.getJSONObject("duration").getString("value"));
                startPosition = legJSON.getJSONObject("start_location");
                this.startLocation = new LatLng(startPosition.getDouble("lat"), startPosition.getDouble("lng"));
                endPosition = legJSON.getJSONObject("end_location");
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
        public void draw(GoogleMap map){
            for(Step step : steps){
                step.draw(map);
            }
        }

        /**
         * Erase the leg from all of its maps
         */
        public void erase(){
            for(Step step : steps){
                step.erase();
            }
        }
    }

    /**
     * Class that represent every step of the directions. It store distance, location and instructions
     */
    private class Step{
        public int distance;
        private int duration;
        public LatLng startLocation;
        public LatLng endLocation;
        public String instructions;
        private Polyline polyline;
        public List<LatLng> subSteps;

        Step(JSONObject stepJSON){
            JSONObject startLocation;
            JSONObject endLocation;
            try {
                distance = Integer.decode(stepJSON.getJSONObject("distance").getString("value"));
                startLocation = stepJSON.getJSONObject("start_location");
                duration = Integer.decode(stepJSON.getJSONObject("duration").getString("value"));
                this.startLocation = new LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng"));
                endLocation = stepJSON.getJSONObject("end_location");
                this.endLocation = new LatLng(endLocation.getDouble("lat"), endLocation.getDouble("lng"));
                try {
                    instructions = URLDecoder.decode(Html.fromHtml(stepJSON.getString("html_instructions")).toString(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                JSONObject polyline = stepJSON.getJSONObject("polyline");
                String encodedString = polyline.getString("points");
                subSteps = NavigationUtils.decodePoly(encodedString);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * Draw the step on the provided map
         * @param map, the map to draw on
         */
        public void draw(GoogleMap map){
            //map.addMarker(new MarkerOptions().position(startLocation).title(distance+"m").snippet(instructions));
            polyline = map.addPolyline(new PolylineOptions().addAll(subSteps).width(12).color(Color.parseColor("#4411EE")));
        }

        /**
         * Redraw the step on the provided map
         * @param map
         */
        public void redraw(GoogleMap map){
            this.erase();
            polyline = map.addPolyline(new PolylineOptions().addAll(subSteps).width(12).color(Color.parseColor("#4411EE")));
        }

        /**
         * Erase the step from all of its maps
         */
        public void erase(){
            polyline.remove();
        }
    }

    /**
     * Class that represents a pause.
     */
    private class Pause{
        private LatLng center;
        private Circle circle;

        public Pause(LatLng center){
            this.center = center;
        }

        /**
         * Draw the pause as a circle
         * @param map, the map to draw it on
         */
        public void draw(GoogleMap map){
            this.circle = map.addCircle(new CircleOptions().center(center).radius(NavigationUtils.RADIUS_IN_KM*1000).fillColor(Color.BLUE));
        }

        /**
         * Erase the pause from all of the maps it has been drawn on
         */
        public void erase(){
            circle.remove();
        }
    }
}
