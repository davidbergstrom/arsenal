package com.edit.reach.model;

import android.util.Log;
import com.edit.reach.model.interfaces.IMilestone;
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
    //private long duration;	// Seconds
    //private long distance;	// Metres
    //private LatLng startLocation, endLocation;
    private IMilestone endMilestone; // Milestones at the end of this leg
    private String DEBUG_TAG = "Leg";

    public Leg(JSONObject legJSON){
        Log.d(DEBUG_TAG, "Creating leg.");
        steps = new ArrayList<Step>();
        try {
            //distance = legJSON.getJSONObject("distance").getLong("value");
            long duration = legJSON.getJSONObject("duration").getLong("value");
            //JSONObject startPosition = legJSON.getJSONObject("start_location");
            //this.startLocation = new LatLng(startPosition.getDouble("lat"), startPosition.getDouble("lng"));
            //JSONObject endPosition = legJSON.getJSONObject("end_location");
            //this.endLocation = new LatLng(endPosition.getDouble("lat"), endPosition.getDouble("lng"));

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
     * Set a milestone at the end of this leg
     * @param milestone, the milestone this leg is heading to
     */
    void setMilestone(IMilestone milestone){
        this.endMilestone = milestone;
    }

    /**
     * Returns the milestone at the end of the leg. Will return null if there is no milestone there.
     * (That means that it is the last leg of the route and the end is the destination)
     * @return the milestone at the end of the leg
     */
    IMilestone getMilestone(){
        return endMilestone;
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

    public float getDuration() {
        float duration = 0;
        for(Step step : steps){
            duration += step.getDuration();
            Log.d(DEBUG_TAG, "Step duration: " + step.getDuration());
        }
        return duration;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public float getDistance() {
        float distance = 0;
        for(Step step : steps){
            distance += step.getDistance();
        }
        return distance;
    }

    public LatLng getStartLocation() {
        return steps.get(0).getStartLocation();
    }

    public LatLng getEndLocation() {
        return steps.get(steps.size()-1).getEndLocation();
    }

}
