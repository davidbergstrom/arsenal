package com.edit.reach.model;

import android.graphics.Color;
import android.text.Html;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;import com.google.android.gms.maps.model.CircleOptions;import com.google.android.gms.maps.model.LatLng;import com.google.android.gms.maps.model.Polyline;import com.google.android.gms.maps.model.PolylineOptions;import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.Integer;import java.lang.Math;import java.lang.String;import java.net.URLDecoder;
import java.util.ArrayList;
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
	}

	public void goTo(LatLng location){
		Leg currentLeg = legs.get(0);
		Step currentStep = currentLeg.steps.get(0);

		if(Math.abs(currentStep.startLocation.latitude - location.latitude) > Math.abs(currentStep.endLocation.latitude - location.latitude) &&
				Math.abs(currentStep.startLocation.longitude - location.longitude) > Math.abs(currentStep.endLocation.longitude - location.longitude)){
			currentStep.erase();
			currentLeg.steps.remove(0);
		}

		if(currentLeg.steps.size() == 0){
			legs.remove(0);
		}

		if(legs.size() == 0){
			// Route finished!

			endPointCircle.remove();
		}

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

		public void removeStep(int index){
			if(steps.size() > 0){
				steps.get(0).erase();
				steps.remove(0);
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
		private List<LatLng> polylineLocations;

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
				polylineLocations = NavigationUtils.decodePoly(encodedString);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void draw(GoogleMap map){
			polyline = map.addPolyline(new PolylineOptions().addAll(polylineLocations).width(8).color(Color.BLUE));
		}

		public void erase(){
			polyline.remove();
		}
	}
}
