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
    public void testMakeURL1() throws Exception {

        IMilestone milestone = null;

        LatLng source = new LatLng(58.3787247,12.3194581); //From Vänersborg, Hamngatan 5b
        LatLng dest = new LatLng(57.6886404,11.9773242); //To Göteborg, Maskingränd 2

        String milestoneXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<id>192</id>"
                + "<geometry>"
                + "<type>Point</type>"
                + "<coordinates>"+ 58.3541 + "</coordinates>" //Torp köpcenter
                + "<coordinates>"+ 11.812722 + "</coordinates>"
                + "<coordinates>"+ 0.0 + "</coordinates>"
                + "<coordinates>"+ 1366281091 + "</coordinates>"
                + "</geometry>"
                + "<properties>"
                + "<owner>" + 41043 + "</owner>"
                + "<owner_firstname>Marko</owner_firstname>"
                + "<owner_lastname>Kauppinen</owner_lastname>"
                + "<name>Arsenal FC</name>"
                + "<description>The world's greatest team</description>"
                + "<category>" + 3 + "</category>"
                + "<rating>" + 2 + "</rating>"
                + "</properties>"
                + "<type>Feature</type>";

        JSONObject jsonObj = null;

        try {
            jsonObj = XML.toJSONObject(milestoneXML);
            milestone = new Milestone(jsonObj);
        } catch (JSONException e) {
            Log.d("GoogleMapsEndpointsTest", "error: " + e.getMessage());
            e.printStackTrace();
        }

        List<IMilestone> listOfWaypoints = new ArrayList<IMilestone>();
        listOfWaypoints.add(milestone);

        URL actual = googleMapsEndpoints.makeURLDirections(source, dest, listOfWaypoints, true);
        URL expected = new URL("http://maps.googleapis.com/maps/api/directions/json?origin=58.3787247,12.3194581&destination=57.6886404,11.9773242&waypoints=optimize:true|58.3541,11.812722&sensor=false&mode=driving&alternatives=true&language=EN");
        assertEquals(expected, actual);
    }

    @SmallTest
    public void testMakeURL2() throws Exception {
        URL actual = googleMapsEndpoints.makeURLGeocode("Soldathemsgatan 20, 41528, Göteborg");
        URL expected = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=soldathemsgatan+20,+41528,+göteborg&key=AIzaSyCqs-SMMT3_BIzMsPr-wsWqsJTthTgFUb8");
        assertEquals(expected,actual);
    }

}