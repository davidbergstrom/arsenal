package com.edit.reach.tests;

import com.edit.reach.utils.TimeConvert;
import junit.framework.TestCase;

public class TimeConvertTest extends TestCase {

	int m1 = 60;
	int m2 = 120;
	int m3 = 1540;
	int m4 = 3;
	int m5 = 0;

	public void testConvertToHoursAndMinutes() throws Exception {
		String s1 = TimeConvert.convertToHoursAndMinutes(m1);
		String s2 = TimeConvert.convertToHoursAndMinutes(m2);
		String s3 = TimeConvert.convertToHoursAndMinutes(m3);
		String s4 = TimeConvert.convertToHoursAndMinutes(m4);
		String s5 = TimeConvert.convertToHoursAndMinutes(m5);
		assertTrue(s1.equals(1 + " h " + 0 + " min") == true);
		assertTrue(s2.equals(2 + " h " + 0 + " min") == true);
		assertTrue(s3.equals(25 + " h " + 40 + " min") == true);
		assertTrue(s4.equals(0 + " h " + 3 + " min") == true);
		assertTrue(s5.equals(0 + " h " + 0 + " min") == true);
	}
}