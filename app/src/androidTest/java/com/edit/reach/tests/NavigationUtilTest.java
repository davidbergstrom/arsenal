package com.edit.reach.tests;

import android.test.suitebuilder.annotation.SmallTest;
import com.edit.reach.utils.NavigationUtil;
import junit.framework.TestCase;

import java.net.URL;

public class NavigationUtilTest extends TestCase {

    NavigationUtil navigationUtil;

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /* Waiting for Milestones before a test can be made
    @SmallTest
    public void testMakeURL1() throws Exception {
        LatLng source = new LatLng(58.3787247,12.3194581); //From Vänersborg, Hamngatan 5b
        LatLng dest = new LatLng(57.6886404,11.9773242); //To Göteborg, Maskingränd 2
        LatLng waypoint = new LatLng(58.3541,11.812722); //Via Torp Köpcentrum
        List<LatLng> listOfWaypoints = new ArrayList<LatLng>();
        listOfWaypoints.add(waypoint);

        URL actual = navigationUtils.makeURL(source, dest, listOfWaypoints, true);
        URL expected = new URL("http://maps.googleapis.com/maps/api/directions/json?origin=58.3787247,12.3194581&destination=57.6886404,11.9773242&waypoints=optimize:true|58.3541,11.812722&sensor=false&mode=driving&alternatives=true&language=EN");
        assertEquals(expected, actual);
    }*/

    @SmallTest
    public void testMakeURL2() throws Exception {
        URL actual = navigationUtil.makeURL("Soldathemsgatan 20, 41528, Göteborg");
        URL expected = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=soldathemsgatan+20,+41528,+göteborg&key=AIzaSyCqs-SMMT3_BIzMsPr-wsWqsJTthTgFUb8");
        assertEquals(expected,actual);
    }
}