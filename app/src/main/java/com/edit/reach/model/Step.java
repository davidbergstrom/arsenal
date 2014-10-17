package com.edit.reach.model;

import android.graphics.Color;
import android.text.Html;
import android.util.Log;
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
    private long distance, duration;
    private LatLng startLocation, endLocation;
    private String instructions;
    private Polyline polyline;
    private List<LatLng> subSteps;
    private String DEBUG_TAG = "Step";

    public Step(JSONObject stepJSON){
        Log.d(DEBUG_TAG, "Creating step.");
        try {
            distance = stepJSON.getJSONObject("distance").getLong("value");
            duration = stepJSON.getJSONObject("duration").getLong("value");
            JSONObject startLocation = stepJSON.getJSONObject("start_location");
            this.startLocation = new LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng"));
            JSONObject endLocation = stepJSON.getJSONObject("end_location");
            this.endLocation = new LatLng(endLocation.getDouble("lat"), endLocation.getDouble("lng"));
            try {
                instructions = URLDecoder.decode(Html.fromHtml(stepJSON.getString("html_instructions")).toString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            JSONObject polyline = stepJSON.getJSONObject("polyline");
            String encodedString = polyline.getString("points");
            subSteps = NavigationUtil.decodePoly(encodedString);
            Log.d(DEBUG_TAG, "Substepsize: "+subSteps.size());

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
        this.erase();
        polyline = map.addPolyline(new PolylineOptions().addAll(subSteps).width(12).color(Color.parseColor("#4411EE")));
    }

    /**
     * Erase the step from all of its maps
     */
    void erase(){
        if(polyline != null){
            polyline.remove();
        }
    }

    public List<LatLng> getSubSteps() {
        return subSteps;
    }

    public String getInstructions() {
        return instructions;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public long getDuration() {
        return duration;
    }

    public long getDistance() {
        return distance;
    }


}
