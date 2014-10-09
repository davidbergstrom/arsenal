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
 * Last Edit: 2014-10-08
 */
class VehicleSystem extends Observable implements Runnable {

	/* --- Instance Variables --- */
	private SCSFloat instantFuelConsumption;
	private SCSFloat instantFuelEconomy;
	private SCSFloat vehicleSpeed;

	private Uint8 card;
	private SCSLong totalVehicleDistance;
	private SCSDouble totalFuelUsed;
	private Uint16 totalWeight;

	// A thread for listening to the vehicle signals.
	private Thread vehicleSignals;

	private SCSInteger distanceToService = new SCSInteger(-1);
	private SCSFloat fuelLevel = new SCSFloat(-1f);
	private Uint8 isMoving = new Uint8(-1);
	private Uint8 workingState = new Uint8(-1);

	// Time that vehicle started driving.
	private long startTime = 0;

	// Kepps track if navigationModel has been notified for "short time".
	private boolean timeHasBeenNotified = false;

	private final AutomotiveCertificate automotiveCertificate = new AutomotiveCertificate(new byte[]{0, 0, 0, 0});

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
					SCSFloat prevFuelLevel = fuelLevel;
					fuelLevel = (SCSFloat) (automotiveSignal.getData());

					// Call methods to determine critical states
					determineLowFuel(prevFuelLevel.getFloatValue(), fuelLevel.getFloatValue());
					determineShortTime();

					Log.d("Signal: FUEL", "Fuellevel: " + fuelLevel.getFloatValue());
					break;

				// Working state of driver
				case AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE:
					Uint8 prevWorkState = workingState;
					workingState = (Uint8) automotiveSignal.getData();

					// If trucker just started driving
					if(workingState.getIntValue() == 3 && prevWorkState.getIntValue() != 3) {
						// Set starttime
						startTime = System.nanoTime();
						timeHasBeenNotified = false;
					}

					// Call method to determine critical states
					determineIfStoppedOrStarted(prevWorkState.getIntValue(), workingState.getIntValue());

					Log.d("Signal: W-STATE", "State: " + workingState.getIntValue());
					break;

				// Distance to service
				case AutomotiveSignalId.FMS_SERVICE_DISTANCE:
					SCSInteger prevDistanceToService = distanceToService;
					distanceToService = (SCSInteger) (automotiveSignal.getData());

					// Call method to determine critical states
					determineCloseToService(prevDistanceToService.getIntValue(), distanceToService.getIntValue());

					Log.d("Signal: Distance-To-Service", "Distance: " + distanceToService.getIntValue());
					break;

				// Is vehicle moving
				case AutomotiveSignalId.FMS_VEHICLE_MOTION:
					isMoving = (Uint8) (automotiveSignal.getData());

					Log.d("Signal: Motion", "Motion " + isMoving.getIntValue());
					break;

				// Instantaneous Fuel consumption
				case AutomotiveSignalId.FMS_FUEL_RATE:
					instantFuelConsumption = (SCSFloat) automotiveSignal.getData();

					Log.d("Signal: FuelRate", "Fuel rate " + instantFuelConsumption.getFloatValue());
					break;

				// Instantaneous Fuel economy
				case AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY:
					instantFuelEconomy = (SCSFloat) automotiveSignal.getData();

					Log.d("Signal: FuelEconomy", "Fuel economy " + instantFuelEconomy.getFloatValue());
					break;

				// Vehicle speed (Tachograph)
				case AutomotiveSignalId.FMS_TACHOGRAPH_VEHICLE_SPEED:
					vehicleSpeed = ((SCSFloat) automotiveSignal.getData());

					Log.d("Signal: VehicleSpeed", "Vehicle speed " + vehicleSpeed.getFloatValue());
					break;

				// Has a driver
				case AutomotiveSignalId.FMS_DRIVER_1_CARD:
					card = (Uint8) automotiveSignal.getData();

					Log.d("Signal: Card", "Card state " + card.getIntValue());
					break;

				// Total fuel used
				case AutomotiveSignalId.FMS_HIGH_RESOLUTION_ENGINE_TOTAL_FUEL_USED:
					totalFuelUsed = (SCSDouble) automotiveSignal.getData();

					Log.d("Signal: TotalFuelUsed", "Total fuel used " + totalFuelUsed.getDoubleValue());
					break;

				// Total vehicle distance
				case AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE:
					totalVehicleDistance = (SCSLong)automotiveSignal.getData();

					Log.d("Signal: TotalDistance", "Total distance " + totalVehicleDistance.getLongValue() );
					break;

				// Total weight of the vehicle
				case AutomotiveSignalId.FMS_GROSS_COMBINATION_VEHICLE_WEIGHT:
					totalWeight = (Uint16) automotiveSignal.getData();

					Log.d("Signal: Vehicle weight", "Weight " + totalWeight.getIntValue());
					break;

				default:
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

	// The Automotive manager.
	private AutomotiveManager manager = AutomotiveFactory.createAutomotiveManagerInstance(automotiveCertificate, automotiveListener, driverDistractionListener);

	/* --- CONSTANTS --- */

	// TODO This is a fictitious tank size
	private static final int TEMP_TANK_SIZE_IN_LITERS = 600;

	// Multiply with this to convert nanoseconds to minutes.
	private static final double NANOSECONDS_TO_MINUTES = 1*(Math.pow(10,-9))/60.0;

	// The maximum number of minutes to drive before a 45 minute break.
	private static final int LEGAL_UPTIME_IN_MINUTES = 270;

	// Threshold for low fuel
	private static final float FUEL_THRESHOLD = 10f;

	// Threshold for short distance to service
	private static final int SERVICE_THRESHOLD = 100;

	// Threshold for short on time.
	private static final int TIME_THRESHOLD = 15;

	/** Constructor.
	 */
	VehicleSystem() {
		vehicleSignals = new Thread(VehicleSystem.this, "VehicleSignals");
		vehicleSignals.start();
	}

	@Override
	public void run() {
		// TODO Only fuel_level is checked at first run. The other values have to change before they are detected. Why?
		manager.register(AutomotiveSignalId.FMS_FUEL_LEVEL_1);
		manager.register(AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE);
		manager.register(AutomotiveSignalId.FMS_FUEL_RATE);
		manager.register(AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY);
		manager.register(AutomotiveSignalId.FMS_SERVICE_DISTANCE);
		manager.register(AutomotiveSignalId.FMS_VEHICLE_MOTION);
		manager.register(AutomotiveSignalId.FMS_TACHOGRAPH_VEHICLE_SPEED);
		manager.register(AutomotiveSignalId.FMS_HIGH_RESOLUTION_ENGINE_TOTAL_FUEL_USED);
		manager.register(AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE);;
	}

	// ****** PACKAGE GET-METHODS ****** //

	/** Estimates how long until a refuel is recommended.
	 * @return how many km until a stop for fueling is recommended.
	 */
	double getKilometersUntilRefuel() {
		try {
			if (getVehicleState() == 1) {
				// TODO, Evaluate for drive and moving state
				return 0;
			} else if (getVehicleState() == 2) {
				// TODO, Evaluate for drive but not moving state
				return 0;
			} else {
				// TODO, a better calculation could be made
				double averageFuelConsumptionPerKilometer = totalFuelUsed.getDoubleValue() / (totalVehicleDistance.getLongValue() * Math.pow(10, -3));
				double currentLitersInTank = (fuelLevel.getFloatValue() * Math.pow(1, -3)) * TEMP_TANK_SIZE_IN_LITERS;
				return currentLitersInTank / averageFuelConsumptionPerKilometer;
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			Log.d("getKilometersUntilRefuel", "totalFuelUsed or totalVehicleDistance not initialized");
			throw new NullPointerException("Variable not initialized");
		}
	}

	/** Method that returns the current state of the vehicle.
	 * @return
	1 - Vehicle is moving.
	2 - Engine is on but vehicle is not moving.
	3 - Engine is off and vehicle is not moving.
	 */
	int getVehicleState() {
		try {
			if (isMoving.getIntValue() == 1 && workingState.getIntValue() == 3) {
				// Is in drive and vehicle is moving.
				return 1;
			} else if (isMoving.getIntValue() == 0 && workingState.getIntValue() == 3) {
				// Is in drive but vehicle not moving.
				return 2;
			} else {
				// Vehicle not in drive
				return 3;
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			Log.d("getVehicleState", "Nullpointer. isMoving or workingState not initialized");
			throw new NullPointerException("Variable not initialized");
		}
	}

	// TODO, do the math
	/** Method that returns the number of minutes until a stop is required.
	 * @return
	 270 if currently in a break
	 Positive number with minutes left if driving
	 Negative number if drive longer than legal.
	 */
	double getTimeUntilForcedRest() {
		try {
			if (getVehicleState() == 1) {
				return (LEGAL_UPTIME_IN_MINUTES - ((System.nanoTime() - startTime) * NANOSECONDS_TO_MINUTES));
			} else if (getVehicleState() == 2) {
				// TODO What to do when not moving but in drive?
				return (LEGAL_UPTIME_IN_MINUTES - ((System.nanoTime() - startTime) * NANOSECONDS_TO_MINUTES));
			} else {
				return LEGAL_UPTIME_IN_MINUTES;
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			Log.d("getTimeUntilForcedRest", "Nullpointer. startTime not initialized");
			throw new NullPointerException("Variable not initialized");
		}
	}

	/** Method that returns the number om kilometers until service is recommended.
	 * @return how many km until a stop for service is recommended.
	 */
	int getKilometersUntilService() {
		try {
			return distanceToService.getIntValue();
		} catch (NullPointerException e) {
			e.printStackTrace();
			Log.d("getKilometersUntilService", "Nullpointer, distanceToService not initialized");
			throw new NullPointerException("Variable not initialized");
		}

	}
	// ****** END ****** //

	// ********** PRIVATE METHODS THAT NOTIFY OBSERVERS ********** //

	private void determineShortTime() {
		Log.d("Time", (LEGAL_UPTIME_IN_MINUTES - ((System.nanoTime() - startTime) * NANOSECONDS_TO_MINUTES)) + "");
		if(workingState.getIntValue() == 3) {
			if ((LEGAL_UPTIME_IN_MINUTES - ((System.nanoTime() - startTime) * NANOSECONDS_TO_MINUTES)) < TIME_THRESHOLD) {
				if (!timeHasBeenNotified) {
					setChanged();
					notifyObservers(SIGNAL_TYPE.SHORT_TIME);
					timeHasBeenNotified = true;
				}
			}
		}
	}

	// Only notifies observers if the previous fuellevel was above the threshould and the current fuellevel is below the threshould.
	// This to avoid multiple observer updates when fuel decreases.
	private void determineLowFuel(float prevFuelLevel, float fuelLevel) {
		if(fuelLevel <= FUEL_THRESHOLD && prevFuelLevel > FUEL_THRESHOLD) {
			setChanged();
			notifyObservers(SIGNAL_TYPE.LOW_FUEL);
		}
	}

	// Notify observers if the state of the driver has changed
	private void determineIfStoppedOrStarted(int prevState, int curState) {
		if(prevState != curState && curState != -1) {
			setChanged();
			notifyObservers(SIGNAL_TYPE.VEHICLE_STOPPED_OR_STARTED);
		}
	}

	// Only notifies observers if the previous km to service was above the threshould and the current km to service is below the threshould.
	// This to avoid multiple observer updates when service decreases.
	private void determineCloseToService(int prevKmToService, int kmToService) {
		if(kmToService <= SERVICE_THRESHOLD && prevKmToService > SERVICE_THRESHOLD) {
			setChanged();
			notifyObservers(SIGNAL_TYPE.SHORT_TO_SERVICE);
		}
	}

	// ****** END ****** //
}