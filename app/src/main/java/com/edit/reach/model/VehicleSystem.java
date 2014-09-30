package com.edit.reach.model;

import android.swedspot.automotiveapi.AutomotiveSignal;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.swedspot.scs.SCS;
import android.swedspot.scs.data.*;
import android.util.Log;
import com.swedspot.automotiveapi.AutomotiveFactory;
import com.swedspot.automotiveapi.AutomotiveListener;
import com.swedspot.automotiveapi.AutomotiveManager;
import com.swedspot.vil.distraction.DriverDistractionLevel;
import com.swedspot.vil.distraction.DriverDistractionListener;
import com.swedspot.vil.policy.AutomotiveCertificate;

/**
 * Created by: Tim Kerschbaumer
 * Project: REACH
 * Date: 2014-09-27
 * Time: 19:28
 * Last Edit: 2014-09-30
 */
public class VehicleSystem {

	/* --- Instance Variables --- */
	private SCSFloat fuelLevel;
	private SCSFloat instantFuelConsumption;
	private SCSFloat instantFuelEconomy;
	private SCSFloat vehicleSpeed;
	private SCSInteger distanceToService;
	private Uint8 movingDirection;
	private Uint8 isMoving;

	// TODO Null? What to have in constructor
	private final AutomotiveCertificate automotiveCertificate = new AutomotiveCertificate(null);

	private final AutomotiveListener automotiveListener = new AutomotiveListener() {
		@Override
		public void receive(AutomotiveSignal automotiveSignal) {
			// Incoming signal
			switch (automotiveSignal.getSignalId()) {

				// How much fuel is left in tank.
				case AutomotiveSignalId.FMS_FUEL_LEVEL_1:
					setFuel((SCSFloat)(automotiveSignal.getData()));
					break;

				// Is vehicle moving
				case AutomotiveSignalId.FMS_VEHICLE_MOTION:
					setMoving((Uint8)(automotiveSignal.getData()));
					break;

				// Instantanious Fuelconsumption
				case AutomotiveSignalId.FMS_FUEL_RATE:
					setInstantaneousFuelConsumption((SCSFloat)automotiveSignal.getData());
					break;

				// Instantanious Fueleconomy
				case AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY:
					setInstantFuelEconomy((SCSFloat)automotiveSignal.getData());
					break;

				// Distance to service
				case AutomotiveSignalId.FMS_SERVICE_DISTANCE:
					setDistanceToService((SCSInteger)(automotiveSignal.getData()));
					break;

				// Vehicle speed (Tachograph)
				case AutomotiveSignalId.FMS_TACHOGRAPH_VEHICLE_SPEED:
					setVehicleSpeed((SCSFloat)automotiveSignal.getData());
					break;

				// Vehicle speed (Wheels)
				case AutomotiveSignalId.FMS_WHEEL_BASED_SPEED:
					setVehicleSpeed((SCSFloat)automotiveSignal.getData());
					break;

				// Moving direction
				case AutomotiveSignalId.FMS_DIRECTION_INDICATOR:
					setMovingDirection((Uint8)automotiveSignal.getData());
					break;

				// Identification of driver1 / driver2
				case AutomotiveSignalId.FMS_DRIVER_1_DRIVER_2_IDENTIFICATION:
					// TODO
					break;

				// Has a driver in slot 1.
				case AutomotiveSignalId.FMS_DRIVER_1_CARD:
					// TODO
					break;

				// Working state of driver in slot 1.
				case AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE:
					// TODO
					break;

				// Time in states of driver in slot 1.
				case AutomotiveSignalId.FMS_DRIVER_1_TIME_REL_STATES:
					// TODO
					break;

				// Has a driver in slot 2.
				case AutomotiveSignalId.FMS_DRIVER_2_CARD:
					// TODO
					break;

				// Working state of driver in slot 2.
				case AutomotiveSignalId.FMS_DRIVER_2_WORKING_STATE:
					// TODO
					break;

				// Time in states of driver in slot 2.
				case AutomotiveSignalId.FMS_DRIVER_2_TIME_REL_STATES:
					// TODO
					break;

				default:
					// Something
					break;
			}
		}

		@Override
		public void timeout(int i) {
			Log.d("TIMEOUT", "Signal ID: " + i);
		}

		@Override
		public void notAllowed(int i) {
			Log.d("NOTALLOWED", "Signal ID: " + i);
		}
	};

	private final DriverDistractionListener driverDistractionListener = new DriverDistractionListener() {
		@Override
		public void levelChanged(DriverDistractionLevel driverDistractionLevel) {
			// TODO what?
		}
	};

	private final AutomotiveManager manager = AutomotiveFactory.createAutomotiveManagerInstance(automotiveCertificate, automotiveListener, driverDistractionListener);

	public VehicleSystem() {
		// TODO Which signals to register?
	}

	/** Gets the amount of fuel in the vehicles tank.
	 * @return A float with Precent of fuel left in tank.
	 */
	public float getFuelLevel() {
		manager.requestValue(AutomotiveSignalId.FMS_FUEL_LEVEL_1);
		return fuelLevel.getFloatValue();
	}

	/** Returns if the vehicle is moving or not.
	 * @return false if not moving, true if moving
	 */
	public boolean isMoving() {
		manager.requestValue(AutomotiveSignalId.FMS_VEHICLE_MOTION);
		int moving = isMoving.getIntValue();
		return(moving != 0);
	}

	/** The Fuel consumed by the engine in Liters per Hour
	 * @return a float with the consumption in L/h
	 */
	public float getInstantFuelConsumption() {
		manager.requestValue(AutomotiveSignalId.FMS_FUEL_RATE);
		return instantFuelConsumption.getFloatValue();
	}

	/** The fuel economy at current velocity in km/l
	 * @return a float with the current economy in km/l
	 */
	public float getInstantFuelEconomy() {
		manager.requestValue(AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY);
		return instantFuelEconomy.getFloatValue();
	}

	/** The distance in km to next service.
	 * @return a int with the number of km to next service.
	 */
	public int getDistanceToService() {
		manager.requestValue(AutomotiveSignalId.FMS_SERVICE_DISTANCE);
		return distanceToService.getIntValue();
	}

	/** The speed of the vehicle.
	 * @return a float with the current speed of the vehicle in km/h
	 */
	public float getVehicleSpeed() {
		// TODO Check that tachograph is working properly.
		manager.requestValue(AutomotiveSignalId.FMS_TACHOGRAPH_VEHICLE_SPEED);
		return vehicleSpeed.getFloatValue();
	}

	/** The moving direction of the vehicle.
	 * @return a int with 0 if moving forward and 1 if moving reverse.
	 */
	public int getMovingDirection() {
		manager.requestValue(AutomotiveSignalId.FMS_DIRECTION_INDICATOR);
		return movingDirection.getIntValue();
	}

	private void setMovingDirection(Uint8 movingDirection) {
		this.movingDirection = movingDirection;
	}

	private void setVehicleSpeed(SCSFloat vehicleSpeed) {
		this.vehicleSpeed = vehicleSpeed;
	}

	private void setDistanceToService(SCSInteger distanceToService) {
		this.distanceToService = distanceToService;
	}

	private void setInstantFuelEconomy(SCSFloat instantFuelEconomy) {
		this.instantFuelEconomy = instantFuelEconomy;
	}

	private void setInstantaneousFuelConsumption(SCSFloat instantFuelConsumption) {
		this.instantFuelConsumption = instantFuelConsumption;
	}

	private void setMoving(Uint8 moving) {
		this.isMoving = moving;
	}

	private void setFuel(SCSFloat fuelLevel) {
		this.fuelLevel = fuelLevel;
	}



}
