package com.edit.reach.model;

import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joakim on 2014-10-16.
 * A leg is a part of a route and contains several smaller legs called Steps.
 */
public class Leg {
    private List<Step> steps;
    private long duration;	// Seconds
    private long distance;	// Metres
    private LatLng startLocation, endLocation;
    private String DEBUG_TAG = "Leg";

    public Leg(JSONObject legJSON){
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
                Log.d(DEBUG_TAG, "Step "+i+" duration: "+step.getDuration());
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

    public long getDuration() {
        return duration;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public long getDistance() {
        return distance;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }



}
