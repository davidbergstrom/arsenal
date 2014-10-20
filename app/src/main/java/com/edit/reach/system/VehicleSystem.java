package com.edit.reach.system;

import android.os.Handler;
import android.os.Looper;
import android.swedspot.automotiveapi.AutomotiveSignal;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.swedspot.scs.data.*;
import android.util.Log;
import com.edit.reach.constants.UniversalConstants;
import com.edit.reach.constants.MovingState;
import com.edit.reach.constants.SignalType;
import com.edit.reach.utils.AtomicFloat;
import com.swedspot.automotiveapi.AutomotiveFactory;
import com.swedspot.automotiveapi.AutomotiveListener;
import com.swedspot.automotiveapi.AutomotiveManager;
import com.swedspot.vil.distraction.DriverDistractionLevel;
import com.swedspot.vil.distraction.DriverDistractionListener;
import com.swedspot.vil.policy.AutomotiveCertificate;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Class that represents a VehicleSystem.
 * This class handles all communication with the AGA SDK.
 * Created by: Tim Kerschbaumer
 * Project: REACH
 * Date: 2014-09-27
 * Time: 19:28
 * Last Edit: 2014-10-17
 */
public final class VehicleSystem extends Observable implements Runnable {
	/* --- INSTANCE VARIABLES --- */

	// The fuel economy.
	private AtomicFloat instantFuelEconomy = new AtomicFloat(0f);

	// The fuel rate.
	private AtomicFloat fuelRate = new AtomicFloat(0f);

	// The fuel level.
	private AtomicFloat fuelLevel = new AtomicFloat(-1f);

	// The fuel level in percent when starting calculation for the tank size.
	private AtomicFloat startFuelLevel = new AtomicFloat(-1f);

	// 0 if not moving 1 if moving
	private AtomicInteger isMoving = new AtomicInteger(-1);

	// The working state of the driver.
	private AtomicInteger workingState = new AtomicInteger(-1);

	// A list with the instant fuel economy
	private final List<Float> instantFuelEconomyList = new ArrayList<Float>();
	// A list with the fuel rate
	private final List<Float> fuelRateList = new ArrayList<Float>();

	// Time that vehicle started driving.
	private AtomicLong startTime = new AtomicLong(0);

	// Time that the vehicle stopped driving.
	private AtomicLong stopTime = new AtomicLong(0);

	// Previous time used to calculate when to notify observers.
	private AtomicLong prevTime = new AtomicLong(0);

	// The size of the tank.
	private AtomicFloat tankSize = new AtomicFloat(0);

	// Keeps track if navigationModel has been notified for "short time".
	private AtomicBoolean timeHasBeenNotified = new AtomicBoolean(false);

	// A handler for the signals
	private Handler signalHandler;

	// A thread for listening to the vehicle signals.
	private final Thread vehicleSignals;

	private final Runnable timeRunnable = new Runnable() {
		@Override
		public void run() {
			Log.d("determineTimeUpdate()", "in timeRunnable");
			if (startTime.get() != 0) {
				Log.d("determineTimeUpdate()", "passed startTime.ger() != 0");
				determineTimeUpdate();
				signalHandler.postDelayed(this, 50);
			}
		}
	};

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
							float prevFuelLevel = fuelLevel.get();
							fuelLevel.set(((SCSFloat) (automotiveSignal.getData())).getFloatValue());

							// Call methods to determine critical states
							determineLowFuel(prevFuelLevel, fuelLevel.get());
							determineFuelUpdate(prevFuelLevel, fuelLevel.get());

							Log.d("Signal: FuelLevel", "Fuel level: " + fuelLevel);
							break;

						// Working state of driver
						case AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE:
							int prevWorkState = workingState.get();
							workingState.set(((Uint8) automotiveSignal.getData()).getIntValue());

							// If trucker just started driving
							if (workingState.get() == 3 && prevWorkState != 3) {
								// Determine if the break was final.
								if (stopTime.get() != 0) {
									if (determineBreakWasFinal()) {
										// Set start time
										startTime.set(System.nanoTime());
										timeHasBeenNotified.set(false);
									}
								} else {
									// First run - set start time
									startTime.set(System.nanoTime());
									timeHasBeenNotified.set(false);
								}
							}

							// If trucker just stopped
							if (workingState.get() != 3 && prevWorkState == 3) {
								stopTime.set(System.nanoTime());
							}

							// Call method to determine critical states
							determineWorkStateChange(prevWorkState, workingState.get());

							Log.d("Signal: WorkingState", "State: " + workingState);
							break;

						// Is vehicle moving
						case AutomotiveSignalId.FMS_VEHICLE_MOTION:
							isMoving.set(((Uint8) (automotiveSignal.getData())).getIntValue());
							determineMovingChange();

							Log.d("Signal: Motion", "Motion: " + isMoving);
							break;

						// Instantaneous Fuel economy
						case AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY:
							instantFuelEconomy.set(((SCSFloat) automotiveSignal.getData()).getFloatValue());

							// Only add to fuel consumption list if the vehicle is moving
							if (isMoving.get() == 1) {
								synchronized (instantFuelEconomyList) {
									instantFuelEconomyList.add(instantFuelEconomy.get());
								}
							}

							Log.d("Signal: FuelEconomy", "Fuel economy: " + instantFuelEconomy);
							break;

						case AutomotiveSignalId.FMS_FUEL_RATE:
							fuelRate.set(((SCSFloat) automotiveSignal.getData()).getFloatValue());

							if (isMoving.get() == 1) {
								synchronized (fuelRateList) {
									fuelRateList.add(fuelRate.get());
								}
							}

							if (startFuelLevel.get() != -1f && fuelLevel.get() != -1f) {
								startFuelLevel.set(fuelLevel.get());
							}

							calculateTankSize();
							determineTankSizeEstimated();

							Log.d("Signal: FuelRate", "Fuel rate: " + instantFuelEconomy.get());
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

	// The time threshold for when to calculate tank size.
	private static final float FUEL_TIME_CALCULATION_THRESHOLD = 100;

	// The name of the thread that runs the vehicle signals.
	private static final String VEHICLE_SIGNALS_THREAD = "VehicleSignalsThread";

	/** Constructor.
	 */
	public VehicleSystem() {
		vehicleSignals = new Thread(VehicleSystem.this, VEHICLE_SIGNALS_THREAD);
		vehicleSignals.start();

		signalHandler.post(timeRunnable);

		// Register Listeners
		synchronized (automotiveManager) {
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
	public float getFuelLevel() {
		Log.d("Thread in VehicleSystem - getFuelLevel()", Thread.currentThread().getName());
		return fuelLevel.get();
	}

	/** Estimates how long until a refuel is recommended.
	 * @return how many km until a stop for fueling is recommended. Returns -1 if not applicable.
	 */
	public double getKilometersUntilRefuel() {
		double currentLitersInTank = ((fuelLevel.get()/100.0) * tankSize.get());

		if (getVehicleState() != MovingState.NOT_IN_DRIVE) {
			return calculateKmToRefuel(currentLitersInTank);
		} else {
			synchronized (instantFuelEconomyList) {
				if (tankSize.get() == 0 || instantFuelEconomyList.size() == 0) {
					// Not applicable because no record of driving is available.
					return -1;
				} else {
					return calculateKmToRefuel(currentLitersInTank);
				}
			}
		}
	}

	/** Method that returns the number of seconds until a stop is required.
	 * @return
	270 if currently in a break
	Positive number with seconds left if driving
	Negative number if drive longer than legal.
	 */
	public double getTimeUntilForcedRest() {
		if (getVehicleState() != MovingState.NOT_IN_DRIVE) {
			return (UniversalConstants.LEGAL_UPTIME_IN_SECONDS - ((System.nanoTime() - startTime.get()) * UniversalConstants.NANOSECONDS_TO_SECONDS));
		} else {
			return UniversalConstants.LEGAL_UPTIME_IN_SECONDS;
		}
	}

	/** Method that returns the current state of the vehicle.
	 * @return a constant int value from class MovingState.
	 */
	public int getVehicleState() {
		if (isMoving.get() == 1 && workingState.get() == 3) {
			// Is in drive and vehicle is moving.
			return MovingState.DRIVE_AND_MOVING;
		} else if (isMoving.get() == 0 && workingState.get() == 3) {
			// Is in drive but vehicle not moving.
			return MovingState.DRIVE_BUT_NOT_MOVING;
		} else {
			// Vehicle not in drive
			return MovingState.NOT_IN_DRIVE;
		}
	}

	// ********** PRIVATE METHODS THAT NOTIFY OBSERVERS ********** //

	// Method that notifies observers if the fuellevel changed more than 1%.
	private void determineFuelUpdate(float prevFuelLevel, float fuelLevel) {
		if(Math.abs(fuelLevel - prevFuelLevel) >= 1) {
			setChanged();
			notifyObservers(SignalType.FUEL_UPDATE);
		}
	}

	// Only notifies observers if the previous fuel level was above the threshold and the current fuel level is below the threshold.
	// This to avoid multiple observer updates when fuel decreases.
	private void determineLowFuel(float prevFuelLevel, float fuelLevel) {
		if(fuelLevel <= UniversalConstants.FUEL_THRESHOLD && prevFuelLevel > UniversalConstants.FUEL_THRESHOLD) {
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

	// Notify observers if the vehicle took a break longer than or equal to the break time.
	private boolean determineBreakWasFinal() {
		boolean wasFinal = false;
		if (((System.nanoTime() - stopTime.get()) * UniversalConstants.NANOSECONDS_TO_SECONDS) >= UniversalConstants.BREAKTIME_IN_SECONDS) {
			setChanged();
			notifyObservers(SignalType.VEHICLE_TOOK_FINAL_BREAK);
			wasFinal = true;
		}
		return wasFinal;
	}

	// Notifies observers if the tank size has been estimated.
	private void determineTankSizeEstimated() {
		if(tankSize.get() != 0) {
			setChanged();
			notifyObservers(SignalType.TANK_SIZE_CALCULATED);
		}
	}

	// Method that notifies observers if the vehicle has been in drive close to threshold.
	private void determineShortTime() {
		if(workingState.get() == 3) {
			if ((UniversalConstants.LEGAL_UPTIME_IN_SECONDS - ((System.nanoTime() - startTime.get()) * UniversalConstants.NANOSECONDS_TO_SECONDS)) < UniversalConstants.TIME_THRESHOLD) {
				if (!timeHasBeenNotified.get()) {
					setChanged();
					notifyObservers(SignalType.SHORT_TIME);
					timeHasBeenNotified.set(true);
				}
			}
		}
	}

	// Method that notifies observers if the time changed more than 60 seconds.
	private void determineTimeUpdate() {
		Log.d("determineTimeUpdate()", "hit");
		if (prevTime.get() == 0) {
			Log.d("determineTimeUpdate()", "passed prevTime.get() == 0");
			setChanged();
			notifyObservers(SignalType.UPTIME_UPDATE);
			prevTime.set(System.nanoTime());
		} else {
			Log.d("determineTimeUpdate()", "did not pass prevTime.get() == 0");
			if (((System.nanoTime() - prevTime.get()) * UniversalConstants.NANOSECONDS_TO_SECONDS) >= 60) {
				setChanged();
				notifyObservers(SignalType.UPTIME_UPDATE);
				prevTime.set(System.nanoTime());
			} else {
				// Do nothing
				Log.d("determineTimeUpdate()", "in: do nothing");
			}
		}
		determineShortTime();
		Log.d("determineTimeUpdate()", "called determineShortTime()");
	}

	// ********** OTHER PRIVATE METHODS ********** //

	// Method that calculates and sets the size of the vehicles tank.
	private void calculateTankSize() {
		if(((System.nanoTime() - startTime.get()) * UniversalConstants.NANOSECONDS_TO_SECONDS) <= FUEL_TIME_CALCULATION_THRESHOLD && tankSize.get() != 0.0) {
			float deltaFuelLevel = fuelLevel.get() - startFuelLevel.get();
			float deltaTime = ((float)(((System.nanoTime() - startTime.get()) * UniversalConstants.NANOSECONDS_TO_SECONDS) * UniversalConstants.SECONDS_TO_HOURS));
			float fuelRateSum = 0;
			synchronized (fuelRateList) {
				for (Float fuelRate : fuelRateList) {
					fuelRateSum = fuelRateSum + fuelRate;
				}
				float meanFuelRate = fuelRateSum / fuelRateList.size();

				tankSize.set((100 / deltaFuelLevel) * (meanFuelRate * deltaTime));
			}
			synchronized (automotiveManager) {
				automotiveManager.unregister(AutomotiveSignalId.FMS_FUEL_RATE);
			}

		}
	}

	// Method that calculates the number of kilometers until a refuel is needed.
	private double calculateKmToRefuel(final double currentLitersInTank) {
		float addedConsumption = 0;
		synchronized (instantFuelEconomyList) {
			for (Float consumption : instantFuelEconomyList) {
				addedConsumption = addedConsumption + consumption;
			}
			double meanFuelEconomy = (double) addedConsumption / instantFuelEconomyList.size();
			return (meanFuelEconomy * currentLitersInTank);
		}
	}
}