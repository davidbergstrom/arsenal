package com.edit.reach.model;

import android.location.Location;
import android.os.Handler;
import android.util.Log;
import com.edit.reach.model.interfaces.IMilestone;
import com.edit.reach.model.interfaces.MilestonesReceiver;
import com.edit.reach.model.interfaces.RouteListener;
import com.edit.reach.system.GoogleMapsEndpoints;
import com.edit.reach.system.Remote;
import com.edit.reach.system.ResponseHandler;
import com.edit.reach.utils.NavigationUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joakim Berntsson on 2014-09-29.
 * Class containing all logic for handling map actions.
 */
public class Map {

    // Constant, the refresh rate of the navigation loop in milliseconds
    private final int UPDATE_INTERVAL = 300;

    // The map which modifies the map view in the activity
    private GoogleMap map;

    // The handler for navigation loop
    private Handler handler;

    private Route currentRoute;
    private Location lastLocation;

    // The state which is used internally in a state pattern
    private State state;

    // All the markers on the map
    private List<Marker> markersOnMap;
    //private List<IMilestone> milestonesOnMap;

    // Class name for logging
    private String DEBUG_TAG = "Map";

    /** Enum class for internal state pattern */
    public enum State{
        STATIONARY, MOVING
    }

    /** A listener for a route */
    private RouteListener routeListener = new RouteListener(){

        @Override
        public void onInitialization(boolean success) {
            // When the route has been initialized, draw it
            if(success){
                updateState();  // Update all the state specific changes when route initialized.
            }
        }

        @Override
        public void onPauseAdded(Pause pause) {
            Log.d("Map", "Pause added");
            if(state == State.STATIONARY){
                // If the current state is overview, draw the pause circle and add the markers to the map
                //map.addCircle(new CircleOptions().center(pauseLocation).fillColor(Color.RED).radius(1000));
                //currentRoute.drawPauses(map);
                pause.draw(map);
                Log.d("Map", "Getting milestones");
                //Ranking.getMilestones(milestonesReceiver, pause.getLocation(), NavigationUtil.RADIUS_IN_DEGREES*2);
            }

        }
    };

    /** A runnable for navigation */
    private Runnable navigationRunnable = new Runnable() {
        @Override
        public void run() {
            if(state == State.MOVING && currentRoute != null && currentRoute.isInitialized()){
                Location myLocation = map.getMyLocation();
                LatLng position = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

                // Move arrow to the current position on the route

                if(currentRoute != null && !myLocation.equals(lastLocation)) {
                    currentRoute.goTo(map, position);
                }
                handler.postDelayed(this, UPDATE_INTERVAL);
                lastLocation = myLocation;
            }else{
                Log.d(DEBUG_TAG, "Current route null or not initialized!");
            }
        }
    };

    /**
     * Construct a Map by providing a google map
     * @param map, the GoogleMap to use
     */
	Map(GoogleMap map){
		this.map = map;
        this.handler = new Handler();
        this.markersOnMap = new ArrayList<Marker>();
        //this.milestonesOnMap = new ArrayList<IMilestone>();
        this.state = State.STATIONARY;
	}

    /**
     * Set the current route to the provided route, this will also initiate an overview of the route.
     * @param newRoute, the new route
     */
    void setRoute(Route newRoute){
        Log.d(DEBUG_TAG, "Erasing old route and adding a new.");
        if(currentRoute != null){
            currentRoute.erase();
            currentRoute.removeListeners();
        }
        currentRoute = newRoute;
        currentRoute.addListener(routeListener);
        // Set the mode to Overview
        state = State.STATIONARY;
        //setState(State.STATIONARY);
    }

    /**
     * Get the current route.
     * @return the route the map is currently on
     */
    Route getRoute() {
        return currentRoute;
    }

    /**
     * Move the camera for the map to the new location.
     * @param location, the location to move to.
     */
    void moveCameraTo(LatLng location){
        CameraPosition newCameraLocation = new CameraPosition.Builder().target(location).zoom(8).build();
        map.moveCamera(CameraUpdateFactory.newCameraPosition(newCameraLocation));
    }

    /**
     * Move the camera for the map to the milestone and add it as a marker.
     * @param milestone, the milestone to add to the map and move the camera to.
     */
    Marker showMilestone(IMilestone milestone){
        LatLng milestoneLocation = milestone.getLocation();
        Marker tempMarker = map.addMarker(new MarkerOptions().position(milestoneLocation).snippet(milestone.getDescription()).title(milestone.getName()));
        moveCameraTo(milestoneLocation);
        return tempMarker;
    }

    /**
     * Sets the state of the map. Available states are:
     *      STATIONARY    -   Will zoom so the whole route is visible and with pauses and milestones added.
     *      NAVIGATION  -   Zoom to the ground and starts a automatic update of the route to follow the users current location.
     * @param newState, the new state of the map
     */
    public void setState(State newState){
        this.state = newState;
        if(newState == State.STATIONARY){
            LatLng routeOrigin = currentRoute.getOrigin();
            LatLng routeDestination = currentRoute.getDestination();
            if(routeOrigin != null && routeDestination != null){
                // Zoom and move camera so the whole route is visible.
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(routeDestination).include(routeOrigin);
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
            }

            if(currentRoute.isInitialized()){
                currentRoute.drawOverview(map);
            }

            List<Pause> pauses = currentRoute.getPauses();
            for (Pause p : pauses) {
                p.draw(map);
                //Ranking.getMilestones(milestonesReceiver, p.getLocation(), NavigationUtil.RADIUS_IN_DEGREES*2);
            }

            map.getUiSettings().setAllGesturesEnabled(true);

            Log.d("Map", "In overview mode.");
        }else if(newState == State.MOVING){
            // Remove markers when navigation is starting
            removeMarkers();

            // Set camera to right tilt and zoom
            Location myLocation = map.getMyLocation();
            LatLng position = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            CameraPosition currentPlace = new CameraPosition.Builder().target(position).tilt(65.5f).zoom(18).build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));

            if(currentRoute.isInitialized()){
                currentRoute.drawNavigation(map);
            }

            // Disable all interactions the user is not allowed to do.

            map.getUiSettings().setScrollGesturesEnabled(false);
            map.getUiSettings().setTiltGesturesEnabled(false);
            map.getUiSettings().setCompassEnabled(false);
            map.getUiSettings().setRotateGesturesEnabled(false);

            // Start navigation runnable
            handler.postDelayed(navigationRunnable, UPDATE_INTERVAL);
        }
    }

    private void updateState(){
        setState(state);
    }

    /**
     * Returns the Milestone at the specified coordinate.
     * @param location, the LatLng to find a milestone on
     * @return the milestone, null if there is no milestones at that coordinate
     */
    public IMilestone getMilestone(LatLng location){
        for(Pause pause : currentRoute.getPauses()){
            for(IMilestone milestone : pause.getMilestones()){
                if(milestone.getLocation().equals(location)){
                    return milestone;
                }
            }
        }
        return null;
    }

    /**
     * Make a request for suggestions of addresses based on the partOfAddress provided. The
     * results will be provided to the handler specified as a JSON object.
     * @param partOfAddress, a part of the address wanted
     * @param handler, the handler to handle the results
     */
    void requestAddressSuggestion(String partOfAddress, ResponseHandler handler){
        URL url = GoogleMapsEndpoints.makeURL(partOfAddress);
        Remote.get(url, handler);
    }

    // Removes all markers from the map
    private void removeMarkers(){
        for(Marker marker : markersOnMap){
            marker.remove();
        }
        //milestonesOnMap.clear();
        markersOnMap.clear();
    }
}
