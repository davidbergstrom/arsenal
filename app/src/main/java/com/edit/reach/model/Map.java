package com.edit.reach.model;

import android.app.ProgressDialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
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
    private Location lastLocation;
    private Circle pointer;
    private static int UPDATE_INTERVAL = 300;

    /**
     * Construct a Map by providing a google map
     * @param map, the GoogleMap to use
     */
	public Map(GoogleMap map){
		this.map = map;
		this.milestones = new ArrayList<IMilestone>();
	}

    /**
     * Create a route
     * @param destination
     */
    public void setDestination(LatLng destination){
        Location myLocation = map.getMyLocation();
        LatLng currentPosition = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        String url = NavigationUtils.makeURL(currentPosition, destination, null, true);
        new connectAsyncTask(url).execute();
    }


    public void setRoute(LatLng origin, LatLng destination){
        String url = NavigationUtils.makeURL(origin, destination, null, true);
        new connectAsyncTask(url).execute();
    }

    public void setRoute(Route route, List<IMilestone> milestones){
        //String url = NavigationUtils.makeURL(origin, destination, null, true);
        // new connectAsyncTask(url).execute();
    }

    public void setRoute(LatLng source, LatLng dest, List<LatLng> wayPoints){
        if(currentRoute != null){
            currentRoute.remove();
        }
        String url = NavigationUtils.makeURL(source, dest, wayPoints, true);
        new connectAsyncTask(url).execute();
    }

    public Route getRoute() {
        return null;
    }

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

	public void stopRoute(){
        handler.removeCallbacks(navigationRunnable);
        if(currentRoute != null){
            currentRoute.remove();
        }
        map.getUiSettings().setAllGesturesEnabled(true);
	}

    public void startOverview(){

    }

    public void stopOverview(){

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
