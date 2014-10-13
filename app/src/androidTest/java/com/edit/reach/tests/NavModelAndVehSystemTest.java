package com.edit.reach.tests;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.edit.reach.model.NavigationModel;
import junit.framework.TestCase;

/* This test class depends on simulator values */
public class NavModelAndVehSystemTest extends TestCase {

	NavigationModel m;

	Handler mainHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message message) {
			// TODO test here.
		}
	};

	public void setUp() throws Exception {
		super.setUp();
		m = new NavigationModel(mainHandler);
	}




}