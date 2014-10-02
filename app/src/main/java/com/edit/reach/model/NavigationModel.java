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
 * Last Edit: 2014-10-02
 */
// TODO Everything that has to do with the stationary view
// TODO Run this class in separate thread.
public class NavigationModel implements Observer {

	private final VehicleSystem vehicleSystem;

	// TODO
	/** Constructor...
	 */
	public NavigationModel() {
		vehicleSystem = new VehicleSystem();
		vehicleSystem.addObserver(this);
	}

	/** Do not call this method. It is called automatically when the observable changes.
	 * @param observable Not used
	 * @param data The id of the signal that initiated this update.
	 */
	@Override
	public void update(Observable observable, Object data) {
		if(data.getClass() == SIGNAL_TYPE.LOW_FUEL.getClass()) {
			// TODO, do stuff. Vehicle is low on fuel.
			vehicleSystem.getKilometersUntilRefuel();

		} else if (data.getClass() == SIGNAL_TYPE.SHORT_TIME.getClass()) {
			// TODO, do stuff. Vehicle is short on time.
			vehicleSystem.getTimeUntilForcedBreak();

		} else if (data.getClass() == SIGNAL_TYPE.SHORT_TO_SERVICE.getClass()) {
			// TODO, do stuff. Vehicle needs service soon.
			vehicleSystem.getKilometersUntilService();

		} else if (data.getClass() == SIGNAL_TYPE.VEHICLE_STOPPED_OR_STARTED.getClass()) {
			// TODO, do stuff. Vehicle started or stopped.
			vehicleSystem.getVehicleState();

		} else {
			Log.d("TYPE ERROR", "Type error in update");
		}

	}
}
