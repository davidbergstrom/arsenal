package com.edit.reach.model;

import android.location.Location;
import android.os.Handler;
import android.util.Log;
import com.edit.reach.app.RankingSystem;
import com.edit.reach.app.Remote;
import com.edit.reach.app.ResponseHandler;
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
class Map {

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
            Log.d(logClass, "Drawing Route");
            // If the route has been initialized then draw it
            if(state == State.OVERVIEW){
                currentRoute.drawOverview(map);
            }else if(state == State.NAVIGATION){
                currentRoute.drawNavigation(map);
            }

        }

        @Override
        public void onPauseAdded(LatLng pauseLocation) {
            if(state == State.OVERVIEW){
                // If the current state is overview, draw the pause circle and add the markers to the map
                currentRoute.drawPauses(map);

                RankingSystem rankingSystem = new RankingSystem(milestonesReceiver);
                rankingSystem.getMilestones(pauseLocation, NavigationUtils.RADIUS_IN_DEGREES);
            }

        }
    };

    /** A class receiving milestones and adding them as markers  */
    private MilestonesReceiver milestonesReceiver = new MilestonesReceiver() {
        @Override
        public void onMilestonesRecieved(ArrayList<IMilestone> milestones) {
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

                    // float bearing = currentRoute.getBearing();

                    // Move the camera to the current position
                    //moveCameraTo(pointerLocation);

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
        handler = new Handler();
        markersOnMap = new ArrayList<Marker>();
        milestonesOnMap = new ArrayList<IMilestone>();
        state = State.NONE;
	}

    /**
     * Set the current route to the provided route, this will also initiate an overview of the route.
     * @param newRoute, the new route
     */
    void setRoute(Route newRoute){
        if(currentRoute != null){
            currentRoute.erase();
        }
        currentRoute = newRoute;
        currentRoute.addListener(routeListener);
        setState(State.OVERVIEW);
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

    private void setState(State newState){
        this.state = newState;
        if(newState == State.OVERVIEW){
            LatLng routeOrigin = currentRoute.getOrigin();
            LatLng routeDestination = currentRoute.getDestination();
            LatLng routeMiddle = new LatLng((routeOrigin.latitude+routeDestination.latitude)/2, (routeOrigin.longitude+routeDestination.longitude)/2);
            CameraPosition currentPlace = new CameraPosition.Builder().target(routeMiddle).zoom(10).build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));

            if(currentRoute.isInitialized()){
                Log.d(logClass, "Route is initialized");
                currentRoute.drawOverview(map);
            }

            RankingSystem rankingSystem = new RankingSystem(milestonesReceiver);
            List<LatLng> pauses = currentRoute.getPauses();
            for (LatLng i : pauses) {
                rankingSystem.getMilestones(i, NavigationUtils.RADIUS_IN_DEGREES);
            }

            map.getUiSettings().setAllGesturesEnabled(true);
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
        URL url = NavigationUtils.makeURL(partOfAddress);
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
