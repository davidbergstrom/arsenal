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
    private List<IMilestone> milestonesOnMap;

    // Class name for logging
    private String logClass = "Map";

    /** Enum class for internal state pattern */
    private enum State{
        OVERVIEW, NAVIGATION, NONE
    }

    /** A listener for a route */
    private RouteListener routeListener = new RouteListener(){

        @Override
        public void onInitialization() {
            // When the route has been initialized, draw it
            updateState();  // Update all the state specific changes when route initialized.
            if(state == State.OVERVIEW){
                Log.d(logClass, "Drawing Route");
                currentRoute.drawOverview(map);
            }else if(state == State.NAVIGATION){
                Log.d(logClass, "Drawing Route");
                currentRoute.drawNavigation(map);
            }

        }

        @Override
        public void onPauseAdded(LatLng pauseLocation) {
            Log.d("Map", "Pause added");
            if(state == State.OVERVIEW){
                // If the current state is overview, draw the pause circle and add the markers to the map
                //map.addCircle(new CircleOptions().center(pauseLocation).fillColor(Color.RED).radius(1000));
                currentRoute.drawPauses(map);
                Log.d("Map", "Getting milestones");
                Ranking.getMilestones(milestonesReceiver, pauseLocation, 100);
            }

        }
    };

    /** A class receiving milestones and adding them as markers  */
    private MilestonesReceiver milestonesReceiver = new MilestonesReceiver() {
        @Override
        public void onMilestonesRecieved(ArrayList<IMilestone> milestones) {
            Log.d("Map", "Adding milestones");
            milestonesOnMap.addAll(milestones);
            for (IMilestone i : milestones) {
                markersOnMap.add(map.addMarker(new MarkerOptions()
                        .position(i.getLocation())
                        .title(i.getName())
                        .snippet(i.getDescription() + "\nRating: " + i.getRank() + "/5")));
            }
        }

        @Override
        public void onMilestonesGetFailed() {
            Log.d("Map", "Failed retireved!");
        }
    };

    /** A runnable for navigation */
    private Runnable navigationRunnable = new Runnable() {
        @Override
        public void run() {
            if(state == State.NAVIGATION && currentRoute != null && currentRoute.isInitialized()){
                Location myLocation = map.getMyLocation();
                LatLng position = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

                // Move arrow to the current position on the route

                if(currentRoute != null && !myLocation.equals(lastLocation)) {
                    currentRoute.goTo(map, position);
                }
                handler.postDelayed(this, UPDATE_INTERVAL);
                lastLocation = myLocation;
            }else{
                Log.d(logClass, "Current route null or not initialized!");
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
        this.milestonesOnMap = new ArrayList<IMilestone>();
        this.state = State.NONE;
	}

    /**
     * Set the current route to the provided route, this will also initiate an overview of the route.
     * @param newRoute, the new route
     */
    public void setRoute(Route newRoute){
        Log.d(logClass, "Erasing old route and adding a new.");
        if(currentRoute != null){
            currentRoute.erase();
            currentRoute.removeListeners();
        }
        currentRoute = newRoute;
        currentRoute.addListener(routeListener);
        // Set the mode to Overview
        state = State.OVERVIEW;
        //setState(State.OVERVIEW);
    }

    /**
     * Get the current route.
     * @return the route the map is currently on
     */
    Route getRoute() {
        return currentRoute;
    }

    /**
     * Start the current route.
     */
	void startNavigation(){
        setState(State.NAVIGATION);
	}

    /**
     * Stop the current route.
     */
	void stopNavigation(){
        setState(State.NONE);
	}

    /**
     * Start an overview of the current route.
     */
    void startOverview(){
        setState(State.OVERVIEW);
    }

    // Sets the state of the map to the provided. This class will behave differently
    // based on what state it is in.
    private void setState(State newState){
        this.state = newState;
        if(newState == State.OVERVIEW){
            LatLng routeOrigin = currentRoute.getOrigin();
            LatLng routeDestination = currentRoute.getDestination();
            if(routeOrigin != null && routeDestination != null){
                LatLng routeMiddle = new LatLng((routeOrigin.latitude+routeDestination.latitude)/2, (routeOrigin.longitude+routeDestination.longitude)/2);
                CameraPosition currentPlace = new CameraPosition.Builder().target(routeMiddle).zoom(6).build();
                map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
            }

            if(currentRoute.isInitialized()){
                currentRoute.drawOverview(map);
            }

            List<LatLng> pauses = currentRoute.getPauses();
            for (LatLng i : pauses) {
                Ranking.getMilestones(milestonesReceiver, i, NavigationUtil.RADIUS_IN_DEGREES);
            }

            map.getUiSettings().setAllGesturesEnabled(true);

            Log.d("Map", "End of overview.");
        }else if(newState == State.NAVIGATION){
            // Remove markers when navigation is starting
            removeMarkers();

            // Set camera to right tilt and zoom
            Location myLocation = map.getMyLocation();
            LatLng position = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            CameraPosition currentPlace = new CameraPosition.Builder().target(position).tilt(65.5f).zoom(18).build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));

            if(currentRoute.isInitialized()){
                Log.d(logClass, "Route is initialized");
                currentRoute.drawNavigation(map);
            }

            // Disable all interactions the user is not allowed to do.

            map.getUiSettings().setScrollGesturesEnabled(false);
            map.getUiSettings().setTiltGesturesEnabled(false);
            map.getUiSettings().setCompassEnabled(false);
            map.getUiSettings().setRotateGesturesEnabled(false);


            // Start navigation runnable
            handler.postDelayed(navigationRunnable, UPDATE_INTERVAL);
        }else{
            // Remove markers when navigation is starting
            removeMarkers();

            // TODO Add what to show (maybe new draw?)
            currentRoute.erase();

            Location myLocation = map.getMyLocation();
            if(myLocation != null){
                LatLng position = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

                CameraPosition currentPlace = new CameraPosition.Builder().target(position).tilt(0).zoom(5).build();
                map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
            }
            map.getUiSettings().setAllGesturesEnabled(true);
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
    IMilestone getMilestone(LatLng location){
        for(IMilestone milestone : milestonesOnMap){
            if(milestone.getLocation().equals(location)){
                return milestone;
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
        milestonesOnMap.clear();
        markersOnMap.clear();
    }
}
