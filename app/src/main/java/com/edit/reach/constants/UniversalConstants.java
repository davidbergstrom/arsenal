package com.edit.reach.constants;

/**
 * Class that holds constants.
 * Created by: Tim Kerschbaumer
 * Project: arsenal
 * Date: 2014-10-16
 * Time: 17:20
 */
public class UniversalConstants {
	// Multiply with this to convert (FROM -> TO)
	public static final double NANOSECONDS_TO_SECONDS = 1.0/1000000000;
	public static final double SECONDS_TO_HOURS = 1.0/3600;
	public static final double SECONDS_TO_MINUTES = 1.0/60;
	public static final double HOURS_TO_SECONDS = 60*60;

	// The maximum number of seconds to drive before a 45 minute break.
	public static final long LEGAL_UPTIME_IN_SECONDS = 16200;

	// The time in seconds that a break has to be after a 16200 second drive.
	public static final long BREAKTIME_IN_SECONDS = 2700;

    // 10% fuel left.
    public static final float FUEL_THRESHOLD = 10f;

    // 15 minutes before timestop.
    public static final int TIME_THRESHOLD = 900;
}
