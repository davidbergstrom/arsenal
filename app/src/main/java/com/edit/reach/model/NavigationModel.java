package com.edit.reach.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.edit.reach.constants.UniversalConstants;
import com.edit.reach.constants.SignalType;
import com.edit.reach.model.interfaces.IMilestone;
import com.edit.reach.model.interfaces.RouteListener;
import com.edit.reach.model.interfaces.SuggestionListener;
import com.edit.reach.system.VehicleSystem;
import com.edit.reach.utils.SuggestionUtil;
import com.google.android.gms.maps.GoogleMap;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Class that merges data from the vehicle and the map.
 * The class finds optimal stops for the trip, among other things.
 * This class is using the singleton pattern.
 * Created by: Tim Kerschbaumer
 * Project: REACH
 * Date: 2014-09-27
 * Time: 19:27
 * Last Edit: 2014-10-17
 */
public class NavigationModel implements Runnable, Observer, SuggestionListener, RouteListener {

	private final VehicleSystem vehicleSystem;

	private final Thread pipelineThread;
	private final Handler mainHandler;

	private Handler pipelineHandler;

	private Map map;

	private List<String> searchResults;

	private static NavigationModel navigationModel;

	/* --- CONSTANTS --- */
	private static final String PIPELINE_THREAD_NAME = "PipelineThread";

	// Private constructor that makes initializations.
	private NavigationModel() {
		this.pipelineThread = new Thread(this, PIPELINE_THREAD_NAME);
		this.pipelineThread.start();

		this.vehicleSystem = new VehicleSystem();
		this.vehicleSystem.addObserver(this);

		this.mainHandler = new Handler(Looper.getMainLooper());
	}

	@Override
	public void run() {
		try {
			Looper.prepare();
			pipelineHandler = new Handler();
			Looper.loop();
		} catch (Throwable t) {
			Log.d("Error in pipelineThread", t + "");
		}
	}

	/** Returns an instance of this class.
	 * @return an instance of the NavigationModel.
	 */
	public static NavigationModel getInstance() {
		if (navigationModel == null) {
			navigationModel = new NavigationModel();
		}
		return navigationModel;
	}

	/** This method is used to find pauses in driving mode.
	 * @param categoryList a list of categories with what the user wants.
	 * @return a IMilestone that matches the categories specified.
	 */
	public IMilestone getPauseSuggestions(List<IMilestone.Category> categoryList) {
		// TODO The AISA method for multiple categories.
		return null;
	}


	/** This method is used to find pauses in driving mode.
	 * @param category a category with what the user wants.
	 * @return a IMilestone that matches the category specified.
	 */
	public IMilestone getPauseSuggestions(IMilestone.Category category) {
		// TODO The AISA method for one category
		return null;
	}

	/** Initialized map with a googleMap.
	 * If the map is already initialized, nothing will happend.
	 * Always set the map before using methods in this class.
	 * If you do not call this method it will result in a nullpointer for many methods in this class.
	 * @param googleMap
	 */
	public void setGoogleMap(GoogleMap googleMap) {
		if (this.map == null) {
			this.map = new Map(googleMap);
		}
	}

	/** Returns a map object.
	 * @return a Map
	 */
	public Map getMap() {
		return map;
	}

	/** Add milestones to the route.
	 * @param list a list of milestones to be added.
	 */
	public void addMilestones(List<IMilestone> list) {
		map.getRoute().addMilestones(list);
	}

	/** This method is used to match search result strings.
	 * @param searchString the string to match a result with
	 * @return a list of strings with results.
	 */
	public List<String> getMatchedStringResults(final String searchString) {
		SuggestionUtil suggestionUtil = new SuggestionUtil(this);
		suggestionUtil.searchForAddresses(searchString);
		return searchResults;
	}

	/** Sets the route in the map.
	 * @param newRoute the route to be set.
	 */
	public void setRoute(final Route newRoute) {
		map.setRoute(newRoute);
		newRoute.addListener(this);
	}

	@Override
	public void onGetSuccess(List<String> results) {
		searchResults = results;
	}

	@Override
	public void onInitialization(boolean success) {
		Message message = mainHandler.obtainMessage();
		if (success) {
			addTimePause();
			message.what = SignalType.ROUTE_INITIALIZATION_SUCCEDED;
		} else {
			message.what = SignalType.ROUTE_INITIALIZATION_FAILED;
		}
		mainHandler.sendMessage(message);
	}

	@Override
	public void onLegFinished(Leg finishedLeg) {
		// TODO Reached a milestone.
	}

	@Override
	public void onPauseAdded(Pause pause) {
		// TODO do nothing?
	}

	@Override
	public void onStepFinished(Step finishedStep) {
		// TODO do nothing?
	}

	/**
	 * Do not call this method. It is called automatically when the observable changes.
	 * @param observable Not used
	 * @param data The id of the signal that initiated this update.
	 */
	@Override
	public synchronized void update(Observable observable, final Object data) {
		pipelineHandler.post(new Runnable() {
			@Override
			public void run() {
				// Obtain message from handler.
				Message message = Message.obtain(mainHandler);

				// Sets the message to the routes legs and send it to the UI.
				message.obj = map.getRoute().getLegs();
				message.what = SignalType.LEG_UPDATE;
				mainHandler.sendMessage(message);

				// If vehicle is low on fuel.
				if ((Integer) data == SignalType.LOW_FUEL) {
					// TODO what to do here?
					Log.d("UPDATE", "TYPE: LOW_FUEL");
					Log.d("GET", "Km to refuel: " + vehicleSystem.getKilometersUntilRefuel());

				// If vehicles up time is short relative to the legal up time.
				} else if ((Integer) data == SignalType.SHORT_TIME) {
					// TODO what to do here?
					Log.d("UPDATE", "TYPE: SHORT_TIME");
					Log.d("GET", "Time until rest: " + vehicleSystem.getTimeUntilForcedRest());

				// If vehicle stopped or started
				} else if ((Integer) data == SignalType.VEHICLE_STOPPED_OR_STARTED) {
					Log.d("UPDATE", "TYPE: VEHICLE_STOPPED_OR_STARTED");
					Log.d("GET", "Vehicle State: " + vehicleSystem.getVehicleState());

					// Send the MovingState to the UI.
					message.obj = vehicleSystem.getVehicleState();
					message.what = SignalType.VEHICLE_STOPPED_OR_STARTED;
					mainHandler.sendMessage(message);

				// If a vehicle took a break longer than or equal to 45 minutes.
				} else if ((Integer) data == SignalType.VEHICLE_TOOK_FINAL_BREAK) {
					Log.d("UPDATE", "TYPE: VEHICLE_TOOK_FINAL_BREAK");

					// This has to be done in UI thread because of Googles Map.
					mainHandler.post(new Runnable() {
						@Override
						public void run() {
							// Remove all pauses and add them again.
							map.getRoute().removeAllPauses();
							addTimePause();
						}
					});

				// If the up time is updated.
				} else if ((Integer) data == SignalType.UPTIME_UPDATE) {
					Log.d("UPDATE", "TYPE: UP_TIME_UPDATE");

					// Send the number of seconds until break to UI.
					message.obj = vehicleSystem.getTimeUntilForcedRest();
					message.what = SignalType.UPTIME_UPDATE;
					mainHandler.sendMessage(message);

				// If the fuel level is updated.
				} else if ((Integer) data == SignalType.FUEL_UPDATE) {
					Log.d("UPDATE", "TYPE: FUEL_UPDATE");

					// Send the amount of fuel in tank to the UI.
					message.obj = vehicleSystem.getFuelLevel();
					message.what = SignalType.FUEL_UPDATE;
					mainHandler.sendMessage(message);

				// If it is possible to get km to refuel.
				} else if ((Integer) data == SignalType.TANK_SIZE_CALCULATED) {
					Log.d("UPDATE", "TYPE: TANK_SIZE_CALCULATED");

					final double kmToRefuel = vehicleSystem.getKilometersUntilRefuel();

					// This has to be done in UI thread because of Googles Map.
					mainHandler.post(new Runnable() {
						@Override
						public void run() {
							addFuelPause(kmToRefuel);
						}
					});

				// If No signal matches
				} else {
					Log.d("TYPE ERROR", "Type error in update");
				}
			}
		});
	}

	// Method that adds time-pauses in the map.
	private void addTimePause() {
		long routeTime = map.getRoute().getDuration();
		long nmbrOfPauses = routeTime / UniversalConstants.LEGAL_UPTIME_IN_SECONDS;

		for (int i = 1; i < nmbrOfPauses; i++) {
			Log.d("NavModel", "Adding pause: ");
			map.getRoute().addPause(i * UniversalConstants.LEGAL_UPTIME_IN_SECONDS);
		}
	}

	// Method that add fuel-pause in the map.
	private void addFuelPause(double kmToRefuel) {
		map.getRoute().addPause(kmToRefuel);
	}
}
