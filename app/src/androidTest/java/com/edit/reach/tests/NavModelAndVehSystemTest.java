package com.edit.reach.tests;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.edit.reach.model.NavigationModel;
import com.edit.reach.model.SignalType;
import junit.framework.TestCase;

/* This test class depends on simulator values */
public class NavModelAndVehSystemTest extends TestCase {

	NavigationModel m;

	Handler mainHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message message) {
			if(message.what == SignalType.LOW_FUEL) {
				// TODO
			} else if(message.what == SignalType.VEHICLE_STOPPED_OR_STARTED) {
				// TODO
			} else if(message.what == SignalType.SHORT_TIME) {
				// TODO
			} else if(message.what == SignalType.SHORT_TO_SERVICE) {
				// TODO
			} else {
				Log.d("handleMessage", "Error");
				assertTrue(1 == 2);
			}
		}
	};

	public void setUp() throws Exception {
		super.setUp();
		m = new NavigationModel(mainHandler);
	}




}