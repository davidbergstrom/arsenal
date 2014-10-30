package com.edit.reach.tests;

import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import com.edit.reach.model.Milestone;
import com.edit.reach.model.IMilestone;
import com.edit.reach.system.GoogleMapsEndpoints;
import com.google.android.gms.maps.model.LatLng;
import junit.framework.TestCase;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GoogleMapsEndpointsTest extends TestCase {

    GoogleMapsEndpoints googleMapsEndpoints;

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    @SmallTest
    public void testMakeURLGeocode() throws Exception {
        URL actual = googleMapsEndpoints.makeURLGeocode("Soldathemsgatan 20, 41528, Göteborg");
        URL expected = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=soldathemsgatan+20,+41528,+göteborg&key=AIzaSyCqs-SMMT3_BIzMsPr-wsWqsJTthTgFUb8");
        assertEquals(expected,actual);
    }

    @SmallTest
    public void testMakeURLPlaces() throws Exception {
        URL actual = googleMapsEndpoints.makeURLGeocode("Soldathemsgatan");
        URL expected = new URL("https://maps.googleapis.com/maps/api/place/autocomplete/json?input=soldathemsgatan&sensor=true&key=AIzaSyCqs-SMMT3_BIzMsPr-wsWqsJTthTgFUb8");
        assertEquals(expected,actual);
    }

}