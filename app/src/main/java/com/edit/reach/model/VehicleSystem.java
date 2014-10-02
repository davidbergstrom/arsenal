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

import java.util.Iterator;
import java.util.List;
import java.util.Observable;

/**
 * Created by: Tim Kerschbaumer
 * Project: REACH
 * Date: 2014-09-27
 * Time: 19:28
 * Last Edit: 2014-10-02
 */
class VehicleSystem extends Observable {

	/* --- Instance Variables --- */
	private SCSFloat fuelLevel = new SCSFloat(-1f);
	private SCSFloat instantFuelConsumption = new SCSFloat(-1f);
	private SCSFloat instantFuelEconomy = new SCSFloat(-1f);
	private SCSFloat vehicleSpeed = new SCSFloat(-1f);

	private Uint8 movingDirection = new Uint8(-1);
	private Uint8 isMoving = new Uint8(-1);
	private Uint8 card = new Uint8(-1);
	private Uint8 workingState = new Uint8(-1);
	private Uint8 timeRelativeWorkingState = new Uint8(-1);

	private SCSInteger distanceToService = new SCSInteger(-1);
	private SCSInteger totalVehicleDistance;

	private SCSDouble totalFuelUsed;

	private SCSLong totalHoursOfOperation;

	private Uint16 totalWeight;

	private long driveStartTime = -1;
	private long driveStopTime = -1;

	private List<Integer> signalIDs;

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

			// Incoming signal
			switch (automotiveSignal.getSignalId()) {

				// How much fuel is left in tank.
				case AutomotiveSignalId.FMS_FUEL_LEVEL_1:
					SCSFloat prevFuelLevel = fuelLevel;
					fuelLevel = (SCSFloat)(automotiveSignal.getData());

					if(isRegistered(AutomotiveSignalId.FMS_FUEL_LEVEL_1) && prevFuelLevel.getFloatValue() != fuelLevel.getFloatValue()) {
						setChanged();
						notifyObservers(AutomotiveSignalId.FMS_FUEL_LEVEL_1);
					}
					break;

				// Is vehicle moving
				case AutomotiveSignalId.FMS_VEHICLE_MOTION:
					Uint8 prevIsMoving = isMoving;
					isMoving = (Uint8)(automotiveSignal.getData());

					if(isRegistered(AutomotiveSignalId.FMS_VEHICLE_MOTION) && prevIsMoving.getIntValue() != isMoving.getIntValue()) {
						setChanged();
						notifyObservers(AutomotiveSignalId.FMS_VEHICLE_MOTION);
					}
					break;

				// Instantanious Fuelconsumption
				case AutomotiveSignalId.FMS_FUEL_RATE:
					SCSFloat prevFuelRate = instantFuelConsumption;
					instantFuelConsumption = (SCSFloat)automotiveSignal.getData();

					if(isRegistered(AutomotiveSignalId.FMS_FUEL_RATE) && prevFuelRate.getFloatValue() != instantFuelConsumption.getFloatValue()) {
						setChanged();
						notifyObservers(AutomotiveSignalId.FMS_FUEL_RATE);
					}
					break;

				// Instantanious Fueleconomy
				case AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY:
					SCSFloat prevFuelEconomy = instantFuelEconomy;
					instantFuelEconomy = (SCSFloat)automotiveSignal.getData();

					if(isRegistered(AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY) && prevFuelEconomy.getFloatValue() != instantFuelEconomy.getFloatValue() ) {
						setChanged();
						notifyObservers(AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY);
					}
					break;

				// Distance to service
				case AutomotiveSignalId.FMS_SERVICE_DISTANCE:
					SCSInteger prevDistanceToService = distanceToService;
					distanceToService = (SCSInteger)(automotiveSignal.getData());

					if(isRegistered(AutomotiveSignalId.FMS_SERVICE_DISTANCE) && prevDistanceToService.getIntValue() != distanceToService.getIntValue() ) {
						setChanged();
						notifyObservers(AutomotiveSignalId.FMS_SERVICE_DISTANCE);
					}
					break;

				// Vehicle speed (Tachograph)
				case AutomotiveSignalId.FMS_TACHOGRAPH_VEHICLE_SPEED:
					SCSFloat prevVehicleSpeed = vehicleSpeed;
					vehicleSpeed = ((SCSFloat)automotiveSignal.getData());

					if(isRegistered(AutomotiveSignalId.FMS_TACHOGRAPH_VEHICLE_SPEED) && prevVehicleSpeed.getFloatValue() != vehicleSpeed.getFloatValue()) {
						setChanged();
						notifyObservers(AutomotiveSignalId.FMS_TACHOGRAPH_VEHICLE_SPEED);
					}
					break;

				// Moving direction
				case AutomotiveSignalId.FMS_DIRECTION_INDICATOR:
					Uint8 prevMovingDirection = movingDirection;
					movingDirection = ((Uint8)automotiveSignal.getData());

					if(isRegistered(AutomotiveSignalId.FMS_DIRECTION_INDICATOR) && prevMovingDirection.getIntValue() != movingDirection.getIntValue()) {
						setChanged();
						notifyObservers(AutomotiveSignalId.FMS_DIRECTION_INDICATOR);
					}
					break;

				// Has a driver
				case AutomotiveSignalId.FMS_DRIVER_1_CARD:
					Uint8 prevCard = card;
					card = (Uint8)automotiveSignal.getData();

					if(isRegistered(AutomotiveSignalId.FMS_DRIVER_1_CARD) && prevCard.getIntValue() != card.getIntValue()) {
						setChanged();
						notifyObservers(AutomotiveSignalId.FMS_DRIVER_1_CARD);
					}
					break;

				// Working state of driver
				case AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE:
					Uint8 prevWorkingState = workingState;
					workingState = (Uint8)automotiveSignal.getData();

					setTime(prevWorkingState.getIntValue(), workingState.getIntValue());

					if(isRegistered(AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE) && prevWorkingState.getIntValue() != workingState.getIntValue()) {
						setChanged();
						notifyObservers(AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE);
					}
					break;

				// Time in states of driver
				case AutomotiveSignalId.FMS_DRIVER_1_TIME_REL_STATES:
					Uint8 prevTimeRWS = timeRelativeWorkingState;
					timeRelativeWorkingState = (Uint8)automotiveSignal.getData();

					if(isRegistered(AutomotiveSignalId.FMS_DRIVER_1_TIME_REL_STATES) && prevTimeRWS.getIntValue() != timeRelativeWorkingState.getIntValue()) {
						setChanged();
						notifyObservers(AutomotiveSignalId.FMS_DRIVER_1_TIME_REL_STATES);
					}
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

	private final AutomotiveManager manager = AutomotiveFactory.createAutomotiveManagerInstance(automotiveCertificate, automotiveListener, driverDistractionListener);

	/* --- CONSTANTS --- */
	private static final double NANOSECONDS_TO_MINUTES = (Math.pow(10,-9))/60.0;

	// TODO should be determined dynamically.
	private static final int TEMP_TANK_SIZE_IN_LITERS = 600;

	private static final int LEGAL_UPTIME = 270;

	// TODO
	/** Default empty constructor.
	 */
	VehicleSystem() {
	}

	/** Constructor that registers listeners for ID:s given as parameter.
	 * @param identities The signal ID:s to be listened to.
	 */
	VehicleSystem(List<Integer> identities) {
		signalIDs = identities;
		this.registerListeners(signalIDs);
	}

	/** Register listeners for identities in the list.
	 * @param identities the ID:s for the signals to be listened to.
	 */
	void registerListeners(List<Integer> identities) {
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

	/** Unregister listeners for ID:s in the list.
	 * @param identities the ID:S for the signals to be unregistered from listening.
	 */
	void unregisterListeners(List<Integer> identities) {
		Iterator<Integer> it = identities.iterator();
		while(it.hasNext()) {
			if(signalIDs.contains(it.next())) {
				manager.unregister((int)it.next());
				signalIDs.remove((int) it.next());
			}
		}
	}

	/** Gets the amount of fuel in the vehicles tank.
	 * @return A float with Percent of fuel left in tank.
	 */
	float getFuelLevel() {
		if(!isRegistered(AutomotiveSignalId.FMS_FUEL_LEVEL_1)) {
			manager.requestValue(AutomotiveSignalId.FMS_FUEL_LEVEL_1);
		}
		return fuelLevel.getFloatValue();
	}

	/** Returns if the vehicle is moving or not.
	 * @return false if not moving, true if moving
	 */
	boolean isMoving() {
		if(!isRegistered(AutomotiveSignalId.FMS_VEHICLE_MOTION)) {
			manager.requestValue(AutomotiveSignalId.FMS_VEHICLE_MOTION);
		}
		int moving = isMoving.getIntValue();
		return(moving != 0);
	}

	/** The Fuel consumed by the engine in Liters per Hour
	 * @return a float with the consumption in L/h
	 */
	float getInstantFuelConsumption() {
		if(!isRegistered(AutomotiveSignalId.FMS_FUEL_RATE)) {
			manager.requestValue(AutomotiveSignalId.FMS_FUEL_RATE);
		}
		return instantFuelConsumption.getFloatValue();
	}

	/** The fuel economy at current velocity in km/l
	 * @return a float with the current economy in km/l
	 */
	float getInstantFuelEconomy() {
		if(!isRegistered(AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY)) {
			manager.requestValue(AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY);
		}
		return instantFuelEconomy.getFloatValue();
	}

	/** The distance in km to next service.
	 * @return a int with the number of km to next service.
	 */
	int getDistanceToService() {
		if(!isRegistered(AutomotiveSignalId.FMS_SERVICE_DISTANCE)) {
			manager.requestValue(AutomotiveSignalId.FMS_SERVICE_DISTANCE);
		}
		return distanceToService.getIntValue();
	}

	/** The speed of the vehicle.
	 * @return a float with the current speed of the vehicle in km/h
	 */
	float getVehicleSpeed() {
		// TODO Check that tachograph is working properly. If not, take wheel speed.
		if(!isRegistered(AutomotiveSignalId.FMS_TACHOGRAPH_VEHICLE_SPEED)) {
			manager.requestValue(AutomotiveSignalId.FMS_TACHOGRAPH_VEHICLE_SPEED);
		}
		return vehicleSpeed.getFloatValue();
	}

	/** The moving direction of the vehicle.
	 * @return a int with 0 if moving forward and 1 if moving reverse.
	 */
	int getMovingDirection() {
		if(!isRegistered(AutomotiveSignalId.FMS_DIRECTION_INDICATOR)) {
			manager.requestValue(AutomotiveSignalId.FMS_DIRECTION_INDICATOR);
		}
		return movingDirection.getIntValue();
	}

	/** If the vehicle has an inserted driver card.
	 * @return true if a card is inserted. False otherwise
	 */
	boolean hasDriverCard() {
		if(!isRegistered(AutomotiveSignalId.FMS_DRIVER_1_CARD)) {
			manager.requestValue(AutomotiveSignalId.FMS_DRIVER_1_CARD);
		}
		return (card.getIntValue() != 0);
	}

	/** Returns the total fuel used by the vehicle.
	 * @return the total liters the truck as consumed as a double.
	 */
	double getTotalFuelUsed() {
		manager.requestValue(AutomotiveSignalId.FMS_HIGH_RESOLUTION_ENGINE_TOTAL_FUEL_USED);
		return totalFuelUsed.getDoubleValue();
	}

	/** Returns the total vehicle distance travelled.
	 * @return the total distance in meters as an int
	 */
	int getTotalDistanceTravelled() {
		manager.requestValue(AutomotiveSignalId.FMS_HIGH_RESOLUTION_ENGINE_TOTAL_FUEL_USED);
		return totalVehicleDistance.getIntValue();
	}

	/** Returns the total weight of the vehicle and attached trailers.
	 * @return the weight in kg as an int
	 */
	int getVehicleWeight() {
		manager.requestValue(AutomotiveSignalId.FMS_GROSS_COMBINATION_VEHICLE_WEIGHT);
		return totalWeight.getIntValue();
	}

	/** Returns the total hours of operation of the vehicle.
	 * @return the number of hours the vehicle has been in operation as a long.
	 */
	long getTotalOperationHours() {
		manager.requestValue(AutomotiveSignalId.FMS_ENGINE_TOTAL_HOURS_OF_OPERATION);
		return totalHoursOfOperation.getLongValue();
	}

	/** Returns the state of the driver.
	 * @return  An int with
	            - 0: Rest
				- 1: Driver available
				- 2: Work
				- 3: Drive
				- 6: Error
				- 7: Not available
	 */
	int getWorkingState() {
		if(!isRegistered(AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE)) {
			manager.requestValue(AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE);
		}
		return (workingState.getIntValue());
	}

	/** Indicates if the driver approaches or exceeds workings time limit.
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
	int getTimeRelativeWorkingState() {
		if(!isRegistered(AutomotiveSignalId.FMS_DRIVER_1_TIME_REL_STATES)) {
			manager.requestValue(AutomotiveSignalId.FMS_DRIVER_1_TIME_REL_STATES);
		}
		return (timeRelativeWorkingState.getIntValue());
	}

	// TODO The tank size is not correct
	/** Estimates how long until a refuel is recommended.
	 * @return how many km until a stop for fueling is recommended. +- 10km
	 */
	double getEstimatedKilometersUntilRefuel() {
		// If not driving the truck
		if(getWorkingState() != 3) {
			double averageFuelConsumptionPerKilometer = getTotalFuelUsed() / ((double)getTotalDistanceTravelled()*Math.pow(10,-3));
			double currentLitersInTank = getFuelLevel()*Math.pow(1,-3)*TEMP_TANK_SIZE_IN_LITERS;
			return currentLitersInTank/averageFuelConsumptionPerKilometer;
		} else if(getWorkingState() == 3) {
			// TODO do dynamic calculation
			return 0.0;
		} else {
			// TODO
			return 0;
		}
	}

	// TODO correct?
	/** Method that returns the number of minutes until a stop i required.
	 * @return 270 if currently in a break, Positive number with minutes left, if drive longer than legal.
	 */
	double getTimeUntilTimeStop() {
		if(getWorkingState() != 3) {
			return LEGAL_UPTIME;
		} else if(getWorkingState() == 3) {
			return LEGAL_UPTIME-((System.nanoTime() - driveStartTime) * NANOSECONDS_TO_MINUTES);
		} else {
			// TODO
			return 0;
		}
	}

	// Method that sets and determines start- and stop time
	private void setTime(int prevValue, int currentValue) {
		if(prevValue != 3 & currentValue == 3) {
			driveStartTime = System.nanoTime();
		} else if(prevValue == 3 && currentValue != 3) {
			driveStopTime = System.nanoTime();
		}
	}

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
