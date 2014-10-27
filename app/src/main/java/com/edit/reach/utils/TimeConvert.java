package com.edit.reach.utils;

/**
 * A converter that converts time.
 */
public class TimeConvert {

	public TimeConvert() {}

	/**
	 * Divides minutes into hours and minutes.
	 * @param inTime Minutes to convert.
	 */
	public static String convertTime(int inTime) {
		int hours;
		int minutes;

		hours = inTime / 60;
		minutes = inTime % 60;

		return  hours + " h " + minutes + " min";
	}

}
