package com.edit.reach.model;

import android.location.Location;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;

import java.net.URL;
import java.util.List;

/**
 * Created by Joakim Berntsson on 2014-09-29.
 * Class containing all logic for handling map actions.
 */
public class Map {
    private GoogleMap map;
    private Handler handler = new Handler();
    private Route currentRoute;
    private Location lastLocation;
    private static int UPDATE_INTERVAL = 300;
    private String logClass = "Map";

    /** A listener for a route */
    private RouteListener routeListener = new RouteListener(){

        @Override
        public void onInitialization() {
            Log.d(logClass, "Drawing Route");
            currentRoute.draw(map);
        }

        public void onPauseSelect(LatLng pauseLt){
            // Ranking.getMilestones
            // map.paintMlestones;
            //

        }
    };

    /** A runnable for navigation */
    private Runnable navigationRunnable = new Runnable() {
        @Override
        public void run() {
            if(currentRoute != null && currentRoute.isInitialized()){
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
	}

    /**
     * Set the current route to the provided route
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
        }else{
            Log.d(logClass, "Route is NOT initialized");
            currentRoute.addListener(routeListener);
        }
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
	public void startRoute(){
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
	}

    /**
     * Stop the current route.
     */
	public void stopRoute(){
        Location myLocation = map.getMyLocation();
        if(myLocation != null){
            LatLng position = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            CameraPosition currentPlace = new CameraPosition.Builder().target(position).tilt(0).zoom(5).build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
        }

        handler.removeCallbacks(navigationRunnable);
        if(currentRoute != null){
            currentRoute.remove();
            currentRoute = null;
        }
        map.getUiSettings().setAllGesturesEnabled(true);
	}

    /**
     * Start an overview of the current route.
     */
    public void startOverview(){
        LatLng routeOrigin = currentRoute.getOrigin();
        LatLng routeDestination = currentRoute.getDestination();
        LatLng routeMiddle = new LatLng((routeOrigin.latitude+routeDestination.latitude)/2, (routeOrigin.longitude+routeDestination.longitude)/2);

        CameraPosition currentPlace = new CameraPosition.Builder().target(routeMiddle).zoom(10).build();
        map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));

        // TODO Get pauses from route, ask ranking which milestones are in those places.
        // TODO Loop through the milestone and add them as markers to the map.

        // TODO start the route overview (painting the overview inside Route)
        // List<LatLng> pauses = currentRoute.getPauses();
        // List<IMilestone> pelM = Ranking.getMilestones(pauses.get(0));

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return false;
            }
        });
        // TODO Show the add milestones view
    }

    /**
     * Stop the current overview
     */
    public void stopOverview(){
        // TODO Should be 3 views: Overview, Navigation, Standard.
    }

    /**
     * Get all milestones a certain amount of time into the current route.
     * @param timeIntoRoute, driving time to the milestones in seconds.
     */
    public void getMilestones(int timeIntoRoute){
        // TODO Ask the route how far it is to the time specified.
        // LatLng position = currentRoute.getLatLngAtTime(timeIntoRoute); Should return a location that is in a "safe" distance from the real location
        // Ranking.getMilestones(position, 2); 2 is the degrees
        //map.addMarker(new MarkerOptions().)

        // TODO This should be done in the activity.
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LatLng p = marker.getPosition();
                //View v = getLayoutInflater().inflate();

                //Button b = new Button(this);


                /*b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //IMilestone m = currentRoute.matchMilestone(p);
                        //listOfPrelM.add(m); // Add to list

                        // When finished and user selected "Submit/Start nav" then : map.getRoute.addMilestones(listOfPrelM);
                    }
                });*/
                return null;
            }
        });
    }

    public List getAddressFromSearch(String input){
        // TODO Fix or delete!
        List<String> addresList = null;
        URL url = NavigationUtils.makeURL(input);
        //Remote.get(url, routeHandler);

        return addresList;
    }
}
