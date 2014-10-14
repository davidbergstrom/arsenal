package com.edit.reach.model;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import junit.framework.TestCase;

public class RouteTest extends TestCase {
    private Route route;

    public void setUp() throws Exception {
        route = new Route("Trollhattan", "Goteborg");
        route.addListener(new RouteListener() {
            @Override
            public void onInitialization() {
                assertTrue(route.isInitialized());
            }

            @Override
            public void onPauseAdded(LatLng pauseLocation) {
                assertTrue(route.getPauses().size() > 0);
                assertNotNull(pauseLocation);
            }
        });
        while(!route.isInitialized()){
            Log.d("RouteTest", "Waiting");
            Thread.sleep(100);
        }
    }

    public void testGetDuration() throws Exception {
        assertNotNull(route.getDuration());
    }

    public void testGetDistance() throws Exception {
        assertTrue(route.getDistance() < 120);
    }

    public void testAddPause() throws Exception {
        route.addPause(100);
    }

    public void testAddPause1() throws Exception {
        route.addPause(40.0);
    }

    public void testGetPauses() throws Exception {

    }

    public void testRemoveAllPauses() throws Exception {

    }

    public void testGetDestination() throws Exception {
        assertNotNull(route.getDestination());
    }

    public void testGetOrigin() throws Exception {
        assertNotNull(route.getOrigin());
    }

    public void testIsInitialized() throws Exception {
        assertTrue(route.isInitialized());
    }
}