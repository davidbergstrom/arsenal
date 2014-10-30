package com.edit.reach.utils;

/**
 * A converter that converts time.
 * Created by: David Bergstrom
 * Project: Milestone
 * Date: 14-10-22
 * Time: 19.16
 */
public final class TimeConvert {

	private TimeConvert() {}

	/**
	 * Divides minutes into hours and minutes.
	 * @param minutesToConvert Minutes to convert.
	 */
	public static final String convertToHoursAndMinutes(int minutesToConvert) {
		int hours = minutesToConvert / 60;
		int minutes = minutesToConvert % 60;

		return  hours + " h " + minutes + " min";
	}

}
