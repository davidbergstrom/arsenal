package com.edit.reach.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.edit.reach.constants.SignalType;
import com.edit.reach.model.interfaces.IMilestone;
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
 * Last Edit: 2014-10-14
 */
public class NavigationModel implements Runnable, Observer, SuggestionListener {

	private VehicleSystem vehicleSystem;
	private Map map;

	private Handler mainHandler;
	private Handler pipelineHandler;
	private Thread pipelineThread;

	private List<String> searchResults;

	/* --- CONSTANTS --- */
	private static final String PIPELINE_THREAD_NAME = "PipelineThread";

	/** Constructor that does not initialize map. Used for testing.
	 * Other usage and calling for methods using map will result in NullPointer.
	 * @param mainHandler a handler created on the main Looper thread.
	 */
	public NavigationModel(Handler mainHandler) {
		pipelineThread = new Thread(this, PIPELINE_THREAD_NAME);
		pipelineThread.start();

		vehicleSystem = new VehicleSystem();
		vehicleSystem.addObserver(this);

		this.mainHandler = mainHandler;
	}

	/** Constructor
	 * @param googleMap a GoogleMap
	 * @param mainHandler a handler created on the main Looper thread.
	 */
	public NavigationModel(GoogleMap googleMap, Handler mainHandler) {
		this(mainHandler);
		map = new Map(googleMap);
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

	/** Returns a IMilestone matching the lat and longitude.
	 * @param latLng a LatLng with the latitude and longitude.
	 * @return a IMilestone, null if no milestone is not found.
	 */
	public IMilestone getMatchedMilestone(final LatLng latLng) {
		return map.getMilestone(latLng);
	}

	// This method must run on UI thread because of google map objects in Map class.
	/** Sets the route in the map.
	 * @param newRoute the route to be set.
	 */
	public void setRoute(final Route newRoute) {
		map.setRoute(newRoute);
        newRoute.addListener(new RouteListener() {
			@Override
			public void onInitialization() {
				Log.d("NavModel", "Adding pauses.");
				//map.getRoute().addPause(vehicleSystem.getKilometersUntilRefuel());

				long routeTime = map.getRoute().getDuration();
				long nmbrOfPauses = routeTime/VehicleSystem.getLegalUptimeInSeconds();

				for(int i = 1; i < nmbrOfPauses; i++) {
					Log.d("NavModel", "Adding pause: ");
					map.getRoute().addPause(i*VehicleSystem.getLegalUptimeInSeconds());
				}
			}

			@Override
			public void onPauseAdded(LatLng pauseLocation) {
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
				Message message = Message.obtain(mainHandler);
				Log.d("THREAD", "Thread in update: " + Thread.currentThread().getName());

				if((Integer)data == SignalType.LOW_FUEL) {
					Log.d("UPDATE", "TYPE: LOW_FUEL");
					Log.d("GET", "Km to refuel: " + vehicleSystem.getKilometersUntilRefuel());

					// TODO
					message.obj = vehicleSystem.getKilometersUntilRefuel();
					message.what = SignalType.LOW_FUEL;
					mainHandler.sendMessage(message);

				} else if ((Integer)data == SignalType.SHORT_TIME) {
					Log.d("UPDATE", "TYPE: SHORT_TIME");
					Log.d("GET", "Time until rest: " + vehicleSystem.getTimeUntilForcedRest());

					// TODO
					message.obj = vehicleSystem.getTimeUntilForcedRest();
					message.what = SignalType.SHORT_TIME;
					mainHandler.sendMessage(message);

				} else if ((Integer)data == SignalType.SHORT_TO_SERVICE) {
					Log.d("UPDATE", "TYPE: SHORT_TO_SERVICE");
					Log.d("GET", "Km to service: " + vehicleSystem.getKilometersUntilService());

					// TODO
					message.obj = vehicleSystem.getKilometersUntilService();
					message.what = SignalType.SHORT_TO_SERVICE;
					mainHandler.sendMessage(message);

				} else if ((Integer)data == SignalType.VEHICLE_STOPPED_OR_STARTED) {
					Log.d("UPDATE", "TYPE: VEHICLE_STOPPED_OR_STARTED");
					Log.d("GET", "Vehicle State: " + vehicleSystem.getVehicleState());

					// TODO
					message.obj = vehicleSystem.getVehicleState();
					message.what = SignalType.VEHICLE_STOPPED_OR_STARTED;
					mainHandler.sendMessage(message);

				} else {
					Log.d("TYPE ERROR", "Type error in update");
				}
			}
		});
	}
}
