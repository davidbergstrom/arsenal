package com.edit.reach.model;


import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by: Tim Kerschbaumer
 * Project: REACH
 * Date: 2014-09-27
 * Time: 19:27
 * Last Edit: 2014-10-01
 */
// TODO Everything that has to do with the stationairy view
// TODO Run this class in separate thread.
public class NavigationModel implements Observer {

	private final VehicleSystem vehicleSystem;

	// TODO
	/** Constructor...
	 */
	public NavigationModel() {
		List<Integer> signalList = new ArrayList<Integer>();
		vehicleSystem = new VehicleSystem();
		vehicleSystem.addObserver(this);
	}

	/** Do not call this method. It is called automatically when the observable changes.
	 * @param observable Not used
	 * @param data The id of the signal that initiated this update.
	 */
	@Override
	public void update(Observable observable, Object data) {
		int id;
		if(data.getClass() == Integer.TYPE) {
			id = (Integer)data;

			switch (id) {
				// How much fuel is left in tank.
				case AutomotiveSignalId.FMS_FUEL_LEVEL_1:
					// TODO do stuff
					break;

				// Is vehicle moving
				case AutomotiveSignalId.FMS_VEHICLE_MOTION:
					// TODO do stuff
					break;

				// Instantanious Fuelconsumption
				case AutomotiveSignalId.FMS_FUEL_RATE:
					// TODO do stuff
					break;

				// Instantanious Fueleconomy
				case AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY:
					// TODO do stuff
					break;

				// Distance to service
				case AutomotiveSignalId.FMS_SERVICE_DISTANCE:
					// TODO do stuff
					break;

				// Vehicle speed (Tachograph)
				case AutomotiveSignalId.FMS_TACHOGRAPH_VEHICLE_SPEED:
					// TODO do stuff
					break;

				// Moving direction
				case AutomotiveSignalId.FMS_DIRECTION_INDICATOR:
					// TODO do stuff
					break;

				// Has a driver
				case AutomotiveSignalId.FMS_DRIVER_1_CARD:
					// TODO do stuff
					break;

				// Working state of driver
				case AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE:
					// TODO do stuff
					break;

				// Time in states of driver
				case AutomotiveSignalId.FMS_DRIVER_1_TIME_REL_STATES:
					// TODO do stuff
					break;
			}
		} else {
			Log.d("OBSERVER", "Observer called without any correct ID");
		}
	}
}
