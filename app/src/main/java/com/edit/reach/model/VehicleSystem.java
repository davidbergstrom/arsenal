package com.edit.reach.model;

import android.swedspot.automotiveapi.AutomotiveSignal;
import com.swedspot.automotiveapi.AutomotiveFactory;
import com.swedspot.automotiveapi.AutomotiveListener;
import com.swedspot.automotiveapi.AutomotiveManager;
import com.swedspot.vil.distraction.DriverDistractionLevel;
import com.swedspot.vil.distraction.DriverDistractionListener;
import com.swedspot.vil.policy.AutomotiveCertificate;

import java.util.Date;
import java.util.Timer;

/**
 * Created by: Tim Kerschbaumer
 * Project: REACH
 * Date: 2014-09-27
 * Time: 19:28
 */
// TODO Everything that has to do with the stationairy view
public class VehicleSystem {
	private final AutomotiveCertificate automotiveCertificate = new AutomotiveCertificate(null);

	private final DriverDistractionListener driverDistractionListener = new DriverDistractionListener() {
		@Override
		public void levelChanged(DriverDistractionLevel driverDistractionLevel) {

		}
	};

	private final AutomotiveListener automotiveListener = new AutomotiveListener() {
		@Override
		public void receive(AutomotiveSignal automotiveSignal) {

		}

		@Override
		public void timeout(int i) {

		}

		@Override
		public void notAllowed(int i) {

		}
	};

	private final AutomotiveManager automotiveManager = AutomotiveFactory.createAutomotiveManagerInstance(automotiveCertificate, automotiveListener, driverDistractionListener);

	private final Timer timer;

	private Date time;

	private boolean isDriving;

	public VehicleSystem() {
		this.timer = new Timer();
	}

	// TODO Not void
	public void getFuel() {
	}

	// TODO Not void
	public void getUptime() {
	}

	// TODO Not void
	public void getLatestDownTime() {
	}

	// TODO Not void
	public void getCurrentVelocity() {
	}

	// TODO Not void
	public void getMeanVelocity(double startTime, double endTime) {
	}

	public boolean isDriving() {
		return isDriving;
	}

}
