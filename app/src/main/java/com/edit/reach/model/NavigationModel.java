package com.edit.reach.model;

/**
 * Created by: Tim Kerschbaumer
 * Project: REACH
 * Date: 2014-09-27
 * Time: 19:27
 * Last Edit:
 */
// TODO Everything that has to do with the stationairy view
public class NavigationModel {

	private final VehicleSystem vehicleSystem;

	public NavigationModel() {
		vehicleSystem = new VehicleSystem();
	}

	public VehicleSystem getVehicleSystem() {
		return vehicleSystem;
	}


}
