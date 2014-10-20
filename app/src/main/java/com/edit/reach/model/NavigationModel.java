package com.edit.reach.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.edit.reach.constants.VehicleState;
import com.edit.reach.constants.UniversalConstants;
import com.edit.reach.constants.SignalType;
import com.edit.reach.model.interfaces.IMilestone;
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
public final class NavigationModel implements Runnable, Observer, SuggestionListener {

	private final VehicleSystem vehicleSystem;
	private final Map map;
	private final Thread pipelineThread;
	private final Handler mainHandler;

	private Handler pipelineHandler;

	private List<String> searchResults;

	/* --- CONSTANTS --- */
	private static final String PIPELINE_THREAD_NAME = "PipelineThread";
	private static final int FOOD_THRESHOLD = 30*60;
	private static final int REST_THRESHOLD = 15*60;
	private static final int GAS_THRESHOLD = 20*60;

	public NavigationModel(GoogleMap googleMap, Handler mainHandler) {
		this.pipelineThread = new Thread(this, PIPELINE_THREAD_NAME);
		this.pipelineThread.start();
		this.vehicleSystem = new VehicleSystem();
		this.vehicleSystem.addObserver(this);

		this.map = new Map(googleMap);
        this.map.addObserver(this);

		this.mainHandler = mainHandler;
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
		// TODO AISA for one category.
		if(category == IMilestone.Category.FOOD) {
			if(map.getRoute().getLegs().get(0).getDuration() <= FOOD_THRESHOLD) {

			}
		} else if(category == IMilestone.Category.RESTAREA) {
			if(map.getRoute().getLegs().get(0).getDuration() <= REST_THRESHOLD) {

			}

		} else if(category == IMilestone.Category.GASSTATION) {
			if(map.getRoute().getLegs().get(0).getDuration() <= GAS_THRESHOLD) {

			}

		} else {

		}
		return null;
	}

	/** Returns a map object.
	 * @return a Map
	 */
	public Map getMap() {
		synchronized (map) {
			return map;
		}
	}

	/** Add milestones to the route.
	 * @param list a list of milestones to be added.
	 */
	public void addMilestones(List<IMilestone> list) {
		synchronized (map) {
			map.getRoute().addMilestones(list);
		}
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
		Log.d("NavigationModel", "setRoute()");
		synchronized (map) {
			map.setRoute(newRoute);
		}
	}

	@Override
	public void onGetSuccess(List<String> results) {
		searchResults = results;
	}

	/**
	 * Do not call this method. It is called automatically when the observable changes.
	 * @param observable Not used
	 * @param data The id of the signal that initiated this update.
	 */
	@Override
	public void update(Observable observable, final Object data) {
		Log.d("NavigationModel", "update()");
		pipelineHandler.post(new Runnable() {
			@Override
			public void run() {
				// Obtain message from handler.
				Message message = Message.obtain(mainHandler);

				// Get the route.
				synchronized (map) {
					Route route = map.getRoute();

					switch ((Integer) data) {
						// If vehicle is low on fuel.
						case SignalType.LOW_FUEL:
							// TODO what to do here? Not used
							Log.d("UPDATE", "TYPE: LOW_FUEL");
							break;

						// If vehicles up time is short relative to the legal up time.
						case SignalType.SHORT_TIME:
							// TODO what to do here? Not used
							Log.d("UPDATE", "TYPE: SHORT_TIME");
							break;

						// If vehicle stopped or started
						case SignalType.VEHICLE_STOPPED_OR_STARTED:
							Log.d("UPDATE", "TYPE: VEHICLE_STOPPED_OR_STARTED");
							Log.d("GET", "Vehicle State: " + vehicleSystem.getVehicleState());

							// Send the MovingState to the UI.
							message.obj = vehicleSystem.getVehicleState();
							message.what = SignalType.VEHICLE_STOPPED_OR_STARTED;
							mainHandler.sendMessage(message);

							if(vehicleSystem.getVehicleState() == VehicleState.NOT_IN_DRIVE) {
								map.setState(Map.State.STATIONARY);
							} else {
								map.setState(Map.State.MOVING);
							}

							break;

						// If a vehicle took a break longer than or equal to 45 minutes.
						case SignalType.VEHICLE_TOOK_FINAL_BREAK:
							Log.d("UPDATE", "TYPE: VEHICLE_TOOK_FINAL_BREAK");

							// This has to be done in UI thread because of Googles Map.
							mainHandler.post(new Runnable() {
								@Override
								public void run() {
									synchronized (map) {
										// Remove all pauses and add them again.
										map.getRoute().removeAllPauses();
										addTimePause();
									}
								}
							});
							break;

						case SignalType.UPTIME_UPDATE:
							Log.d("UPDATE", "TYPE: UP_TIME_UPDATE");

							// Send the number of seconds until break to UI.
							message.obj = vehicleSystem.getTimeUntilForcedRest();
							message.what = SignalType.UPTIME_UPDATE;
							mainHandler.sendMessage(message);
							break;

						case SignalType.FUEL_UPDATE:
							Log.d("UPDATE", "TYPE: FUEL_UPDATE");
							Log.d("Thread in NavigationModel - Fuelupdate", Thread.currentThread().getName());

							// Send the amount of fuel in tank to the UI.
							message.obj = vehicleSystem.getFuelLevel();
							message.what = SignalType.FUEL_UPDATE;
							mainHandler.sendMessage(message);
							break;

						case SignalType.TANK_SIZE_CALCULATED:
							Log.d("UPDATE", "TYPE: TANK_SIZE_CALCULATED");
							final double kmToRefuel = vehicleSystem.getKilometersUntilRefuel();
							addFuelPause(kmToRefuel);
							break;

						case SignalType.LEG_UPDATE:
							Log.d("UPDATE", "TYPE: LEG_UPDATE");
							message.obj = route.getLegs().get(0);
							message.what = SignalType.LEG_UPDATE;
							mainHandler.sendMessage(message);
							break;

						case SignalType.ROUTE_TOTAL_TIME_UPDATE:
							Log.d("UPDATE", "TYPE: ROUTE_TOTAL_TIME_UPDATE");
							message.obj = route.getDuration();
							message.what = SignalType.ROUTE_TOTAL_TIME_UPDATE;
							mainHandler.sendMessage(message);
							break;

						case SignalType.ROUTE_INITIALIZATION_SUCCEDED:
							Log.d("UPDATE", "TYPE: ROUTE_INITIALIZATION_SUCCEEDED");
							addTimePause();
							message.what = SignalType.ROUTE_INITIALIZATION_SUCCEDED;
							mainHandler.sendMessage(message);
							break;

						case SignalType.ROUTE_INITIALIZATION_FAILED:
							Log.d("UPDATE", "TYPE: ROUTE_INITIALIZATION_FAILED");
							message.what = SignalType.ROUTE_INITIALIZATION_FAILED;
							mainHandler.sendMessage(message);
							break;

						default:
							Log.d("TYPE ERROR", "Type error in update");
							break;
					}
				}
			}
		});
	}

	// Method that adds time-pauses in the map.
	private void addTimePause() {
		synchronized (map) {
		long routeTime = map.getRoute().getDuration();
		long nmbrOfPauses = routeTime / UniversalConstants.LEGAL_UPTIME_IN_SECONDS;

		for (int i = 1; i < nmbrOfPauses; i++) {
			Log.d("NavModel", "Adding pause: ");
			map.getRoute().addPause(i * UniversalConstants.LEGAL_UPTIME_IN_SECONDS);
			}
		}
	}

	// Method that add fuel-pause in the map.
	private void addFuelPause(double kmToRefuel) {
		synchronized (map) {
			map.getRoute().addPause(kmToRefuel);
		}
	}
}
