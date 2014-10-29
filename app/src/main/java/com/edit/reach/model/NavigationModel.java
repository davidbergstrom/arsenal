package com.edit.reach.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.edit.reach.constants.SignalType;
import com.edit.reach.constants.UniversalConstants;
import com.edit.reach.system.VehicleSystem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

// TODO Test Multithreading for AISA
// Bugs:
// Clicking on ADD not working.
// Clicking on cancel not working properly
// Slow reloading?

/**
 * Class that merges data from the vehicle and the map.
 * The class finds optimal stops for the trip, among other things.
 * This class is using the singleton pattern.
 * Created by: Tim Kerschbaumer
 * Project: Milestone
 * Date: 2014-09-27
 * Time: 19:27
 * Last Edit: 2014-10-29
 */
public final class NavigationModel implements Runnable, Observer {

	private final VehicleSystem vehicleSystem;
	private final Map map;
	private final Thread pipelineThread;
	private final Handler mainHandler;
	private final Object mapLock = new Object();

	private Handler pipelineHandler;

	private IMilestone milestone;

	private List<String> searchResults;

	private List<IMilestone> milestones;

	private Marker marker;

	private IMilestone.Category milestoneCategory;

	//private final AtomicInteger milestoneAlgorithmStage = new AtomicInteger(0);

	// Using AtomicInteger for thread safety
	private final MilestonesReceiver milestonesReceiver = new MilestonesReceiver() {

		@Override
		public void onMilestonesRecieved(ArrayList<IMilestone> milestonesList) {
			Log.d("NavigationModel", "entered onMilestonesRecieved");

			// If milestones has not been initialized.
			if(milestones == null) {
				milestones = milestonesList;
			}
			updateMilestonesList();
		}

		@Override
		public void onMilestonesGetFailed() {
			Log.d("NavigationModel", "entered onMilestonesGetFailed");
		}
	};

	private final SuggestionListener suggestionListener = new SuggestionListener() {
		@Override
		public void onGetSuccess(List<String> results) {
			Log.d("NavigationModel", "entered onGetSuccess");
			searchResults = results;
		}
	};

	/* --- CONSTANTS --- */
	private static final String PIPELINE_THREAD_NAME = "PipelineThread";

	private static final int FOOD_THRESHOLD = 45 * 60;
	private static final int GAS_THRESHOLD = 30 * 60;
	private static final int REST_THRESHOLD = 15 * 60;
	private static final int TOILET_THRESHOLD = 10 * 60;
	private static final long STANDARD_SEARCH_RANGE = 15 * 60;

	// Using AtomicLong for thread safety
	private final AtomicLong searchRange = new AtomicLong(STANDARD_SEARCH_RANGE);

	/** Constructor
	 * @param googleMap a google map
	 * @param mainHandler a main handler for recieving data.
	 */
	public NavigationModel(GoogleMap googleMap, Handler mainHandler) {
		// Create a new thread and start it.
		this.pipelineThread = new Thread(this, PIPELINE_THREAD_NAME);
		this.pipelineThread.start();

		// Create new VehicleSystem and make it an observable.
		this.vehicleSystem = new VehicleSystem();
		this.vehicleSystem.addObserver(this);

		// Create a new Map and make it an observable.
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
			Log.d("Navigationmodel: Error in pipelineThread", t + "");
		}
	}

	/**
	 * This method is used to find pauses in driving mode.
	 *
	 * @param category a category with what the user wants.
	 */
	public void getPauseSuggestions(final IMilestone.Category category) {
		Log.d("NavigationModel", "entered getPauseSuggestions");
		milestoneCategory = category;

		switch (milestoneCategory) {
			case TOILET:
				getPauses(TOILET_THRESHOLD);
				break;
			case GASSTATION:
				getPauses(GAS_THRESHOLD);
				break;
			case RESTAREA:
				getPauses(REST_THRESHOLD);
				break;
			case RESTAURANT:
				getPauses(FOOD_THRESHOLD);
				break;
			default:
				Log.d("getPauseSuggestions()", "Passed an illegal category");
				break;
		}
	}

	/**
	 * This method is called to tell the navigationModel if a suggested milestone was accepted or not.
	 *
	 * @param accepted
	 */
	public void acceptMilestone(final boolean accepted) {
		Log.d("NavigationModel", "entered acceptMilestone");

		// Remove marker from map.
		marker.remove();

		synchronized (mapLock) {
			// If milestone was accepted by the user.
			if (accepted) {
				setMapState(Map.MapState.MOVING);
				searchRange.set(STANDARD_SEARCH_RANGE);

				boolean milestoneExists = false;
				// Check if milestone already exists
				for (Leg leg : map.getRoute().getLegs()) {
					if (leg.getMilestone() != null && leg.getMilestone().equals(this.milestone)) {
						milestoneExists = true;
					}
				}
				// If milestone does not exist. Create it.
				if (!milestoneExists) {
					map.getRoute().addMilestone(this.milestone);
				}

				//milestoneAlgorithmStage.set(0);

			// If milestone was not accepted by the user.
			} else{
				//milestoneAlgorithmStage.getAndAdd(1);
				getPauseSuggestions(milestoneCategory);
			}
		}
	}

	/** Call this method to reset initial values for the pause suggestion algorithm.
	 * Call only if algorithm is cancelled by the UI
	 */
	public void cancelMilestone() {
		// Reset the search range.
		searchRange.set(STANDARD_SEARCH_RANGE);
		//milestoneAlgorithmStage.set(0);
		setMapState(Map.MapState.MOVING);
	}

	/**
	 * Returns a map object.
	 *
	 * @return a Map
	 */
	public Map getMap() {
		Log.d("NavigationModel", "entered getMap");
		synchronized (mapLock) {
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
		synchronized (mapLock) {
			map.getRoute().addMilestones(list);
		}
	}

	/**
	 * Sets the route in the map.
	 *
	 * @param newRoute the route to be set.
	 */
	public void setRoute(final Route newRoute) {
		Log.d("NavigationModel", "entered setRoute");
		synchronized (mapLock) {
			map.setRoute(newRoute);
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
		Suggestion suggestion = new Suggestion(suggestionListener);
		suggestion.searchForAddresses(searchString);
		return searchResults;
	}

	/**
	 * Toggles demo mode for this application.
	 * This method is only used to test the application when not in a truck.
	 *
	 * @param demo true if demomode should be activated, false otherwise.
	 */
	public void setDemo(boolean demo) {
		Log.d("NavigationModel", "entered setDemo");
		map.setDemoMode(demo);
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
				synchronized (mapLock) {
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

						// If a leg changes
						case SignalType.LEG_UPDATE:
							Log.d("UPDATE", "TYPE: LEG_UPDATE");

							// Send the current leg to the UI.
							message.obj = route.getLegs().get(0);
							message.what = SignalType.LEG_UPDATE;
							mainHandler.sendMessage(message);
							break;

						// If route total time changes
						case SignalType.ROUTE_TOTAL_TIME_UPDATE:
							Log.d("UPDATE", "TYPE: ROUTE_TOTAL_TIME_UPDATE");

							// Send route duration to the UI.
							message.obj = route.getDuration();
							message.what = SignalType.ROUTE_TOTAL_TIME_UPDATE;
							mainHandler.sendMessage(message);
							break;

						// If route initialization succeeded
						case SignalType.ROUTE_INITIALIZATION_SUCCEDED:
							Log.d("UPDATE", "TYPE: ROUTE_INITIALIZATION_SUCCEEDED");

							// This has to be done in UI thread because of Google map.
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
							Log.d("UPDATE", "TYPE: VEHICLE_TOOK_FINAL_BREAK");

							// This has to be done in UI thread because of Googles Map.
							// Removes and adds new time pauses
							mainHandler.post(new Runnable() {
								@Override
								public void run() {
									synchronized (mapLock) {
										// Remove all pauses and add them again.
										map.getRoute().removeAllPauses();
										addTimePause();
									}
								}
							});
							break;

						// If the tank size has been calculated
						case SignalType.TANK_SIZE_CALCULATED:
							// This signal is not used in the current version of this program.
							Log.d("UPDATE", "TYPE: TANK_SIZE_CALCULATED");
							break;

						// If vehicle is low on fuel.
						case SignalType.LOW_FUEL:
							// This signal is not used in the current version of this program.
							Log.d("UPDATE", "TYPE: LOW_FUEL");
							break;

						// If vehicles up time is short relative to the legal up time.
						case SignalType.SHORT_TIME:
							// This signal is not used in the current version of this program.
							Log.d("UPDATE", "TYPE: SHORT_TIME");
							break;

						default:
							Log.d("UPDATE", "TYPE: ERROR; NOT A VALID SIGNAL");
							break;
					}
				}
			}
		});
	}

	// ******* PRIVATE METHODS ******** //

	// Method that adds time-pauses in the map.
	private void addTimePause() {
		synchronized (mapLock) {
			long routeTime = map.getRoute().getDuration();
			long nmbrOfPauses = routeTime / UniversalConstants.LEGAL_UPTIME_IN_SECONDS;

			for (int i = 1; i <= nmbrOfPauses; i++) {
				map.getRoute().addPause(i * UniversalConstants.LEGAL_UPTIME_IN_SECONDS, Pause.PauseType.TIME);
			}
		}
	}

	// Method that gets best-matched milestones
	private void getPauses(final int threshold) {
		final Route route = map.getRoute();
		final List<Leg> legs = route.getLegs();
		final LatLng pointerLocation = route.getPointerLocation();

		pipelineHandler.post(new Runnable() {
			@Override
			public void run() {
				synchronized (mapLock) {
					if (inThreshold(threshold) && hasCategory(milestoneCategory)) {
						notifyUISuccess(legs.get(0).getMilestone());
					} else {
						LatLng start;
						LatLng end = route.getLocation(searchRange.get());
						if (searchRange.get() == STANDARD_SEARCH_RANGE) {
							start = pointerLocation;
						} else {
							start = route.getLocation(searchRange.get() - (STANDARD_SEARCH_RANGE));
						}
						Ranking.getMilestonesByDistance(start, end, milestoneCategory, milestonesReceiver);
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

	// This method notifies the UI with the found milestone.
	private void notifyUISuccess(IMilestone milestone) {
		Log.d("NavigationModel", "entered notifyUISuccess");
		this.milestone = milestone;
		this.marker = map.showMilestone(this.milestone);

		Message message = mainHandler.obtainMessage();
		message.what = SignalType.MILESTONE_SUCCED;
		message.obj = this.milestone;
		mainHandler.sendMessage(message);
	}

	// This method notifies UI that algorithm failed to find stops.
	private void notifyUIFail() {
		Log.d("NavigationModel", "entered notifyUIFail");
		setMapState(Map.MapState.MOVING);
		Message message = mainHandler.obtainMessage();
		message.what = SignalType.MILESTONE_FAIL;
		mainHandler.sendMessage(message);
	}

	// This method updates the list of milestones.
	private void updateMilestonesList() {
		// List of milestones is empty
		if (this.milestones.size() == 0) {
			this.milestones = null;
			// Extend search range.
			searchRange.getAndAdd(STANDARD_SEARCH_RANGE);
			// Searchrange is longer tha route
			if(map.getRoute().getLocation(searchRange.get()) == null) {
				searchRange.set(STANDARD_SEARCH_RANGE);
				//milestoneAlgorithmStage.set(0);
				// Notify ui that no more milestons are available.
				notifyUIFail();
			} else {
				// Get new pause suggestions with bigger search range
				getPauseSuggestions(this.milestoneCategory);
			}
		// List of milestones contains only one object
		} else if(this.milestones.size() == 1) {
			// Extend search range.
			searchRange.getAndAdd(STANDARD_SEARCH_RANGE);
			IMilestone milestone = this.milestones.get(0);
			milestones.remove(0);
			this.milestones = null;
			notifyUISuccess(milestone);
		// List of milestones contains more than one object
		} else {
			IMilestone m = this.milestones.get(0);
			milestones.remove(0);
			notifyUISuccess(m);
		}
	}

	// Private method that sets mapstate via UI thread
	private void setMapState(final Map.MapState mapState) {
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				map.setMapState(mapState);
			}
		});
	}
}