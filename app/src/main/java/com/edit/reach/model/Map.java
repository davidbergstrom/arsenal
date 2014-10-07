package com.edit.reach.model;

import android.app.ProgressDialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
    private Circle pointer;
    private static int UPDATE_INTERVAL = 300;
    private String logClass = "Map";

    /** A listener for a route */
    private RouteListener routeListener = new RouteListener(){

        @Override
        public void onInitialization() {
            Log.d(logClass, "Drawing Route");
            currentRoute.draw(map);
        }
    };

    /** A runnable for navigation */
    private Runnable navigationRunnable = new Runnable() {
        @Override
        public void run() {
            Location myLocation = map.getMyLocation();
            LatLng position = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            // Move arrow to the current position on the route
            if(currentRoute != null && !myLocation.equals(lastLocation)){
                LatLng pointerLocation = currentRoute.goTo(map, position);

                // float bearing = currentRoute.getBearing();

                // Move the camera to the current position
                //moveCameraTo(pointerLocation);
            }

            lastLocation = myLocation;
            handler.postDelayed(this, UPDATE_INTERVAL);
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

    }

    /**
     * Stop the current overview
     */
    public void stopOverview(){

    }

    /**
     * Get all milestones a certain amount of time into the current route.
     * @param timeIntoRoute, driving time to the milestones.
     */
    public void getMilestones(double timeIntoRoute){

    }

    private void decodeAddress(JSONObject address) {

        LatLng latLng = null;

        try {
            JSONObject location = address.getJSONObject("location");
            latLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));

        } catch (JSONException ignored) {

        }
    }
}
