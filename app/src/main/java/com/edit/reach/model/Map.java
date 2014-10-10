package com.edit.reach.model;

import android.location.Location;
import android.os.Handler;
import android.util.Log;
import com.edit.reach.app.RankingSystem;
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
public class Map implements MilestonesReceiver {
    private GoogleMap map;
    private Handler handler = new Handler();
    private Route currentRoute;
    private Location lastLocation;
    private static int UPDATE_INTERVAL = 300;
    private String logClass = "Map";
    private State state;
    private enum State{
        OVERVIEW, NAVIGATION, NONE
    }

    /** A listener for a route */
    private RouteListener routeListener = new RouteListener(){

        @Override
        public void onInitialization() {
            Log.d(logClass, "Drawing Route");
            startOverview();
        }

        @Override
        public void onPauseAdded(LatLng pauseLocation) {
            currentRoute.drawPauses(map);
            // Ranking.getMilestones
            // map.paintMlestones;
            //
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
	public Map(GoogleMap map){
		this.map = map;
        state = State.NONE;
	}

    /**
     * Set the current route to the provided route, this will also initiate an overview of the route.
     * @param newRoute, the new route
     */
    public void setRoute(Route newRoute){
        if(currentRoute != null){
            currentRoute.remove();
        }
        currentRoute = newRoute;
        if(currentRoute.isInitialized()){
            Log.d(logClass, "Route is initialized");
            currentRoute.draw(map);
            currentRoute.drawOverview(map);
        }else{
            Log.d(logClass, "Route is NOT initialized");
            currentRoute.addListener(routeListener);
        }
        setState(State.OVERVIEW);
    }

    /**
     * Get the current route.
     * @return the route the map is currently on
     */
    public Route getRoute() {
        return currentRoute;
    }

    /**
     * Start the current route.
     */
	public void startNavigation(){
        setState(State.NAVIGATION);
	}

    /**
     * Stop the current route.
     */
	public void stopNavigation(){
        setState(State.NONE);
	}

    private void setState(State newState){
        this.state = newState;
        if(newState == State.OVERVIEW){
            LatLng routeOrigin = currentRoute.getOrigin();
            LatLng routeDestination = currentRoute.getDestination();
            LatLng routeMiddle = new LatLng((routeOrigin.latitude+routeDestination.latitude)/2, (routeOrigin.longitude+routeDestination.longitude)/2);

            CameraPosition currentPlace = new CameraPosition.Builder().target(routeMiddle).zoom(10).build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));

            RankingSystem rankingSystem = new RankingSystem(this);

            List<LatLng> pauses = currentRoute.getPauses();

            for (LatLng i : pauses) {
                rankingSystem.getMilestones(i, NavigationUtils.RADIUS_IN_DEGREES);
            }

            map.getUiSettings().setAllGesturesEnabled(true);

            currentRoute.drawOverview(map);
        }else if(newState == State.NAVIGATION){
            Location myLocation = map.getMyLocation();
            LatLng position = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            CameraPosition currentPlace = new CameraPosition.Builder().target(position).tilt(65.5f).zoom(18).build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));

            // Disable all interactions the user is not allowed to do.
            map.getUiSettings().setScrollGesturesEnabled(false);
            map.getUiSettings().setTiltGesturesEnabled(false);
            map.getUiSettings().setCompassEnabled(false);
            map.getUiSettings().setRotateGesturesEnabled(false);

            handler.postDelayed(navigationRunnable, UPDATE_INTERVAL);
        }else{
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
     * Start an overview of the current route.
     */
    public void startOverview(){
        setState(State.OVERVIEW);
    }

    public List getAddressFromSearch(String input){
        // TODO Fix or delete!
        List<String> addresList = null;
        URL url = NavigationUtils.makeURL(input);
        //Remote.get(url, routeHandler);

        return addresList;
    }

    @Override
    public void onMilestonesRecieved(ArrayList<IMilestone> milestones) {
        for (IMilestone i : milestones) {
            map.addMarker(new MarkerOptions()
                    .position(i.getLocation())
                    .title(i.getName())
                    .snippet(i.getDescription() + "\nRating: " + i.getRank() + "/5"));
        }
    }

    @Override
    public void onMilestonesGetFailed() {

    }
}
