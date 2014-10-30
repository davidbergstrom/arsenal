package com.edit.reach.system;

import android.os.Handler;
import android.os.Looper;
import android.swedspot.automotiveapi.AutomotiveSignal;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.swedspot.scs.data.SCSFloat;
import android.swedspot.scs.data.Uint8;
import android.util.Log;
import com.edit.reach.constants.SignalType;
import com.edit.reach.constants.UniversalConstants;
import com.edit.reach.constants.VehicleState;
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
 * This class handles all communication with the AGA Signals.
 * Created by: Tim Kerschbaumer
 * Project: Milestone
 * Date: 2014-09-27
 * Time: 19:28
 * Last Edit: 2014-10-29
 */
public final class VehicleSystem extends Observable implements Runnable {
	/* --- INSTANCE VARIABLES --- */

	// Using Atomic types for thread-safety

	// The name of the thread that runs the vehicle signals.
	private static final String VEHICLE_SIGNALS_THREAD = "VehicleSignalsThread";
	// The fuel rate.
	private final AtomicFloat fuelRate = new AtomicFloat(0f);
	// The fuel level.
	private final AtomicFloat fuelLevel = new AtomicFloat(-1f);
	// The fuel level in percent when starting calculation for the tank size.
	private final AtomicFloat startFuelLevel = new AtomicFloat(-1f);
	// The size of the tank.
	private final AtomicFloat tankSize = new AtomicFloat(0f);
	// Time that vehicle started driving.
	private final AtomicLong startTime = new AtomicLong(0);
	// Time that the vehicle stopped driving.
	private final AtomicLong stopTime = new AtomicLong(0);
	// Previous time used to calculate when to notify observers.
	private final AtomicLong prevTime = new AtomicLong(0);
	// 0 if not moving 1 if moving
	private final AtomicInteger isMoving = new AtomicInteger(-1);
	// The working state of the driver.
	private final AtomicInteger workingState = new AtomicInteger(-1);
	// The distraction leve.
	private final AtomicInteger distractionLevel = new AtomicInteger(-1);
	// Keeps track if navigationModel has been notified for "short time".
	private final AtomicBoolean timeHasBeenNotified = new AtomicBoolean(false);
	// A list with the fuel rate
	private final List<Float> fuelRateList = new ArrayList<Float>();

	// A thread for listening to the vehicle signals.
	private final Thread vehicleSignals;
	// A handler for the signals
	private Handler signalHandler;
	// Lock objects
	private final Object handlerLock = new Object();
	private final Object fuelRateListLock = new Object();
	private final Object automotiveManagerLock = new Object();

	private final AutomotiveListener automotiveListener = new AutomotiveListener() {

		@Override
		public void receive(final AutomotiveSignal automotiveSignal) {
			signalHandler.post(new Runnable() {
				@Override
				public void run() {
					// Switch between incoming signals
					switch (automotiveSignal.getSignalId()) {

						// Fuel percent left in tank changed
						case AutomotiveSignalId.FMS_FUEL_LEVEL_1:
							float prevFuelLevel = fuelLevel.get();
							fuelLevel.set(((SCSFloat) (automotiveSignal.getData())).getFloatValue());

							// Call methods to determine critical states
							determineFuelUpdate(prevFuelLevel, fuelLevel.get());
							determineLowFuel(prevFuelLevel, fuelLevel.get());

							Log.d("Signal: FuelLevel", "Fuel level: " + fuelLevel.get());
							break;

						// Working state of driver changed
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

							Log.d("Signal: WorkingState", "State: " + workingState.get());
							break;

						// Vehicle motion changed
						case AutomotiveSignalId.FMS_VEHICLE_MOTION:
							isMoving.set(((Uint8) (automotiveSignal.getData())).getIntValue());
							determineMovingChange();

							Log.d("Signal: Motion", "Motion: " + isMoving.get());
							break;

						// Vehicle fuel rate changed
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

							// Try to calculate the tank size
							calculateTankSize();

							// Determine and notify observers if tank size has been calculated
							determineTankSizeEstimated();

							Log.d("Signal: FuelRate", "Fuel rate: " + fuelRate.get());
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

	// A runnable that updates time.
	private final Runnable timeRunnable = new Runnable() {
		@Override
		public void run() {
			if (startTime.get() != 0) {
				// Determine if time has been changed
				determineTimeUpdate();
			}
			// Keep posting to this runnable with 1 second delay.
			signalHandler.postDelayed(this, 1000);
		}
	};

	private final AutomotiveCertificate automotiveCertificate = new AutomotiveCertificate(new byte[1]);

	private final DriverDistractionListener driverDistractionListener = new DriverDistractionListener() {
		@Override
		public void levelChanged(final DriverDistractionLevel driverDistractionLevel) {
			signalHandler.post(new Runnable() {
				@Override
				public void run() {
					if(distractionLevel.get() != driverDistractionLevel.getLevel()) {
						distractionLevel.set(driverDistractionLevel.getLevel());
						setChanged();
						notifyObservers(SignalType.DISTRACTION_LEVEL);
					}
				}
			});

		}
	};

	private final AutomotiveManager automotiveManager = AutomotiveFactory.createAutomotiveManagerInstance(automotiveCertificate, automotiveListener, driverDistractionListener);

	/**
	 * Constructor.
	 * Creates a VehicleSystem and starts listening to signals from vehicle.
	 */
	public VehicleSystem() {
		vehicleSignals = new Thread(VehicleSystem.this, VEHICLE_SIGNALS_THREAD);
		vehicleSignals.start();

		// Register Listeners
		synchronized (automotiveManagerLock) {
			automotiveManager.register(
					AutomotiveSignalId.FMS_FUEL_LEVEL_1,
					AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE,
					AutomotiveSignalId.FMS_FUEL_RATE,
					AutomotiveSignalId.FMS_VEHICLE_MOTION);
		}

		synchronized (handlerLock) {
			while (signalHandler == null) {
				try {
					handlerLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		signalHandler.post(timeRunnable);
	}

	@Override
	public void run() {
		try {
			Looper.prepare();
			synchronized (handlerLock) {
				signalHandler = new Handler();
				handlerLock.notifyAll();
			}
			Looper.loop();
		} catch (Throwable t) {
			Log.d("Error in signalThread: ", "" + t);
		}
	}

	// ****** GET-METHODS ****** //

	/**
	 * This methods returns the level of fuel left in tank in percent from 0 to 100.
	 *
	 * @return a float from 0-100 that represents the fuel level. -1 if fuelLevel not intialized.
	 */
	public float getFuelLevel() {
		return fuelLevel.get();
	}

	/**
	 * Method that returns the current state of the vehicle.
	 *
	 * @return a constant int value representing the state of the vehicle.
	 */
	public int getVehicleState() {
		if (isMoving.get() == 1 && workingState.get() == 3) {
			// Is in drive and vehicle is moving.
			return VehicleState.DRIVE_AND_MOVING;
		} else if (isMoving.get() == 0 && workingState.get() == 3) {
			// Is in drive but vehicle not moving.
			return VehicleState.DRIVE_BUT_NOT_MOVING;
		} else {
			// Vehicle not in drive
			return VehicleState.NOT_IN_DRIVE;
		}
	}

	/**
	 * Method that returns the number of seconds until a stop is required.
	 *
	 * @return The legal uptime if currently in a break
	 * Positive number with seconds left if driving
	 * Negative number if drive longer than legal.
	 */
	public double getTimeUntilForcedRest() {
		if (getVehicleState() != VehicleState.NOT_IN_DRIVE) {
			return (UniversalConstants.LEGAL_UPTIME_IN_SECONDS - ((System.nanoTime() - startTime.get()) * UniversalConstants.NANOSECONDS_TO_SECONDS));
		} else {
			return UniversalConstants.LEGAL_UPTIME_IN_SECONDS;
		}
	}

	// This method is not used in current version.
	/**
	 * Estimates how long until a refuel is recommended.
	 *
	 * @return how many km until a stop for fueling is recommended. Returns -1 if not applicable.
	 */
	public double getTimeUntilRefuel() {
		float currentLitersInTank = ((fuelLevel.get() / 100) * tankSize.get());

		if (getVehicleState() != VehicleState.NOT_IN_DRIVE) {
			return calculateTimeToRefuel(currentLitersInTank);
		} else {
			synchronized (fuelRateListLock) {
				if (tankSize.get() == 0 || fuelRateList.size() == 0) {
					// Not applicable because no record of driving is available.
					return -1;
				} else {
					return calculateTimeToRefuel(currentLitersInTank);
				}
			}
		}
	}

	// This method is not used in current version.
	/**
	 * This method returns the current distractionLevel.
	 * If it has not been set it returns -1.
	 * @return the distractionLevel of the driver.
	 */
	public int getDistractionLevel() {
		return distractionLevel.get();
	}

	// ********** PRIVATE METHODS THAT NOTIFY OBSERVERS ********** //

	// Method that notifies observers if the fuel level changed more than 1%.
	private void determineFuelUpdate(float prevFuelLevel, float fuelLevel) {
		if (Math.abs(fuelLevel - prevFuelLevel) >= 1) {
			setChanged();
			notifyObservers(SignalType.FUEL_UPDATE);
		}
	}

	// Only notifies observers if the previous fuel level was above the threshold and the current fuel level is below the threshold.
	// This to avoid multiple observer updates when fuel decreases.
	private void determineLowFuel(float prevFuelLevel, float fuelLevel) {
		if (fuelLevel <= UniversalConstants.FUEL_THRESHOLD && prevFuelLevel > UniversalConstants.FUEL_THRESHOLD) {
			setChanged();
			notifyObservers(SignalType.LOW_FUEL);
		}
	}

	// Notify observers if the state of the driver has changed.
	private void determineWorkStateChange(int prevState, int curState) {
		if (prevState != curState && curState != -1) {
			setChanged();
			notifyObservers(SignalType.VEHICLE_STOPPED_OR_STARTED);
		}
	}

	// Notify observers if the vehicle started or stopped moving.
	private void determineMovingChange() {
		// No check is necessary, prev value will always differ.
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

	// Method that notifies observers if the vehicle has been in drive close to threshold.
	private void determineShortTime() {
		if (workingState.get() == 3) {
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
		if (prevTime.get() == 0) {
			setChanged();
			notifyObservers(SignalType.UPTIME_UPDATE);
			prevTime.set(System.nanoTime());
		} else {
			if (((System.nanoTime() - prevTime.get()) * UniversalConstants.NANOSECONDS_TO_SECONDS) >= 60) {
				setChanged();
				notifyObservers(SignalType.UPTIME_UPDATE);
				prevTime.set(System.nanoTime());
			}
		}
		determineShortTime();
	}

	// Notifies observers if the tank size has been estimated.
	private void determineTankSizeEstimated() {
		if (tankSize.get() != 0f) {
			setChanged();
			notifyObservers(SignalType.TANK_SIZE_CALCULATED);
		}
	}

	// ********** OTHER PRIVATE METHODS ********** //

	// Method that calculates and sets the size of the vehicles tank.
	private void calculateTankSize() {
		synchronized (fuelRateListLock) {
			if (tankSize.get() == 0f && fuelRateList.size() > 100) {
				float deltaFuelLevel = fuelLevel.get() - startFuelLevel.get();
				float deltaTime = ((float) (((System.nanoTime() - startTime.get()) * UniversalConstants.NANOSECONDS_TO_SECONDS) * UniversalConstants.SECONDS_TO_HOURS));
				float fuelRateSum = 0;
				for (Float fuelRate : fuelRateList) {
					fuelRateSum += fuelRate;
				}
				float meanFuelRate = fuelRateSum / fuelRateList.size();
				tankSize.set((100 / deltaFuelLevel) * (meanFuelRate * deltaTime));
			}
		}
	}

	// Method that calculates the time until a refuel is needed.
	private double calculateTimeToRefuel(final float currentLitersInTank) {
		float addedRate = 0;
		synchronized (fuelRateListLock) {
			for (Float rate : fuelRateList) {
				addedRate += rate;
			}
			float meanFuelRate = addedRate / fuelRateList.size();
			return ((currentLitersInTank / meanFuelRate) * UniversalConstants.HOURS_TO_SECONDS);
		}
	}
}