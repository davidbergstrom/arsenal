package com.edit.reach.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.Observable;
import java.util.Observer;

/**
 * Class that merges data from the vehicle and the map. The class finds optimal stops for the trip.
 * Created by: Tim Kerschbaumer
 * Project: REACH
 * Date: 2014-09-27
 * Time: 19:27
 * Last Edit: 2014-10-13
 */
public class NavigationModel implements Runnable, Observer {

	private VehicleSystem vehicleSystem;
	private Map map;

	private Handler mainHandler;
	private Handler pipelineHandler;
	private Thread pipelineThread;

	/* --- CONSTANTS --- */
	private static final String PIPELINE_THREAD_NAME = "PipelineThread";

	/** Constructor
	 * @param googleMap a GoogleMap
	 * @param mainHandler a handler created on the main Looper thread.
	 */
	public NavigationModel(GoogleMap googleMap, Handler mainHandler) {
		pipelineThread = new Thread(this, PIPELINE_THREAD_NAME);
		pipelineThread.start();

		vehicleSystem = new VehicleSystem();
		vehicleSystem.addObserver(this);

		map = new Map(googleMap);
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

	/** Returns a IMilestone matching the lat and longitude.
	 * @param latLng a LatLng with the latitude and longitude.
	 * @return a IMilestone, null if no milestone is not found.
	 */
	public IMilestone getMatchedMilestone(final LatLng latLng) {
		return map.getMilestone(latLng);
	}

	// This method must run on UI thread because of map objects... :'(
	/** Sets the route in the map.
	 * @param newRoute the route to be set.
	 */
    private RouteListener r = new RouteListener() {
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
    };
	public void setRoute(final Route newRoute) {
        newRoute.addListener(r);

		map.setRoute(newRoute);
        if(newRoute.isInitialized()){
            Log.d("NavModel", "Adding pauses.");
            long routeTime = map.getRoute().getDuration();
            long nmbrOfPauses = routeTime/VehicleSystem.getLegalUptimeInSeconds();

            for(int i = 1; i < nmbrOfPauses; i++) {
                Log.d("NavModel", "Adding pause...");
                map.getRoute().addPause(i*VehicleSystem.getLegalUptimeInSeconds());
            }
        }


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
				Log.d("THREAD", "Thread in update: " + Thread.currentThread().getName());

				if(data == SignalType.LOW_FUEL) {
					Log.d("UPDATE", "TYPE: LOW_FUEL");
					Log.d("GET", "Km to refuel: " + vehicleSystem.getKilometersUntilRefuel());

				} else if (data == SignalType.SHORT_TIME) {
					Log.d("UPDATE", "TYPE: SHORT_TIME");
					Log.d("GET", "Time until rest: " + vehicleSystem.getTimeUntilForcedRest());

				} else if (data == SignalType.SHORT_TO_SERVICE) {
					Log.d("UPDATE", "TYPE: SHORT_TO_SERVICE");
					Log.d("GET", "Km to service: " + vehicleSystem.getKilometersUntilService());

				} else if (data == SignalType.VEHICLE_STOPPED_OR_STARTED) {
					Log.d("UPDATE", "TYPE: VEHICLE_STOPPED_OR_STARTED");
					Log.d("GET", "Vehicle State: " + vehicleSystem.getVehicleState());

					// TODO Used for sending messages to UI thread
					// Message message = Message.obtain(mainHandler);
					// message.obj = vehicleSystem.getVehicleState();
					// message.what = 1;
					//  mainHandler.sendMessage(message);

				} else {
					Log.d("TYPE ERROR", "Type error in update");
				}
			}
		});
	}
}
