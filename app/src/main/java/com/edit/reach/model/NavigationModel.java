package com.edit.reach.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.Observable;
import java.util.Observer;

/**
 * Class that merges data from the vehicle and the map. The class finds optimal stops for the trip.
 * Created by: Tim Kerschbaumer
 * Project: REACH
 * Date: 2014-09-27
 * Time: 19:27
 * Last Edit: 2014-10-10
 */
// TODO Run this class in separate thread.
public class NavigationModel implements Observer {

	private VehicleSystem vehicleSystem;
	private Map map;

	public NavigationModel(GoogleMap googleMap) {
		map = new Map(googleMap);
		vehicleSystem = new VehicleSystem();
		vehicleSystem.addObserver(this);
	}

	/** Returns a IMilestone matching the lat and longitude.
	 * @param latLng
	 * @return a IMilestone, null if no milestone is not found.
	 */
	public IMilestone getMatchedMilestone(LatLng latLng) {
		return map.getRoute().getMilestone(latLng);
	}

	/** Sets the route in the map.
	 * @param newRoute the route to be set.
	 */
	public void setRoute(final Route newRoute) {
		map.setRoute(newRoute);
		newRoute.addListener(new RouteListener() {
			@Override
			public void onInitialization() {
				map.getRoute().addPause(vehicleSystem.getKilometersUntilRefuel());
				long routeTime = map.getRoute().getDuration();
				long nmbrOfPauses = routeTime/vehicleSystem.getLegalUptimeInseconds();

				for(int i = 1; i < nmbrOfPauses; i++) {
					map.getRoute().addPause(i*vehicleSystem.getLegalUptimeInseconds());
				}
			}

			@Override
			public void onPauseAdded(LatLng pauseLocation) {
				// TODO what here?
			}
		});
	}

	/** Do not call this method. It is called automatically when the observable changes.
	 * @param observable Not used
	 * @param data The id of the signal that initiated this update.
	 */
	@Override
	public void update(Observable observable, Object data) {
		if(data == SIGNAL_TYPE.LOW_FUEL) {
			Log.d("UPDATE", "TYPE: LOW_FUEL");
			Log.d("GET", "Km to refuel: " + vehicleSystem.getKilometersUntilRefuel());
		} else if (data == SIGNAL_TYPE.SHORT_TIME) {
			Log.d("UPDATE", "TYPE: SHORT_TIME");
			Log.d("GET", "Time until rest: " + vehicleSystem.getTimeUntilForcedRest());
		} else if (data == SIGNAL_TYPE.SHORT_TO_SERVICE) {
			Log.d("UPDATE", "TYPE: SHORT_TO_SERVICE");
			Log.d("GET", "Km to service: " + vehicleSystem.getKilometersUntilService());
		} else if (data == SIGNAL_TYPE.VEHICLE_STOPPED_OR_STARTED) {
			Log.d("UPDATE", "TYPE: VEHICLE_STOPPED_OR_STARTED");
			Log.d("GET", "Vehicle State: " + vehicleSystem.getVehicleState());
		} else {
			Log.d("TYPE ERROR", "Type error in update");
		}
	}
}
