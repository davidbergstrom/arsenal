package com.edit.reach.model;

import android.graphics.Color;
import android.text.Html;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.Integer;import java.lang.Math;import java.lang.String;import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Joakim Berntsson on 2014-10-01.
 * Route class containing information for one route
 */
public class Route {
    private List<Leg> legs;
    private Circle endPointCircle;
    private Circle pointer; // Should this be an individual class (following a route)?

    public Route(JSONObject route) throws JSONException {
        legs = new ArrayList<Leg>();
        // Set route start and end locations as variables with getters
        JSONArray arrayLegs = route.getJSONArray("legs");
        for(int i = 0; i < arrayLegs.length(); i++) {
            JSONObject legJSON = arrayLegs.getJSONObject(0);
            legs.add(new Leg(legJSON));
        }
    }

    public void draw(GoogleMap map){
        for(Leg leg : legs){
            leg.draw(map);
        }
        // Add an end point
        this.endPointCircle = map.addCircle(new CircleOptions()
                .center(legs.get(legs.size()-1).endLocation)
                .radius(10)
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE));
    }

    public void remove(){
        for(Leg leg : legs){
            leg.erase();
        }
        this.endPointCircle.remove();
        this.pointer.remove();
    }

    private LatLng evaluateNewLocation(LatLng rawLocation){
        Leg currentLeg = legs.get(0);
        Step nearestStep = currentLeg.steps.get(0);
        for(Step step : currentLeg.steps){
            LatLng location = step.startLocation;
            LatLng nearestLocation = nearestStep.startLocation;
            if(location.latitude - rawLocation.latitude <= nearestLocation.latitude - rawLocation.latitude &&
                    location.longitude - rawLocation.longitude <= nearestLocation.longitude - rawLocation.longitude){
                nearestStep = step;
            }else{
                break;
            }
        }

        return nearestStep.startLocation;
    }

    public LatLng goTo(GoogleMap map, LatLng location){
        LatLng nearestLocation = legs.get(0).steps.get(0).subSteps.get(0);
        Step stepToRedraw = legs.get(0).steps.get(0);

        // TODO: Remake recursive!
        outerLoop:
        for(Iterator<Leg> iteratorLeg = legs.iterator(); iteratorLeg.hasNext(); ){
            Leg leg = iteratorLeg.next();
            for(Iterator<Step> iteratorStep = leg.steps.iterator(); iteratorStep.hasNext(); ){
                Step step = iteratorStep.next();
                for(int i = 0; i < step.subSteps.size() - 1; i++){
                    LatLng subStep = step.subSteps.get(i);
                    LatLng subStepTwo = step.subSteps.get(i + 1);

                    double a = Math.pow(Math.sin(Math.toRadians(location.latitude-subStep.latitude)/2), 2) +
                            Math.cos(location.latitude) * Math.cos(subStep.latitude) *
                                    Math.pow(Math.sin(Math.toRadians(location.longitude-subStep.longitude)/2), 2);
                    double distance1 = getDistance(subStep, location); //R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

                    a = Math.pow(Math.sin(Math.toRadians(subStepTwo.latitude-location.latitude)/2), 2) +
                            Math.cos(subStepTwo.latitude) * Math.cos(location.latitude) *
                                    Math.pow(Math.sin(Math.toRadians(subStepTwo.longitude-location.longitude)/2), 2);
                    double distance2 = getDistance(location, subStepTwo); //R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
                    /*if(Math.abs(subStep.latitude - location.latitude) >= Math.abs(subStepTwo.latitude - location.latitude) &&
                            Math.abs(subStep.longitude - location.longitude) >= Math.abs(subStepTwo.longitude - location.longitude)){*/

                    if(distance2 <= distance1){
                        nearestLocation = subStepTwo;
                        step.subSteps.remove(i);
                    }else{
                        stepToRedraw = step;
                        break outerLoop; // Break the outer loop
                    }
                }
                step.erase();
                iteratorStep.remove();
                //leg.steps.remove(step);
            }
            leg.erase();
            iteratorLeg.remove();
            //legs.remove(leg);
        }

        if(legs.size() == 0){
            // Route finished!

            endPointCircle.remove();
        }else{
            if(stepToRedraw != null){
                stepToRedraw.redraw(map);
            }
            if(pointer != null && !pointer.getCenter().equals(nearestLocation)){
                pointer.remove();
                pointer = map.addCircle(new CircleOptions().center(nearestLocation).fillColor(Color.GREEN).radius(8));
                // Calculate new bearing and rotate the camera
                Step newStep = legs.get(0).steps.get(0);
                float bearing = (float) finalBearing(newStep.startLocation.latitude, newStep.startLocation.longitude,
                        newStep.endLocation.latitude, newStep.endLocation.longitude);
                CameraPosition lastPosition = map.getCameraPosition();
                CameraPosition currentPlace = new CameraPosition.Builder().target(nearestLocation).bearing(bearing)
                        .tilt(lastPosition.tilt).zoom(lastPosition.zoom).build();

                map.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));
            }else if(pointer == null){
                pointer = map.addCircle(new CircleOptions().center(nearestLocation).fillColor(Color.GREEN).radius(8));
            }

        }
        return nearestLocation;
    }

    private double getDistance(LatLng firstPosition, LatLng secondPosition){
        int R = 6371; // Earths radius in km
        double a = Math.pow(Math.sin(Math.toRadians(secondPosition.latitude-firstPosition.latitude)/2), 2) +
                Math.cos(secondPosition.latitude) * Math.cos(firstPosition.latitude) *
                        Math.pow(Math.sin(Math.toRadians(secondPosition.longitude-firstPosition.longitude)/2), 2);
        double distance1 = R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return distance1;
    }

    private static double finalBearing(double lat1, double long1, double lat2, double long2){
        return (_bearing(lat2, long2, lat1, long1) + 180.0) % 360;
    }

    private static double _bearing(double lat1, double long1, double lat2, double long2){
        double degToRad = Math.PI / 180.0;
        double phi1 = lat1 * degToRad;
        double phi2 = lat2 * degToRad;
        double lam1 = long1 * degToRad;
        double lam2 = long2 * degToRad;

        return Math.atan2(Math.sin(lam2-lam1)*Math.cos(phi2),
                Math.cos(phi1)*Math.sin(phi2) - Math.sin(phi1)*Math.cos(phi2)*Math.cos(lam2-lam1)) * 180/Math.PI;
    }

    /**
     * Class that represents a leg
     */
    private class Leg{
        public List<Step> steps;
        public int distance;	// Metres
        public int duration;	// Seconds
        public LatLng startLocation;
        public LatLng endLocation;

        Leg(JSONObject legJSON){
            steps = new ArrayList<Step>();
            JSONObject startPosition;
            JSONObject endPosition;
            try {
                distance = Integer.decode(legJSON.getJSONObject("distance").getString("value"));
                duration = Integer.decode(legJSON.getJSONObject("duration").getString("value"));
                startPosition = legJSON.getJSONObject("start_location");
                this.startLocation = new LatLng(startPosition.getDouble("lat"), startPosition.getDouble("lng"));
                endPosition = legJSON.getJSONObject("end_location");
                this.endLocation = new LatLng(endPosition.getDouble("lat"), endPosition.getDouble("lng"));

                JSONArray stepsArray = legJSON.getJSONArray("steps");
                for(int i = 0; i < stepsArray.length(); i++){
                    Step step = new Step(stepsArray.getJSONObject(i));
                    steps.add(step);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void draw(GoogleMap map){
            for(Step step : steps){
                step.draw(map);
            }
        }

        public void erase(){
            for(Step step : steps){
                step.erase();
            }
        }
    }

    /**
     * Class that represent every step of the directions. It store distance, location and instructions
     */
    private class Step{
        public int distance;
        public LatLng startLocation;
        public LatLng endLocation;
        public String instructions;
        private Polyline polyline;
        public List<LatLng> subSteps;

        Step(JSONObject stepJSON){
            JSONObject startLocation;
            JSONObject endLocation;
            try {
                distance = Integer.decode(stepJSON.getJSONObject("distance").getString("value"));
                startLocation = stepJSON.getJSONObject("start_location");
                this.startLocation = new LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng"));
                endLocation = stepJSON.getJSONObject("end_location");
                this.endLocation = new LatLng(endLocation.getDouble("lat"), endLocation.getDouble("lng"));
                try {
                    instructions = URLDecoder.decode(Html.fromHtml(stepJSON.getString("html_instructions")).toString(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                JSONObject polyline = stepJSON.getJSONObject("polyline");
                String encodedString = polyline.getString("points");
                subSteps = NavigationUtils.decodePoly(encodedString);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void draw(GoogleMap map){
            //map.addMarker(new MarkerOptions().position(startLocation).title(distance+"m").snippet(instructions));
            polyline = map.addPolyline(new PolylineOptions().addAll(subSteps).width(12).color(Color.parseColor("#4411EE")));
        }

        public void redraw(GoogleMap map){
            this.erase();
            polyline = map.addPolyline(new PolylineOptions().addAll(subSteps).width(12).color(Color.parseColor("#4411EE")));
        }

        public void erase(){
            polyline.remove();
        }
    }
}
