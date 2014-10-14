package com.edit.reach.system;

import android.os.Handler;
import android.os.Looper;
import android.swedspot.automotiveapi.AutomotiveSignal;
import android.swedspot.automotiveapi.AutomotiveSignalId;
import android.swedspot.scs.data.*;
import android.util.Log;
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
 * Class that represents a VehicleSystem (Or a Vehicle).
 * Created by: Tim Kerschbaumer
 * Project: REACH
 * Date: 2014-09-27
 * Time: 19:28
 * Last Edit: 2014-10-14
 */
public class VehicleSystem extends Observable implements Runnable {
	/* --- Instance Variables --- */
	private SCSLong totalVehicleDistance;
	private SCSDouble totalFuelUsed;

	private SCSFloat instantFuelEconomy = new SCSFloat(0f);
	private SCSFloat fuelLevel = new SCSFloat(-1f);

	private Uint8 isMoving = new Uint8(-1);
	private Uint8 workingState = new Uint8(-1);
	private SCSInteger distanceToService = new SCSInteger(-1);

	private List<SCSFloat> instantFuelEconomyList = new ArrayList<SCSFloat>();

	// Will these be used?
	private SCSFloat vehicleSpeed;
	private Uint8 card;
	private Uint16 totalWeight;

	// Time that vehicle started driving.
	private long startTime = 0;

	// Keeps track if navigationModel has been notified for "short time".
	private boolean timeHasBeenNotified = false;

	// A thread for listening to the vehicle signals.
	private Thread vehicleSignals;

	private Handler signalHandler;

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

							Log.d("Signal: FUEL", "Fuellevel: " + fuelLevel.getFloatValue());
							break;

						// Working state of driver
						case AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE:
							Uint8 prevWorkState = workingState;
							workingState = (Uint8) automotiveSignal.getData();

							// If trucker just started driving
							if (workingState.getIntValue() == 3 && prevWorkState.getIntValue() != 3) {
								// Set starttime
								startTime = System.nanoTime();
								timeHasBeenNotified = false;
							}

							// Call method to determine critical states
							determineWorkStateChange(prevWorkState.getIntValue(), workingState.getIntValue());

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
							determineMovingChange();

							Log.d("Signal: Motion", "Motion " + isMoving.getIntValue());
							break;

						// Instantaneous Fuel economy
						case AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY:
							instantFuelEconomy = (SCSFloat) automotiveSignal.getData();

							// Only add to fuelconsumption list if the vehicle is moving
							if(isMoving.getIntValue() == 1) {
								instantFuelEconomyList.add(instantFuelEconomy);
							}

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
							totalVehicleDistance = (SCSLong) automotiveSignal.getData();

							Log.d("Signal: TotalDistance", "Total distance " + totalVehicleDistance.getLongValue());
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

	// TODO This is a fictitious tank size
	private static final double TEMP_TANK_SIZE_IN_LITERS = 600.0;

	// Multiply with this to convert nanoseconds to seconds.
	private static final double NANOSECONDS_TO_SECONDS = 1.0/1000000000;

	// The maximum number of seconds to drive before a 45 minute break.
	private static final long LEGAL_UPTIME_IN_SECONDS = 40; // 16200

	// Threshold for low fuel
	private static final float FUEL_THRESHOLD = 10f;

	// Threshold for short distance to service
	private static final int SERVICE_THRESHOLD = 100;

	// Threshold for short on time.
	private static final long TIME_THRESHOLD = 10; // 900

	/** Constructor.
	 */
	public VehicleSystem() {
		vehicleSignals = new Thread(VehicleSystem.this, "VehicleSignalsThread");
		vehicleSignals.start();

		// Requests values because of bug in register values.
		automotiveManager.requestValue(
				AutomotiveSignalId.FMS_FUEL_LEVEL_1,
				AutomotiveSignalId.FMS_DRIVER_1_WORKING_STATE,
				AutomotiveSignalId.FMS_FUEL_RATE,
				AutomotiveSignalId.FMS_INSTANTANEOUS_FUEL_ECONOMY,
				AutomotiveSignalId.FMS_SERVICE_DISTANCE,
				AutomotiveSignalId.FMS_VEHICLE_MOTION,
				AutomotiveSignalId.FMS_TACHOGRAPH_VEHICLE_SPEED,
				AutomotiveSignalId.FMS_HIGH_RESOLUTION_ENGINE_TOTAL_FUEL_USED,
				AutomotiveSignalId.FMS_HIGH_RESOLUTION_TOTAL_VEHICLE_DISTANCE);

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

	/** Class Method that returns the legal uptime in seconds constant
	 * @return A long with the max legal uptime in seconds.
	 */
	public static long getLegalUptimeInSeconds() {
		return LEGAL_UPTIME_IN_SECONDS;
	}

	/** Estimates how long until a refuel is recommended.
	 * @return how many km until a stop for fueling is recommended.
	 */
	public synchronized double getKilometersUntilRefuel() {
		try {
			double currentLitersInTank = ((fuelLevel.getFloatValue()/100.0) * TEMP_TANK_SIZE_IN_LITERS);

			if (getVehicleState() == MovingState.DRIVE_AND_MOVING) {
				float addedConsumption = 0;
				for(SCSFloat consumption : instantFuelEconomyList) {
					addedConsumption = addedConsumption + consumption.getFloatValue();
				}
				double meanFuelEconomy =  (double) addedConsumption / instantFuelEconomyList.size();
				return (meanFuelEconomy * currentLitersInTank);
			} else if (getVehicleState() == MovingState.DRIVE_BUT_NOT_MOVING) {
				// TODO, Evaluate for drive but not moving state
				return 0;
			} else {
				double averageFuelConsumptionPerKilometer = totalFuelUsed.getDoubleValue() / (totalVehicleDistance.getLongValue() * Math.pow(10, -3));
				return currentLitersInTank / averageFuelConsumptionPerKilometer;
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new NullPointerException("Nullpointer in getKilometersUntilRefuel: totalFuelUsed or totalVehicleDistance not initialized");
		}
	}

	/** Method that returns the number of seconds until a stop is required.
	 * @return
	270 if currently in a break
	Positive number with seconds left if driving
	Negative number if drive longer than legal.
	 */
	public synchronized double getTimeUntilForcedRest() {
		try {
			if (getVehicleState() != MovingState.NOT_IN_DRIVE) {
				return (LEGAL_UPTIME_IN_SECONDS - ((System.nanoTime() - startTime) * NANOSECONDS_TO_SECONDS));
			} else {
				return LEGAL_UPTIME_IN_SECONDS;
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new NullPointerException("Nullpointer in getTimeUntilForcedRest: startTime not initialized");
		}
	}

	/** Method that returns the number om kilometers until service is recommended.
	 * @return how many km until a stop for service is recommended.
	 */
	public synchronized int getKilometersUntilService() {
		try {
			return distanceToService.getIntValue();
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new NullPointerException("Nullpointer in getKilometersUntilService: distanceToService not initialized");
		}
	}

	/** Method that returns the current state of the vehicle.
	 * @return a constant int value from class MovingState.
	 */
	public synchronized int getVehicleState() {
		try {
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
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new NullPointerException("Nullpointer in getVehicleState: isMoving or workingState not initialized");
		}
	}

	// ********** PRIVATE METHODS THAT NOTIFY OBSERVERS ********** //

	private void determineShortTime() {
		if(workingState.getIntValue() == 3) {
			Log.d("Time diff", ((System.nanoTime() - startTime) * NANOSECONDS_TO_SECONDS) + "");
			Log.d("Time diff with legalUptime:", (LEGAL_UPTIME_IN_SECONDS - ((System.nanoTime() - startTime) * NANOSECONDS_TO_SECONDS) + ""));
			if ((LEGAL_UPTIME_IN_SECONDS - ((System.nanoTime() - startTime) * NANOSECONDS_TO_SECONDS)) < TIME_THRESHOLD) {
				if (!timeHasBeenNotified) {
					setChanged();
					notifyObservers(SignalType.SHORT_TIME);
					timeHasBeenNotified = true;
				}
			}
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

	// Notify observers if the state of the driver has changed
	private void determineWorkStateChange(int prevState, int curState) {
		if(prevState != curState && curState != -1) {
			setChanged();
			notifyObservers(SignalType.VEHICLE_STOPPED_OR_STARTED);
		}
	}

	private void determineMovingChange() {
		// No check is necessary
		setChanged();
		notifyObservers(SignalType.VEHICLE_STOPPED_OR_STARTED);
	}

	// Only notifies observers if the previous km to service was above the threshold and the current km to service is below the threshold.
	// This to avoid multiple observer updates when service decreases.
	private void determineCloseToService(int prevKmToService, int kmToService) {
		if(kmToService <= SERVICE_THRESHOLD && prevKmToService > SERVICE_THRESHOLD) {
			setChanged();
			notifyObservers(SignalType.SHORT_TO_SERVICE);
		}
	}
}