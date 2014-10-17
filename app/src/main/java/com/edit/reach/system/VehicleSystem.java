package com.edit.reach.system;

import android.os.Handler;
import android.os.Looper;
import android.swedspot.automotiveapi.AutomotiveSignal;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.swedspot.scs.data.*;
import android.util.Log;
import com.edit.reach.constants.Constants;
import com.edit.reach.constants.MovingState;
import com.edit.reach.constants.SignalType;
import com.swedspot.automotiveapi.AutomotiveFactory;
import com.swedspot.automotiveapi.AutomotiveListener;
import com.swedspot.automotiveapi.AutomotiveManager;
import com.swedspot.vil.distraction.DriverDistractionLevel;
import com.swedspot.vil.distraction.DriverDistractionListener;
import com.swedspot.vil.policy.AutomotiveCertificate;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Class that represents a VehicleSystem.
 * This class handles all communication with the AGA SDK.
 * Created by: Tim Kerschbaumer
 * Project: REACH
 * Date: 2014-09-27
 * Time: 19:28
 * Last Edit: 2014-10-17
 */
public class VehicleSystem extends Observable implements Runnable {
	/* --- INSTANCE VARIABLES --- */

	// The fuel economy.
	private SCSFloat instantFuelEconomy = new SCSFloat(0f);
	// The fuel rate.
	private SCSFloat fuelRate = new SCSFloat(0f);
	// The fuel level.
	private SCSFloat fuelLevel = new SCSFloat(-1f);
	// The fuel level in percent when starting calculation for the tank size.
	private SCSFloat startFuelLevel = new SCSFloat(-1f);

	// 0 if not moving 1 if moving
	private Uint8 isMoving = new Uint8(-1);
	// The working state of the driver.
	private Uint8 workingState = new Uint8(-1);
	// The number of km til service.
	private SCSInteger distanceToService = new SCSInteger(-1);

	// A list with the instant fuel economy
	private final List<SCSFloat> instantFuelEconomyList = new ArrayList<SCSFloat>();
	// A list with the fuel rate
	private final List<SCSFloat> fuelRateList = new ArrayList<SCSFloat>();

	// Time that vehicle started driving.
	private long startTime = 0;

	// Time that the vehicle stopped driving.
	private long stopTime = 0;

	// Previous time used to calculate when to notify observers.
	private long prevTime = 0;

	// The size of the tank.
	private double tankSize = 0.0;

	// Keeps track if navigationModel has been notified for "short time".
	private boolean timeHasBeenNotified = false;

	// A handler for the signals
	private Handler signalHandler;

	// A thread for listening to the vehicle signals.
	private final Thread vehicleSignals;

	private	final AutomotiveCertificate automotiveCertificate = new AutomotiveCertificate(new byte[0]);

	private final AutomotiveListener automotiveListener = new AutomotiveListener() {

		@Override
		public void receive(final AutomotiveSignal automotiveSignal) {
			signalHandler.post(new Runnable() {
				@Override
				public void run() {

					// Switch between incoming signals
					switch (automotiveSignal.getSignalId()) {

						// How much fuel is left in tank.
						case AutomotiveSignalId.FMS_FUEL_LEVEL_1:
							SCSFloat prevFuelLevel = fuelLevel;
							fuelLevel = (SCSFloat) (automotiveSignal.getData());

							// Call methods to determine critical states
							determineLowFuel(prevFuelLevel.getFloatValue(), fuelLevel.getFloatValue());
							determineFuelUpdate(prevFuelLevel.getFloatValue(), fuelLevel.getFloatValue());

							// TODO where to put the time updates?
							if(startTime != 0) {
								determineShortTime();
								determineTimeUpdate();
							}

							Log.d("Signal: FuelLevel", "Fuel level: " + fuelLevel.getFloatValue());
							break;

						// Working state of driver
						case AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE:
							Uint8 prevWorkState = workingState;
							workingState = (Uint8) automotiveSignal.getData();

							// If trucker just started driving
							if (workingState.getIntValue() == 3 && prevWorkState.getIntValue() != 3) {
								// Determine if the break was final.
								if(stopTime != 0) {
									if(determineBreakWasFinal()) {
										// Set start time
										startTime = System.nanoTime();
										timeHasBeenNotified = false;
									}
								} else {
									// First run - set start time
									startTime = System.nanoTime();
									timeHasBeenNotified = false;
								}
							}

							// If trucker just stopped
							if (workingState.getIntValue() != 3 && prevWorkState.getIntValue() == 3) {
								stopTime = System.nanoTime();
							}

							// Call method to determine critical states
							determineWorkStateChange(prevWorkState.getIntValue(), workingState.getIntValue());

							Log.d("Signal: WorkingState", "State: " + workingState.getIntValue());
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
							determineMovingChange();

							Log.d("Signal: Motion", "Motion: " + isMoving.getIntValue());
							break;

						// Instantaneous Fuel economy
						case AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY:
							instantFuelEconomy = (SCSFloat) automotiveSignal.getData();

							// Only add to fuelconsumption list if the vehicle is moving
							if(isMoving.getIntValue() == 1) {
								instantFuelEconomyList.add(instantFuelEconomy);
							}

							Log.d("Signal: FuelEconomy", "Fuel economy: " + instantFuelEconomy.getFloatValue());
							break;

						case AutomotiveSignalId.FMS_FUEL_RATE:
							fuelRate = (SCSFloat) automotiveSignal.getData();

							if(isMoving.getIntValue() == 1) {
								fuelRateList.add(fuelRate);
							}

							if(startFuelLevel.getFloatValue() != -1f && fuelLevel.getFloatValue() != -1f) {
								startFuelLevel = fuelLevel;
							}

							calculateTankSize();

							Log.d("Signal: FuelRate", "Fuel rate: " + instantFuelEconomy.getFloatValue());
							break;

						default:
							break;
					}
				}
			});
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
			// TODO what here?
		}
	};

	private final AutomotiveManager automotiveManager = AutomotiveFactory.createAutomotiveManagerInstance(automotiveCertificate, automotiveListener, driverDistractionListener);

	/* --- CONSTANTS --- */

	// Threshold for low fuel
	private static final float FUEL_THRESHOLD = 10f;

	// Threshold for short distance to service
	private static final int SERVICE_THRESHOLD = 100;

	// Threshold for short on time.
	private static final long TIME_THRESHOLD = 900;

	// The time threshold for when to calculate tank size.
	private static final float FUEL_TIME_CALCULATION_THRESHOLD = 100;

	// The name of the thread that runs the vehicle signals.
	private static final String VEHICLE_SIGNALS_THREAD = "VehicleSignalsThread";

	/** Constructor.
	 */
	public VehicleSystem() {
		vehicleSignals = new Thread(VehicleSystem.this, VEHICLE_SIGNALS_THREAD);
		vehicleSignals.start();

		// Register Listeners
		automotiveManager.register(
				AutomotiveSignalId.FMS_FUEL_LEVEL_1,
				AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE,
				AutomotiveSignalId.FMS_FUEL_RATE,
				AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY,
				AutomotiveSignalId.FMS_SERVICE_DISTANCE,
				AutomotiveSignalId.FMS_VEHICLE_MOTION,
				AutomotiveSignalId.FMS_TACHOGRAPH_VEHICLE_SPEED,
				AutomotiveSignalId.FMS_HIGH_RESOLUTION_ENGINE_TOTAL_FUEL_USED,
				AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE);
	}

	@Override
	public void run() {
		try {
			Looper.prepare();
			signalHandler = new Handler();
			Looper.loop();
		} catch (Throwable t) {
			Log.d("Error in signalThread: ", "" + t);
		}
	}

	// ****** GET-METHODS ****** //

	/** This methods returns the level of fuel left in tank in percent from 0 to 100.
	 * @return a float from 0-100 that represents the fuel level
	 */
	public synchronized float getFuelLevel() {
		return (fuelLevel.getFloatValue());
	}

	/** Estimates how long until a refuel is recommended.
	 * @return how many km until a stop for fueling is recommended. Returns -1 if not applicable.
	 */
	public synchronized double getKilometersUntilRefuel() {
		double currentLitersInTank = ((fuelLevel.getFloatValue()/100.0) * tankSize);

		if (getVehicleState() != MovingState.NOT_IN_DRIVE) {
			return calculateKmToRefuel(currentLitersInTank);
		} else {
			if(tankSize == 0 || instantFuelEconomyList.size() == 0) {
				// Not applicable because no record of driving is available.
				return -1;
			} else {
				return calculateKmToRefuel(currentLitersInTank);
			}
		}
	}

	/** Method that returns the number of seconds until a stop is required.
	 * @return
	270 if currently in a break
	Positive number with seconds left if driving
	Negative number if drive longer than legal.
	 */
	public synchronized double getTimeUntilForcedRest() {
		if (getVehicleState() != MovingState.NOT_IN_DRIVE) {
			return (Constants.LEGAL_UPTIME_IN_SECONDS - ((System.nanoTime() - startTime) * Constants.NANOSECONDS_TO_SECONDS));
		} else {
			return Constants.LEGAL_UPTIME_IN_SECONDS;
		}
	}

	/** Method that returns the current state of the vehicle.
	 * @return a constant int value from class MovingState.
	 */
	public synchronized int getVehicleState() {
		if (isMoving.getIntValue() == 1 && workingState.getIntValue() == 3) {
			// Is in drive and vehicle is moving.
			return MovingState.DRIVE_AND_MOVING;
		} else if (isMoving.getIntValue() == 0 && workingState.getIntValue() == 3) {
			// Is in drive but vehicle not moving.
			return MovingState.DRIVE_BUT_NOT_MOVING;
		} else {
			// Vehicle not in drive
			return MovingState.NOT_IN_DRIVE;
		}
	}

	// TODO not used because the AGA simulator does not have this signal.
	/** Method that returns the number om kilometers until service is recommended.
	 * @return how many km until a stop for service is recommended.
	 */
	public synchronized int getKilometersUntilService() {
		return distanceToService.getIntValue();
	}

	// ********** PRIVATE METHODS THAT NOTIFY OBSERVERS ********** //

	// Method that notifies observers if the fuellevel changed more than 1%.
	private void determineFuelUpdate(float prevFuelLevel, float fuelLevel) {
		if(fuelLevel - prevFuelLevel >= 1) {
			setChanged();
			notifyObservers(SignalType.FUEL_UPDATE);
		}
	}

	// Only notifies observers if the previous fuel level was above the threshold and the current fuel level is below the threshold.
	// This to avoid multiple observer updates when fuel decreases.
	private void determineLowFuel(float prevFuelLevel, float fuelLevel) {
		if(fuelLevel <= FUEL_THRESHOLD && prevFuelLevel > FUEL_THRESHOLD) {
			setChanged();
			notifyObservers(SignalType.LOW_FUEL);
		}
	}

	// Notify observers if the state of the driver has changed.
	private void determineWorkStateChange(int prevState, int curState) {
		if(prevState != curState && curState != -1) {
			setChanged();
			notifyObservers(SignalType.VEHICLE_STOPPED_OR_STARTED);
		}
	}

	// Notify observers if the vehicle started or stopped moving.
	private void determineMovingChange() {
		// No check is necessary
		setChanged();
		notifyObservers(SignalType.VEHICLE_STOPPED_OR_STARTED);
	}

	// Method that notifies observers if the vehicle has been in drive close to threshold.
	private void determineShortTime() {
		if(workingState.getIntValue() == 3) {
			if ((Constants.LEGAL_UPTIME_IN_SECONDS - ((System.nanoTime() - startTime) * Constants.NANOSECONDS_TO_SECONDS)) < TIME_THRESHOLD) {
				if (!timeHasBeenNotified) {
					setChanged();
					notifyObservers(SignalType.SHORT_TIME);
					timeHasBeenNotified = true;
				}
			}
		}
	}

	// Notify observers if the vehicle took a break longer than or equal to the break time.
	private boolean determineBreakWasFinal() {
		boolean wasFinal = false;
		if (((System.nanoTime() - stopTime) * Constants.NANOSECONDS_TO_SECONDS) >= Constants.BREAKTIME_IN_SECONDS) {
			setChanged();
			notifyObservers(SignalType.VEHICLE_TOOK_FINAL_BREAK);
			wasFinal = true;
		}
		return wasFinal;
	}

	// Method that notifies observers if the time changed more than 60 seconds.
	private void determineTimeUpdate() {
		if(prevTime == 0) {
			setChanged();
			notifyObservers(SignalType.UPTIME_UPDATE);
			prevTime = System.nanoTime();
		} else {
			if(((System.nanoTime() - prevTime) * Constants.NANOSECONDS_TO_SECONDS) >= 60) {
				setChanged();
				notifyObservers(SignalType.UPTIME_UPDATE);
				prevTime = System.nanoTime();
			} else {
				// Do nothing
			}
		}
	}

	// Only notifies observers if the previous km to service was above the threshold and the current km to service is below the threshold.
	// This to avoid multiple observer updates when service decreases.
	private void determineCloseToService(int prevKmToService, int kmToService) {
		if(kmToService <= SERVICE_THRESHOLD && prevKmToService > SERVICE_THRESHOLD) {
			setChanged();
			notifyObservers(SignalType.SHORT_TO_SERVICE);
		}
	}

	// ********** OTHER PRIVATE METHODS ********** //

	// Method that calculates and sets the size of the vehicles tank.
	private void calculateTankSize() {
		if(((System.nanoTime() - startTime) * Constants.NANOSECONDS_TO_SECONDS) <= FUEL_TIME_CALCULATION_THRESHOLD && tankSize != 0.0) {
			float deltaFuelLevel = fuelLevel.getFloatValue() - startFuelLevel.getFloatValue();
			double deltaTime = (((System.nanoTime() - startTime) * Constants.NANOSECONDS_TO_SECONDS) * Constants.SECONDS_TO_HOURS);
			float fuelRateSum = 0;
			for(SCSFloat fuelRate : fuelRateList) {
				fuelRateSum = fuelRateSum + fuelRate.getFloatValue();
			}
			float meanFuelRate = fuelRateSum / fuelRateList.size();

			tankSize = (100/deltaFuelLevel) * (meanFuelRate * deltaTime);
			automotiveManager.unregister(AutomotiveSignalId.FMS_FUEL_RATE);
		}
	}

	// Method that calculates the number of kilometers until a refuel is needed.
	private double calculateKmToRefuel(final double currentLitersInTank) {
		float addedConsumption = 0;
		for(SCSFloat consumption : instantFuelEconomyList) {
			addedConsumption = addedConsumption + consumption.getFloatValue();
		}
		double meanFuelEconomy =  (double) addedConsumption / instantFuelEconomyList.size();
		return (meanFuelEconomy * currentLitersInTank);
	}
}