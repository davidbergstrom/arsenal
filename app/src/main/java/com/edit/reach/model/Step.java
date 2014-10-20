package com.edit.reach.model;

import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import com.edit.reach.app.R;
import com.edit.reach.utils.NavigationUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 * Created by Joakim on 2014-10-16.
 * A step is a part of route with instructions.
 */
public class Step {
    private float distancePerSubStep, durationPerSubStep;
    private String instructions;
    private Polyline polyline;
    private List<LatLng> subSteps;
    private String DEBUG_TAG = "Step";

    /**
     * Constructs a step with the information retrieved from the provided JSONObject.
     * @param stepJSON the JSONObject to get information from
     */
    public Step(JSONObject stepJSON){
        Log.d(DEBUG_TAG, "Creating step.");
        try {
            JSONObject startLocation = stepJSON.getJSONObject("start_location");
            JSONObject endLocation = stepJSON.getJSONObject("end_location");
            try {
                instructions = URLDecoder.decode(Html.fromHtml(stepJSON.getString("html_instructions")).toString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            JSONObject polyline = stepJSON.getJSONObject("polyline");
            String encodedString = polyline.getString("points");
            subSteps = NavigationUtil.decodePoly(encodedString);
            Log.d(DEBUG_TAG, "Number of sub steps: " + subSteps.size());
            distancePerSubStep = (float)stepJSON.getJSONObject("distance").getDouble("value") / (subSteps.size()-1);
            durationPerSubStep = (float)stepJSON.getJSONObject("duration").getDouble("value") / (subSteps.size()-1);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Draw the step on the provided map
     * @param map, the map to draw on
     */
    void draw(GoogleMap map){
        polyline = map.addPolyline(new PolylineOptions()
                .addAll(subSteps)
                .width(14)
                .color(0xff0066ff));
    }

    /**
     * Erase the step from all of its maps
     */
    void erase(){
        if(polyline != null){
            polyline.remove();
        }
    }

    /**
     * Returns the sub steps of this step.
     * @return the sub steps
     */
    public List<LatLng> getSubSteps() {
        return subSteps;
    }

    /**
     * Returns the instructions of this step. These will help you get to the next step.
     * @return the instuctions
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * Returns the start location of this step.
     * @return the location at the start
     */
    public LatLng getStartLocation() {
        return subSteps.get(0);
    }

    /**
     * Returns the end location of this step.
     * @return the location at the end
     */
    public LatLng getEndLocation() {
        return subSteps.get(subSteps.size()-1);
    }

    /**
     * Returns the duration of this step.
     * @return the duration in seconds
     */
    public float getDuration() {
        return durationPerSubStep * (subSteps.size()-1);
    }

    /**
     * Returns the distance of this step.
     * @return the distance in metres
     */
    public float getDistance() {
        return distancePerSubStep * (subSteps.size()-1);
    }

}
