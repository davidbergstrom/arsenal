package com.edit.reach.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.edit.reach.constants.UniversalConstants;
import com.edit.reach.constants.SignalType;
import com.edit.reach.model.interfaces.IMilestone;
import com.edit.reach.model.interfaces.MilestonesReceiver;
import com.edit.reach.model.interfaces.SuggestionListener;
import com.edit.reach.system.VehicleSystem;
import com.edit.reach.utils.SuggestionUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

// TODO remove marker.
// TODO If last milestone is same milestone.
// TODO If if one milestone was showed. Do not show i again.
// TODO If milestone failed. Make map go back to original mode.
// Maybe we should only get the milestone once and go through the list?

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
public final class NavigationModel implements Runnable, Observer, SuggestionListener, MilestonesReceiver {

	private final VehicleSystem vehicleSystem;
	private final Map map;
	private final Thread pipelineThread;
	private final Handler mainHandler;

	private Handler pipelineHandler;

	private IMilestone milestone;

	private List<String> searchResults;

	private int milestoneAlgorithmStage = 0;
	private IMilestone.Category milestoneCategory;
	private int milestoneListIndex = 0;

	/* --- CONSTANTS --- */
	private static final String PIPELINE_THREAD_NAME = "PipelineThread";

	private int FOOD_THRESHOLD = 45 * 60;
	private int GAS_THRESHOLD = 30 * 60;
	private int REST_THRESHOLD = 15 * 60;
	private int TOILET_THRESHOLD = 10 * 60;

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

	/**
	 * This method is used to find pauses in driving mode.
	 *
	 * @param category a category with what the user wants.
	 * @return a IMilestone that matches the category specified.
	 */
	public void getPauseSuggestions(final IMilestone.Category category) {
		Log.d("NavigationModel", "entered getPauseSuggestions");
		this.milestoneCategory = category;
		Route route = map.getRoute();
		List<Leg> legs = route.getLegs();
		Message message = mainHandler.obtainMessage();

		if (category == IMilestone.Category.FOOD) {
			if (inThreshold(FOOD_THRESHOLD) && this.milestoneAlgorithmStage == 0) {
				if (hasCategory(category)) {
					notifyUI(legs.get(0).getMilestone());
				}
			} else {
				milestoneAlgorithmStage += 1;
			}

			if (milestoneAlgorithmStage >= 1) {
				if(route.getLocation(FOOD_THRESHOLD) == null) {
					message.what = SignalType.MILESTONE_FAIL;
					mainHandler.sendMessage(message);
				} else {
					Ranking.getMilestonesByDistance(route.getPointerLocation(), route.getLocation(FOOD_THRESHOLD), category, this);
				}
			}

		} else if (category == IMilestone.Category.GASSTATION) {
			if (inThreshold(GAS_THRESHOLD) && this.milestoneAlgorithmStage == 0) {
				if (hasCategory(category)) {
					notifyUI(legs.get(0).getMilestone());
				}
			} else {
				milestoneAlgorithmStage += 1;
			}

			if(route.getLocation(FOOD_THRESHOLD) == null) {
				message.what = SignalType.MILESTONE_FAIL;
				mainHandler.sendMessage(message);
			} else {
				Ranking.getMilestonesByDistance(route.getPointerLocation(), route.getLocation(GAS_THRESHOLD), category, this);
			}

		} else if (category == IMilestone.Category.RESTAREA) {
			if (inThreshold(REST_THRESHOLD) && this.milestoneAlgorithmStage == 0) {
				if (hasCategory(category)) {
					notifyUI(legs.get(0).getMilestone());
				}
			} else {
				milestoneAlgorithmStage += 1;
			}

			if(route.getLocation(FOOD_THRESHOLD) == null) {
				message.what = SignalType.MILESTONE_FAIL;
				mainHandler.sendMessage(message);
			} else {
				Ranking.getMilestonesByDistance(route.getPointerLocation(), route.getLocation(REST_THRESHOLD), category, this);

			}

		} else if (category == IMilestone.Category.TOILET) {
			if (inThreshold(TOILET_THRESHOLD) && this.milestoneAlgorithmStage == 0) {
				if (hasCategory(category)) {
					notifyUI(legs.get(0).getMilestone());
				}
			} else {
				milestoneAlgorithmStage += 1;
				mainHandler.sendMessage(message);
			}

			if(route.getLocation(FOOD_THRESHOLD) == null) {
				message.what = SignalType.MILESTONE_FAIL;
				mainHandler.sendMessage(message);
			} else {
				Ranking.getMilestonesByDistance(route.getPointerLocation(), route.getLocation(TOILET_THRESHOLD), category, this);
			}

		} else {
			Log.d("getPauseSuggestions()", "Passed an illegal category");
		}
	}

	/**
	 * This method is called to tell the navigationModel if a suggested milestone was accepted or not.
	 *
	 * @param accepted
	 */
	public void acceptedMilestone(boolean accepted) {
		Log.d("NavigationModel", "entered acceptedMilestone");
		// If milestone was accepted by the user.
		if (milestone != null && accepted) {
			map.setMapState(Map.MapState.MOVING);
			boolean milestoneExists = false;

			for (Leg l : map.getRoute().getLegs()) {
				if (l.getMilestone().equals(this.milestone)) {
					milestoneExists = true;
				}
			}

			if (!milestoneExists) {
				map.getRoute().addMilestone(this.milestone);
			}

		// If milestone was not accepted by the user.
		} else {
			milestoneAlgorithmStage += 1;
			if (milestoneAlgorithmStage >= 1) {
				milestoneListIndex += 1;
			}
			getPauseSuggestions(milestoneCategory);
		}
	}

	@Override
	public void onMilestonesRecieved(ArrayList<IMilestone> milestones) {
		Log.d("NavigationModel", "entered onMilestonesRecieved");
		if (milestones.size() == 0 || milestoneListIndex >= milestones.size()) {
			milestoneAlgorithmStage += 1;
			milestoneListIndex = 0;
			extendMilestoneSearch();
			getPauseSuggestions(this.milestoneCategory);
		} else {
			notifyUI(milestones.get(milestoneListIndex));
		}
	}

	@Override
	public void onMilestonesGetFailed() {
		Log.d("NavigationModel", "entered onMilestonesGetFailed");
	}

	/**
	 * Returns a map object.
	 *
	 * @return a Map
	 */
	public Map getMap() {
		Log.d("NavigationModel", "entered getMap");
		synchronized (map) {
			return map;
		}
	}

	/**
	 * Add milestones to the route.
	 *
	 * @param list a list of milestones to be added.
	 */
	public void addMilestones(List<IMilestone> list) {
		Log.d("NavigationModel", "entered addMilestones");
		synchronized (map) {
			map.getRoute().addMilestones(list);
		}
	}

	/**
	 * This method is used to match search result strings.
	 *
	 * @param searchString the string to match a result with
	 * @return a list of strings with results.
	 */
	public List<String> getMatchedStringResults(final String searchString) {
		Log.d("NavigationModel", "entered getMatchedStringResults");
		SuggestionUtil suggestionUtil = new SuggestionUtil(this);
		suggestionUtil.searchForAddresses(searchString);
		return searchResults;
	}

	/**
	 * Sets the route in the map.
	 *
	 * @param newRoute the route to be set.
	 */
	public void setRoute(final Route newRoute) {
		Log.d("NavigationModel", "entered setRoute");
		synchronized (map) {
			map.setRoute(newRoute);
		}
	}

	/**
	 * Toggles demomode on the map.
	 *
	 * @param demo true if demomode should be activated, false otherwise.
	 */
	public void setDemo(boolean demo) {
		Log.d("NavigationModel", "entered setDemo");
		map.setDemoMode(demo);
	}

	@Override
	public void onGetSuccess(List<String> results) {
		Log.d("NavigationModel", "entered onGetSuccess");
		searchResults = results;
	}

	/**
	 * Do not call this method. It is called automatically when the observable changes.
	 *
	 * @param observable Not used
	 * @param data       The id of the signal that initiated this update.
	 */
	@Override
	public void update(Observable observable, final Object data) {
		pipelineHandler.post(new Runnable() {
			@Override
			public void run() {
				// Obtain message from handler.
				Message message = Message.obtain(mainHandler);

				synchronized (map) {
					// Gets the route from the map;
					Route route = map.getRoute();

					switch ((Integer) data) {

						// If vehicle stopped or started
						case SignalType.VEHICLE_STOPPED_OR_STARTED:
							Log.d("UPDATE", "TYPE: VEHICLE_STOPPED_OR_STARTED");
							Log.d("GET", "Vehicle State: " + vehicleSystem.getVehicleState());

							// Send the MovingState to the UI.
							message.obj = vehicleSystem.getVehicleState();
							message.what = SignalType.VEHICLE_STOPPED_OR_STARTED;
							mainHandler.sendMessage(message);
							break;

						// If fuel changes
						case SignalType.FUEL_UPDATE:
							Log.d("UPDATE", "TYPE: FUEL_UPDATE");

							// Send the amount of fuel in tank to the UI.
							message.obj = vehicleSystem.getFuelLevel();
							message.what = SignalType.FUEL_UPDATE;
							mainHandler.sendMessage(message);
							break;

						// If up time changes
						case SignalType.UPTIME_UPDATE:
							Log.d("UPDATE", "TYPE: UP_TIME_UPDATE");

							// Send the number of seconds until break to UI.
							message.obj = vehicleSystem.getTimeUntilForcedRest();
							message.what = SignalType.UPTIME_UPDATE;
							mainHandler.sendMessage(message);
							break;

						// If leg changes
						case SignalType.LEG_UPDATE:
							Log.d("UPDATE", "TYPE: LEG_UPDATE");

							message.obj = route.getLegs().get(0);
							message.what = SignalType.LEG_UPDATE;
							mainHandler.sendMessage(message);
							break;

						// If route total time changes
						case SignalType.ROUTE_TOTAL_TIME_UPDATE:
							Log.d("UPDATE", "TYPE: ROUTE_TOTAL_TIME_UPDATE");

							message.obj = route.getDuration();
							message.what = SignalType.ROUTE_TOTAL_TIME_UPDATE;
							mainHandler.sendMessage(message);
							break;

						// If route initialization succeeded
						case SignalType.ROUTE_INITIALIZATION_SUCCEDED:
							Log.d("UPDATE", "TYPE: ROUTE_INITIALIZATION_SUCCEEDED");

							mainHandler.post(new Runnable() {
								@Override
								public void run() {
									addTimePause();
								}
							});

							message.what = SignalType.ROUTE_INITIALIZATION_SUCCEDED;
							mainHandler.sendMessage(message);
							break;

						// If route initialization failed.
						case SignalType.ROUTE_INITIALIZATION_FAILED:
							Log.d("UPDATE", "TYPE: ROUTE_INITIALIZATION_FAILED");
							message.what = SignalType.ROUTE_INITIALIZATION_FAILED;
							mainHandler.sendMessage(message);
							break;

						// If a vehicle took a break longer than or equal to 45 minutes.
						case SignalType.VEHICLE_TOOK_FINAL_BREAK:
							// TODO Does this work?
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

						// If a pause was added
						//case SignalType.PAUSE_ADDED:
							/*Pause pause = map.getRoute().getPauses().get(0);
							// If the pause was a fuelpause.
							if(pause.getType() == Pause.PauseType.FUEL) {
								map.moveCameraTo(pause.getLocation(), 13);

								// TODO implement in GUI. Remember to set state of map when done.
								message.what = SignalType.PAUSE_ADDED;
								mainHandler.sendMessage(message);
							}*/

						// If the tank size has been calculated
						case SignalType.TANK_SIZE_CALCULATED:
							Log.d("UPDATE", "TYPE: TANK_SIZE_CALCULATED");
							//map.getRoute().addPause((long) vehicleSystem.getTimeUntilRefuel(), Pause.PauseType.FUEL);
							break;

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

						default:
							Log.d("TYPE ERROR", "Type error in update");
							break;
					}
				}
			}
		});
	}

	// This method determines if a milestone is in a threshold
	private boolean inThreshold(final int threshold) {
		Log.d("NavigationModel", "entered inThreshold");

		boolean inThreshold = false;
		List<Leg> legs = map.getRoute().getLegs();
		if (legs.get(0).getDuration() <= threshold) {
			inThreshold = true;
		}
		return inThreshold;
	}

	// This method determines if a milestone has a category
	private boolean hasCategory(IMilestone.Category category) {
		Log.d("NavigationModel", "entered hasCategory");

		boolean hasCategory = false;
		List<Leg> legs = map.getRoute().getLegs();
		if (legs.get(0).getMilestone().hasCategory(category)) {
			hasCategory = true;
		}
		return hasCategory;
	}

	// This method notifies the UI of changes
	private void notifyUI(IMilestone milestone) {
		Log.d("NavigationModel", "entered notifyUI");
		this.milestone = milestone;
		// TODO remove marker when done
		Marker marker = map.showMilestone(this.milestone);

		Message message = mainHandler.obtainMessage();
		message.what = SignalType.MILESTONE_SUCCED;
		message.obj = this.milestone;
		mainHandler.sendMessage(message);
	}

	// Extend search perimiter with one hour.
	private void extendMilestoneSearch() {
		Log.d("NavigationModel", "entered extendMilestoneSearch");
		FOOD_THRESHOLD += 60*60;
		GAS_THRESHOLD += 60*60;
		REST_THRESHOLD += 60*60;
		TOILET_THRESHOLD = 60*60;
	}

	// Method that adds time-pauses in the map.
	private void addTimePause() {
		Log.d("NavigationModel", "entered addTimePause");
		synchronized (map) {
			long routeTime = map.getRoute().getDuration();
			Log.d("RouteTime", routeTime + "");
			long nmbrOfPauses = routeTime / UniversalConstants.LEGAL_UPTIME_IN_SECONDS;
			Log.d("Number of pauses", nmbrOfPauses + "");

			for (int i = 1; i <= nmbrOfPauses; i++) {
				Log.d("Adding pause", "NavigationModel");
				map.getRoute().addPause(i * UniversalConstants.LEGAL_UPTIME_IN_SECONDS, Pause.PauseType.TIME);
			}
		}
	}
}
