package com.edit.reach.tests;

import android.test.InstrumentationTestCase;
import android.util.Log;
import com.edit.reach.model.Milestone;
import com.edit.reach.model.interfaces.IMilestone;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class MilestoneTest extends InstrumentationTestCase {

    private Milestone milestone;

    public void setUp() throws Exception {
        super.setUp();

        String milestoneXML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<id>192</id>"
                + "<geometry>"
                + "<type>Point</type>"
                + "<coordinates>"+ 62.23378 + "</coordinates>"
                + "<coordinates>"+ 25.9186515808105 + "</coordinates>"
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
            Log.d("MilestoneTest", "error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void testGetName() throws Exception {
        assertEquals("Arsenal FC", milestone.getName());
    }

    public void testGetDescription() throws Exception {
        assertEquals("The world's greatest team", milestone.getDescription());
    }

    public void testGetCategory() throws Exception {
        assertEquals(IMilestone.Category.GASSTATION, milestone.getCategory());
    }

    public void testGetRank() throws Exception {
        assertEquals(2, milestone.getRank());
    }

    public void testGetLocation() throws Exception {
        assertEquals(new LatLng(62.23378, 25.9186515808105), milestone.getLocation());
    }
}