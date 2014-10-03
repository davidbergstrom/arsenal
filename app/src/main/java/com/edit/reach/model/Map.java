package com.edit.reach.model;

import android.app.ProgressDialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joakim Berntsson on 2014-09-29.
 * Class containing all logic for handling map actions.
 */
public class Map {
	private GoogleMap map;
	private Handler handler = new Handler();
	private List<IMilestone> milestones;
	private Route currentRoute;

	public Map(GoogleMap map){
		this.map = map;
		this.milestones = new ArrayList<IMilestone>();
	}

	public void setDestination(LatLng destination){
		Location myLocation = map.getMyLocation();
		LatLng currentPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
		String url = NavigationUtils.makeURL(currentPosition, destination, null, true);
		new connectAsyncTask(url).execute();
	}

	public void setRoute(LatLng source, LatLng dest, List<LatLng> wayPoints){
		if(currentRoute != null){
			currentRoute.remove();
		}
		String url = NavigationUtils.makeURL(source, dest, wayPoints, true);
		new connectAsyncTask(url).execute();
	}

	public void startRoute(){
		Location myLocation = map.getMyLocation();
		LatLng position = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

		this.moveCameraTo(position);
		this.zoomCameraTo(18);

		handler.post(navigationRunnable);
	}

	public void stopRoute(){
		handler.removeCallbacks(navigationRunnable);
		currentRoute.remove();
	}

	public void setMilestones(List<IMilestone> listOfMilestones){

	}

	public IMilestone getMilestone(IMilestone.Category category, double kmFromCurrentPosition){
		return null;
	}

	public List<IMilestone> getAllMilstones(double kmFromCurrentPosition){
		return null;
	}

	public void removeMilestone(int index){
		milestones.remove(index);
	}

	public void removeMilestone(IMilestone milestone){
		milestones.remove(milestone);
	}

	public void removeAllMilestones(){
		milestones.clear();
	}

	private void zoomCameraTo(int zoomLevel){
		map.moveCamera(CameraUpdateFactory.zoomTo(zoomLevel));
	}

	private void moveCameraTo(LatLng position){
		map.moveCamera(CameraUpdateFactory.newLatLng(position));
	}

	private Runnable navigationRunnable = new Runnable() {
		@Override
		public void run() {
			Location myLocation = map.getMyLocation();
			LatLng position = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

			// Move arrow to the current position on the route
			if(currentRoute != null){
				currentRoute.goTo(position);
			}

			// Move the camera to the current position
			moveCameraTo(position);

			handler.postDelayed(this, 400);
		}
	};

	private class connectAsyncTask extends AsyncTask<Void, Void, String> {
		private ProgressDialog progressDialog;
		String url;

		connectAsyncTask(String urlPass){
			url = urlPass;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//progressDialog = new ProgressDialog(context);
			//progressDialog.setMessage("Fetching route, Please wait...");
			//progressDialog.setIndeterminate(true);
			//progressDialog.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			JSONParser jParser = new JSONParser();
			return jParser.getJSONFromUrl(url);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			//progressDialog.hide();
			if(result != null){
				decodeResult(result);
			}
		}
	}

	private void decodeResult(String result) {

		try {
			//Tranform the string into a json object
			final JSONObject json = new JSONObject(result);
			JSONArray routeArray = json.getJSONArray("routes");
			JSONObject route = routeArray.getJSONObject(0);
			currentRoute = new Route(route);
			currentRoute.draw(map);
		}
		catch (JSONException ignored) {

		}
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
