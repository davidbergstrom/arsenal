package com.edit.reach.utils;

/**
 * A converter that converts time.
 * Created by: David Bergstrom
 * Project: arsenal
 * Date: 14-10-22
 * Time: 19.16
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
