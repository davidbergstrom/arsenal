package com.edit.reach.utils;

import com.edit.reach.constants.UniversalConstants;

/**
 * Created by iDavid on 14-10-23.
 */
public class TimeConvert {

	public TimeConvert() {}

	public static String convertTime(int inTime) {
		int hours;
		int minutes;

		hours = inTime / 60;
		minutes = inTime % 60;

		return  hours + " h " + minutes + " min";
	}

}
