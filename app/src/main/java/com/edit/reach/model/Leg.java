package com.edit.reach.model;

import android.util.Log;
import com.edit.reach.model.interfaces.IMilestone;
import com.edit.reach.utils.NavigationUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
    private IMilestone endMilestone; // Milestones at the end of this leg
    private Marker milestoneMarker;
    private String DEBUG_TAG = "Leg";

    /**
     * Constructs a leg with the information in the provided JSONObject.
     * @param legJSON the JSON to retrieve information from
     */
    public Leg(JSONObject legJSON){
        Log.d(DEBUG_TAG, "Creating leg.");
        steps = new ArrayList<Step>();
        try {
            JSONArray stepsArray = legJSON.getJSONArray("steps");
            for(int i = 0; i < stepsArray.length(); i++){
                Step step = new Step(stepsArray.getJSONObject(i));
                Log.d(DEBUG_TAG, "Added step " + step.toString() + " with duration: " + step.getDuration());
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
        if(milestone != null){
            Log.d(DEBUG_TAG, "Added " + milestone.getName() + " to " + this.toString());
        }else{
            Log.d(DEBUG_TAG, "Added no milestone to " + this.toString());
        }
        this.endMilestone = milestone;
    }

    /**
     * Returns the milestone at the end of the leg. Will return null if there is no milestone there.
     * (That means that it is the last leg of the route and the end is the destination)
     * @return the milestone at the end of the leg
     */
    public IMilestone getMilestone(){
        return endMilestone;
    }

    /**
     * Draw this leg on the provided map
     * @param map, the map to draw on
     */
    void draw(GoogleMap map){
        this.erase();

        if (endMilestone != null){
            BitmapDescriptor icon = NavigationUtil.getMilestoneIcon(endMilestone);

            milestoneMarker = map.addMarker(new MarkerOptions()
                    .position(endMilestone.getLocation())
                    .title(endMilestone.getName())
                    .icon(icon)
                    .snippet(endMilestone.getSnippet()));
        }

        for(Step step : steps){
            step.draw(map);
        }
    }

    /**
     * Erase the leg from all of its maps
     */
    void erase(){
        if(milestoneMarker != null){
            milestoneMarker.remove();
        }
        for(Step step : steps){
            step.erase();
        }
    }

    /**
     * Get the duration of this leg.
     * @return the duration in seconds
     */
    public float getDuration() {
        float duration = 0;
        for(Step step : steps){
            duration += step.getDuration();
        }
        return duration;
    }

    /**
     * Get the distance of this leg.
     * @return the distance in metres
     */
    public float getDistance() {
        float distance = 0;
        for(Step step : steps){
            distance += step.getDistance();
        }
        return distance;
    }

    /**
     * Get the steps for this leg.
     * @return the steps
     */
    public List<Step> getSteps() {
        return steps;
    }

    /**
     * Get the start location of this leg.
     * @return the location at the start
     */
    public LatLng getStartLocation() {
        return steps.get(0).getStartLocation();
    }

    /**
     * Get the end location of this leg.
     * @return the location at the end
     */
    public LatLng getEndLocation() {
        return steps.get(steps.size()-1).getEndLocation();
    }

}
