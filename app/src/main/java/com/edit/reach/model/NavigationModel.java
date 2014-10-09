package com.edit.reach.model;

import android.util.Log;

import java.util.Observable;
import java.util.Observer;

/**
 * Class that merges data from the vehicle and the map. The class finds optimal stops for the trip.
 * Created by: Tim Kerschbaumer
 * Project: REACH
 * Date: 2014-09-27
 * Time: 19:27
 * Last Edit: 2014-10-08
 */
// TODO Run this class in separate thread.
public class NavigationModel implements Observer {

	private final VehicleSystem vehicleSystem;

	// TODO
	/** Constructor...
	 */
	public NavigationModel() {
		Log.d("NavigationModel", "Navmodel created");
		vehicleSystem = new VehicleSystem();
		vehicleSystem.addObserver(this);
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
			Log.d("GET", "Time until rest: " +  vehicleSystem.getTimeUntilForcedRest());
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
