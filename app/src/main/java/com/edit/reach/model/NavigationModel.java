package com.edit.reach.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.edit.reach.constants.Constants;
import com.edit.reach.constants.SignalType;
import com.edit.reach.model.interfaces.RouteListener;
import com.edit.reach.model.interfaces.SuggestionListener;
import com.edit.reach.system.VehicleSystem;
import com.edit.reach.utils.SuggestionUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Class that merges data from the vehicle and the map. The class finds optimal stops for the trip.
 * Created by: Tim Kerschbaumer
 * Project: REACH
 * Date: 2014-09-27
 * Time: 19:27
 * Last Edit: 2014-10-17
 * This class har the singleton pattern.
 */
public class NavigationModel implements Runnable, Observer, SuggestionListener {

	private final VehicleSystem vehicleSystem;
	private final Thread pipelineThread;
	private final Handler mainHandler;

	private Handler pipelineHandler;
	private Map map;

	private static NavigationModel navigationModel;

	private List<String> searchResults;

	/* --- CONSTANTS --- */
	private static final String PIPELINE_THREAD_NAME = "PipelineThread";

	// Private constructor
	private NavigationModel() {
		this.pipelineThread = new Thread(this, PIPELINE_THREAD_NAME);
		this.pipelineThread.start();

		this.vehicleSystem = new VehicleSystem();
		this.vehicleSystem.addObserver(this);

		this.mainHandler = new Handler(Looper.getMainLooper());
	}

	public static NavigationModel getInstance() {
		if (navigationModel == null) {
			navigationModel = new NavigationModel();
		}
		return navigationModel;
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

	/** Returns a map object.
	 * @return a Map
	 */
	public Map getMap() {
		return map;
	}

	public void setGoogleMap(GoogleMap googleMap) {
		if(this.map != null) {
			this.map = new Map(googleMap);
		}
	}

	@Override
	public void onGetSuccess(List<String> results) {
		searchResults = results;
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

	// This method must run on UI thread because of google map objects in Map class.
	/** Sets the route in the map.
	 * @param newRoute the route to be set.
	 */
	public void setRoute(final Route newRoute) {
		map.setRoute(newRoute);
        newRoute.addListener(new RouteListener() {
			@Override
			public void onInitialization(boolean success) {
                if(success) {
                    long routeTime = map.getRoute().getDuration();
                    long nmbrOfPauses = routeTime/ Constants.LEGAL_UPTIME_IN_SECONDS;

                    for(int i = 1; i < nmbrOfPauses; i++) {
                        Log.d("NavModel", "Adding pause: ");
                        map.getRoute().addPause(i*Constants.LEGAL_UPTIME_IN_SECONDS);
                    }
                } else {
                    // Failed initialization
                }
			}

			@Override
			public void onPauseAdded(Pause pause) {
				// TODO what here?
			}
		});
	}

	/** Do not call this method. It is called automatically when the observable changes.
	 * @param observable Not used
	 * @param data The id of the signal that initiated this update.
	 */
	@Override
	public synchronized void update(Observable observable, final Object data) {
		pipelineHandler.post(new Runnable() {
			@Override
			public void run() {
				// TODO
				Message message = Message.obtain(mainHandler);
				Log.d("THREAD", "Thread in update: " + Thread.currentThread().getName());

				if((Integer)data == SignalType.LOW_FUEL) {
					Log.d("UPDATE", "TYPE: LOW_FUEL");
					Log.d("GET", "Km to refuel: " + vehicleSystem.getKilometersUntilRefuel());

					// TODO
					// message.obj = vehicleSystem.getKilometersUntilRefuel();
					// message.what = SignalType.LOW_FUEL;
					// mainHandler.sendMessage(message);

				} else if ((Integer)data == SignalType.SHORT_TIME) {
					Log.d("UPDATE", "TYPE: SHORT_TIME");
					Log.d("GET", "Time until rest: " + vehicleSystem.getTimeUntilForcedRest());

					// TODO
					// message.obj = vehicleSystem.getTimeUntilForcedRest();
					// message.what = SignalType.SHORT_TIME;
					// mainHandler.sendMessage(message);

				} else if ((Integer)data == SignalType.SHORT_TO_SERVICE) {
					Log.d("UPDATE", "TYPE: SHORT_TO_SERVICE");
					Log.d("GET", "Km to service: " + vehicleSystem.getKilometersUntilService());

					// TODO
					// message.obj = vehicleSystem.getKilometersUntilService();
					// message.what = SignalType.SHORT_TO_SERVICE;
					// mainHandler.sendMessage(message);

				} else if ((Integer)data == SignalType.VEHICLE_STOPPED_OR_STARTED) {
					Log.d("UPDATE", "TYPE: VEHICLE_STOPPED_OR_STARTED");
					Log.d("GET", "Vehicle State: " + vehicleSystem.getVehicleState());

					message.obj = vehicleSystem.getVehicleState();
					message.what = SignalType.VEHICLE_STOPPED_OR_STARTED;
					mainHandler.sendMessage(message);

				} else if ((Integer)data == SignalType.VEHICLE_TOOK_FINAL_BREAK) {
					// TODO what to do when vehicle took a "final" break.
					Log.d("UPDATE", "TYPE: VEHICLE_TOOK_FINAL_BREAK");

				} else if ((Integer)data == SignalType.UPTIME_UPDATE) {
					Log.d("UPDATE", "TYPDE: UP_TIME_UPDATE");

					message.obj = vehicleSystem.getTimeUntilForcedRest();
					message.what = SignalType.UPTIME_UPDATE;
					mainHandler.sendMessage(message);

				} else if ((Integer)data == SignalType.FUEL_UPDATE) {
					Log.d("UPDATE", "TYPDE: FUEL_UPDATE");

					message.obj = vehicleSystem.getFuelLevel();
					message.what = SignalType.FUEL_UPDATE;
					mainHandler.sendMessage(message);

				} else {
					Log.d("TYPE ERROR", "Type error in update");
				}
			}
		});
	}
}
