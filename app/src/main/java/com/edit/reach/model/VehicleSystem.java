package com.edit.reach.model;

import android.swedspot.automotiveapi.AutomotiveSignal;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.swedspot.scs.data.*;
import android.util.Log;
import com.swedspot.automotiveapi.AutomotiveFactory;
import com.swedspot.automotiveapi.AutomotiveListener;
import com.swedspot.automotiveapi.AutomotiveManager;
import com.swedspot.vil.distraction.DriverDistractionLevel;
import com.swedspot.vil.distraction.DriverDistractionListener;
import com.swedspot.vil.policy.AutomotiveCertificate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

/** Enum with 4 different types of signals.
 */
enum SIGNAL_TYPE {
	LOW_FUEL, SHORT_TIME, SHORT_TO_SERVICE, VEHICLE_STOPPED_OR_STARTED
}

/**
 * Class that represents a VehicleSystem (Or a Vehicle).
 * Created by: Tim Kerschbaumer
 * Project: REACH
 * Date: 2014-09-27
 * Time: 19:28
 * Last Edit: 2014-10-02
 */
class VehicleSystem extends Observable {

	/* --- Instance Variables --- */
	private List<Integer> signalIDs;

	private SCSFloat fuelLevel;
	private SCSFloat instantFuelConsumption;
	private SCSFloat instantFuelEconomy;
	private SCSFloat vehicleSpeed;

	private Uint8 movingDirection;
	private Uint8 isMoving;
	private Uint8 card;

	private SCSInteger distanceToService;
	private SCSInteger totalVehicleDistance;

	private SCSDouble totalFuelUsed;
	private SCSLong totalHoursOfOperation;
	private Uint16 totalWeight;

	// Needs initial values to avoid null pointer.
	private Uint8 workingState = new Uint8(-1);
	private Uint8 timeRelativeWorkingState = new Uint8(-1);

	// TODO Null? What to have in constructor
	private final AutomotiveCertificate automotiveCertificate = new AutomotiveCertificate(null);

	// TODO What to do here?
	private final DriverDistractionListener driverDistractionListener = new DriverDistractionListener() {
		@Override
		public void levelChanged(DriverDistractionLevel driverDistractionLevel) {
		}
	};

	private final AutomotiveListener automotiveListener = new AutomotiveListener() {
		@Override
		public void receive(AutomotiveSignal automotiveSignal) {

			// Switch between incoming signals
			switch (automotiveSignal.getSignalId()) {

				// How much fuel is left in tank.
				case AutomotiveSignalId.FMS_FUEL_LEVEL_1:
					fuelLevel = (SCSFloat)(automotiveSignal.getData());

					// Call method to determine critical states
					determineLowFuel(fuelLevel.getFloatValue());

					break;

				// Working state of driver
				case AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE:
					Uint8 prevWorkState = workingState;
					workingState = (Uint8)automotiveSignal.getData();

					// Call method to determine critical states
					determineIfStoppedOrStarted(prevWorkState.getIntValue(), workingState.getIntValue());

					break;

				// Time in states of driver
				case AutomotiveSignalId.FMS_DRIVER_1_TIME_REL_STATES:
					Uint8 prevTimeRelativeWorkingState = timeRelativeWorkingState;
					timeRelativeWorkingState = (Uint8)automotiveSignal.getData();

					// Call method to determine critical states
					determineShortTime(prevTimeRelativeWorkingState.getIntValue(), timeRelativeWorkingState.getIntValue());

					break;

				// Distance to service
				case AutomotiveSignalId.FMS_SERVICE_DISTANCE:
					distanceToService = (SCSInteger)(automotiveSignal.getData());

					// Call method to determine critical states
					determineCloseToService(distanceToService.getIntValue());

					break;

				// TODO, should this signal call a method to determine critical state?
				// Is vehicle moving
				case AutomotiveSignalId.FMS_VEHICLE_MOTION:
					isMoving = (Uint8)(automotiveSignal.getData());
					break;

				// Instantaneous Fuel consumption
				case AutomotiveSignalId.FMS_FUEL_RATE:
					instantFuelConsumption = (SCSFloat)automotiveSignal.getData();
					break;

				// Instantaneous Fuel economy
				case AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY:
					instantFuelEconomy = (SCSFloat)automotiveSignal.getData();
					break;

				// Vehicle speed (Tachograph)
				case AutomotiveSignalId.FMS_TACHOGRAPH_VEHICLE_SPEED:
					vehicleSpeed = ((SCSFloat)automotiveSignal.getData());
					break;

				// Moving direction
				case AutomotiveSignalId.FMS_DIRECTION_INDICATOR:
					movingDirection = ((Uint8)automotiveSignal.getData());
					break;

				// Has a driver
				case AutomotiveSignalId.FMS_DRIVER_1_CARD:
					card = (Uint8)automotiveSignal.getData();
					break;

				// Total hours of operation
				case AutomotiveSignalId.FMS_ENGINE_TOTAL_HOURS_OF_OPERATION:
					totalHoursOfOperation = (SCSLong)automotiveSignal.getData();
					break;

				// Total fuel used
				case AutomotiveSignalId.FMS_HIGH_RESOLUTION_ENGINE_TOTAL_FUEL_USED:
					totalFuelUsed = (SCSDouble)automotiveSignal.getData();
					break;

				// Total vehicle distance
				case AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE:
					totalVehicleDistance = (SCSInteger)automotiveSignal.getData();
					break;

				// Total weight of the vehicle
				case AutomotiveSignalId.FMS_GROSS_COMBINATION_VEHICLE_WEIGHT:
					totalWeight = (Uint16)automotiveSignal.getData();
					break;

				default:
					// TODO What Default?
					break;
			}
		}

		@Override
		public void timeout(int i) {
			// TODO Something here?
			Log.d("TIMEOUT", "Signal ID: " + i);
		}

		@Override
		public void notAllowed(int i) {
			// TODO Something here?
			Log.d("NOTALLOWED", "Signal ID: " + i);
		}
	};

	// The Automotive manager.
	private final AutomotiveManager manager = AutomotiveFactory.createAutomotiveManagerInstance(automotiveCertificate, automotiveListener, driverDistractionListener);

	/* --- CONSTANTS --- */

	// Multiply with this to convert nanoseconds to minutes.
	private static final double NANOSECONDS_TO_MINUTES = (Math.pow(10,-9))/60.0;

	// TODO tank-size should be determined dynamically.
	// A fictitious tank size
	private static final int TEMP_TANK_SIZE_IN_LITERS = 600;

	// The maximum number of minutes to drive before a 45 minute break.
	private static final int LEGAL_UPTIME_IN_MINUTES = 270;

	// TODO, Any parameters? Should it do more things?
	/** Constructor.
	 */
	VehicleSystem() {
		signalIDs = new ArrayList<Integer>();

		// TODO Which signals to register?
		// signalIDs.add(AutomotiveSignalId.FMS_FUEL_LEVEL_1);
		// signalIDs.add(AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE);
		// signalIDs.add(AutomotiveSignalId.FMS_DRIVER_1_TIME_REL_STATES);
		// signalIDs.add(AutomotiveSignalId.FMS_SERVICE_DISTANCE);
		// signalIDs.add(AutomotiveSignalId.FMS_VEHICLE_MOTION);
		// signalIDs.add(AutomotiveSignalId.FMS_FUEL_RATE);
		// signalIDs.add(AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY);
		// signalIDs.add(AutomotiveSignalId.FMS_TACHOGRAPH_VEHICLE_SPEED);
		// signalIDs.add(AutomotiveSignalId.FMS_DIRECTION_INDICATOR);
		// signalIDs.add(AutomotiveSignalId.FMS_DRIVER_1_CARD);

		registerListeners(signalIDs);
	}

	// ****** PACKAGE GET-METHODS ****** //

	// TODO, do the math
	/** Estimates how long until a refuel is recommended.
	 * @return how many km until a stop for fueling is recommended.
	 */
	double getKilometersUntilRefuel() {
		return 0;
	}

	// TODO, do the math
	/** Method that returns the number of minutes until a stop i required.
	 * @return 270 if currently in a break, Positive number with minutes left, if drive longer than legal.
	 */
	double getTimeUntilForcedBreak() {
		return 0;
	}

	// TODO, do the math
	/** Method that returns the number om kilometers until service is recommended.
	 * @return how many km until a stop for service is recommended.
	 */
	double getKilometersUntilService() {
		return 0;
	}

	// TODO, do the math
	/** Method that returns the current state of the vehicle.
	 * @return
	1 - Vehicle is moving.
	2 - Engine is on but vehicle is not moving.
	3 - Engine is of and vehicle is not moving.
	 */
	int getVehicleState() {
		return 0;
	}
	// ****** END ****** //


	// ****** PRIVATE GET-METHODS FOR SIGNALS. ****** //
	// ****** USE THESE IF NO LISTENERS ARE ACTIVE ON THE SIGNAL ****** //
	// ****** These methods request a value from the vehicle ****** //

	/* Gets the amount of fuel in the vehicles tank.
	 * @return A float with Percent of fuel left in tank.
	 */
	private float getFuelLevel() {
		manager.requestValue(AutomotiveSignalId.FMS_FUEL_LEVEL_1);
		return fuelLevel.getFloatValue();
	}

	/* Returns if the vehicle is moving or not.
	 * @return false if not moving, true if moving
	 */
	private boolean isMoving() {
		manager.requestValue(AutomotiveSignalId.FMS_VEHICLE_MOTION);
		int moving = isMoving.getIntValue();
		return(moving != 0);
	}

	/* The Fuel consumed by the engine in Liters per Hour
	 * @return a float with the consumption in L/h
	 */
	private float getInstantFuelConsumption() {
		manager.requestValue(AutomotiveSignalId.FMS_FUEL_RATE);
		return instantFuelConsumption.getFloatValue();
	}

	/* The fuel economy at current velocity in km/l
	 * @return a float with the current economy in km/l
	 */
	private float getInstantFuelEconomy() {
		manager.requestValue(AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY);
		return instantFuelEconomy.getFloatValue();
	}

	/* The distance in km to next service.
	 * @return a int with the number of km to next service.
	 */
	private int getDistanceToService() {
		manager.requestValue(AutomotiveSignalId.FMS_SERVICE_DISTANCE);
		return distanceToService.getIntValue();
	}

	/* The speed of the vehicle.
	 * @return a float with the current speed of the vehicle in km/h
	 */
	private float getVehicleSpeed() {
		manager.requestValue(AutomotiveSignalId.FMS_TACHOGRAPH_VEHICLE_SPEED);
		return vehicleSpeed.getFloatValue();
	}

	/* The moving direction of the vehicle.
	 * @return a int with 0 if moving forward and 1 if moving reverse.
	 */
	private int getMovingDirection() {
		manager.requestValue(AutomotiveSignalId.FMS_DIRECTION_INDICATOR);
		return movingDirection.getIntValue();
	}

	/* If the vehicle has an inserted driver card.
	 * @return true if a card is inserted. False otherwise
	 */
	private boolean hasDriverCard() {
		manager.requestValue(AutomotiveSignalId.FMS_DRIVER_1_CARD);
		return (card.getIntValue() != 0);
	}

	/* Returns the state of the driver.
	 * @return  An int with
	            - 0: Rest
				- 1: Driver available
				- 2: Work
				- 3: Drive
				- 6: Error
				- 7: Not available
	 */
	private int getWorkingState() {
		manager.requestValue(AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE);
		return (workingState.getIntValue());
	}

	/* Indicates if the driver approaches or exceeds workings time limit.
 	 * @return An int with
	            - 0: Normal
				- 1: 15 min bef. 4 h 30 min
				- 2: 4 h 30 min reached
				- 3: 15 min bef. 9 h
				- 4: 9 h reached
				- 5: 15 min bef. 16 h
				- 6: 16h reached
				- 14: Error
				- 15: Not available
	 */
	private int getTimeRelativeWorkingState() {
		manager.requestValue(AutomotiveSignalId.FMS_DRIVER_1_TIME_REL_STATES);
		return (timeRelativeWorkingState.getIntValue());
	}

	/* Returns the total fuel used by the vehicle.
	 * @return the total liters the truck as consumed as a double.
	 */
	private double getTotalFuelUsed() {
		manager.requestValue(AutomotiveSignalId.FMS_HIGH_RESOLUTION_ENGINE_TOTAL_FUEL_USED);
		return totalFuelUsed.getDoubleValue();
	}

	/* Returns the total vehicle distance travelled.
	 * @return the total distance in meters as an int
	 */
	private int getTotalDistanceTravelled() {
		manager.requestValue(AutomotiveSignalId.FMS_HIGH_RESOLUTION_ENGINE_TOTAL_FUEL_USED);
		return totalVehicleDistance.getIntValue();
	}

	/* Returns the total weight of the vehicle and attached trailers.
	 * @return the weight in kg as an int
	 */
	private int getVehicleWeight() {
		manager.requestValue(AutomotiveSignalId.FMS_GROSS_COMBINATION_VEHICLE_WEIGHT);
		return totalWeight.getIntValue();
	}

	/* Returns the total hours of operation of the vehicle.
	 * @return the number of hours the vehicle has been in operation as a long.
	 */
	private long getTotalOperationHours() {
		manager.requestValue(AutomotiveSignalId.FMS_ENGINE_TOTAL_HOURS_OF_OPERATION);
		return totalHoursOfOperation.getLongValue();
	}
	// ****** END ****** //


	// ********** PRIVATE HELP METHODS ********** //

	// TODO
	private void determineLowFuel(float fuelLevel) {
		if(fuelLevel <= 10f) {
			setChanged();
			notifyObservers(SIGNAL_TYPE.LOW_FUEL);
		}
	}

	// TODO
	private void determineShortTime(int prevState, int curState) {
		if(prevState != curState) {
			if(curState >= 1 && curState <= 6) {
				setChanged();
				notifyObservers(SIGNAL_TYPE.SHORT_TIME);
			}
		}
	}

	// TODO
	private void determineCloseToService(int kmToService) {
		if(kmToService <= 100) {
			setChanged();
			notifyObservers(SIGNAL_TYPE.SHORT_TO_SERVICE);
		}
	}

	// TODO
	private void determineIfStoppedOrStarted(int prevState, int curState) {
		if(prevState != curState) {
			setChanged();
			notifyObservers(SIGNAL_TYPE.VEHICLE_STOPPED_OR_STARTED);
		}
	}

	// Method that sets and determines start- and stop time
	// TODO Set starttime and/or stoptime
	private void setTime() {

	}
	// ****** END ****** //


	// ****** PRIVATE METHODS THAT HAS TO DO WITH LISTENERS ****** //

	// TODO, needed?
	/* Register listeners for identities in the list.
	 * @param identities the ID:s for the signals to be listened to.
	 */
	private void registerListeners(List<Integer> identities) {
		if(signalIDs != null) {
			Iterator<Integer> it = identities.iterator();
			while(it.hasNext()) {
				if(!signalIDs.contains(it.next())) {
					signalIDs.add(it.next());
				}
			}
		} else {
			signalIDs = identities;
		}

		Iterator<Integer> it = identities.iterator();
		while(it.hasNext()) {
			manager.register((int)it.next());
		}
	}

	// TODO, needed?
	/* Unregister listeners for ID:s in the list.
	 * @param identities the ID:S for the signals to be unregistered from listening.
	 */
	private void unregisterListeners(List<Integer> identities) {
		Iterator<Integer> it = identities.iterator();
		while(it.hasNext()) {
			if(signalIDs.contains(it.next())) {
				manager.unregister((int)it.next());
				signalIDs.remove((int) it.next());
			}
		}
	}

	// TODO, needed?
	// Method that determines if a signal is listened to.
	private boolean isRegistered(int signalId) {
		boolean isListenedAt = false;
		Iterator<Integer> it = signalIDs.iterator();
		while(it.hasNext()) {
			if(it.next() == signalId) {
				isListenedAt = true;
				break;
			}
		}
		return isListenedAt;
	}
}
