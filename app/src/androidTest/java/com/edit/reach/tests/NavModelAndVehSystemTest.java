package com.edit.reach.tests;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.edit.reach.model.NavigationModel;
import com.edit.reach.constants.SignalType;
import junit.framework.TestCase;

/* This test class depends on simulator values */
public class NavModelAndVehSystemTest extends TestCase {
	// TODO unable to test this because of the AGA-Simulator...

	NavigationModel m;

	Handler mainHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message message) {
			if(message.what == SignalType.LOW_FUEL) {
				Log.d("Km-To-Refuel: ", message.obj + "");
			} else if(message.what == SignalType.VEHICLE_STOPPED_OR_STARTED) {
				Log.d("Vehicle-State: ", message.obj + "");
			} else if(message.what == SignalType.SHORT_TIME) {
				Log.d("Time-to-stop: ", message.obj + "");
			} else if(message.what == SignalType.SHORT_TO_SERVICE) {
				Log.d("Distance-to-service: ", message.obj + "");
			} else {
				Log.d("handleMessage", "Error");
			}
		}
	};

	public void setUp() throws Exception {
		super.setUp();

		m = new NavigationModel(mainHandler);
	}




}