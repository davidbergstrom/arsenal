package com.edit.reach.tests;

import android.util.Log;
import com.edit.reach.model.Leg;
import com.edit.reach.model.Pause;
import com.edit.reach.model.Route;
import com.edit.reach.model.Step;
import com.edit.reach.model.interfaces.RouteListener;
import com.google.android.gms.maps.model.LatLng;
import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RouteTest extends TestCase {
    private Route route;
    private String DEBUG_TAG = "RouteTest";

    public void setUp() throws Exception {
        super.setUp();

        final CountDownLatch signal = new CountDownLatch(1);
        route = new Route("Trollhattan", "Goteborg");
        route.addListener(new RouteListener() {
            @Override
            public void onInitialization(boolean success) {
                signal.countDown();// notify the count down latch
            }

            @Override
            public void onPauseAdded(Pause pause) {
                assertTrue(route.getPauses().size() > 0);
                assertNotNull(pause.getLocation());
            }

            @Override
            public void onRouteFinished(Route finishedRoute) {

            }

            @Override
            public void onLegFinished(Leg finishedLeg) {

            }

            @Override
            public void onStepFinished(Step finishedStep) {

            }
        });
        Log.d(DEBUG_TAG, "Waiting for init");
        signal.await(30, TimeUnit.SECONDS);
        Log.d(DEBUG_TAG, "Init done!");
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetDuration() throws Exception {
        assertTrue(route.getDuration() < 60 * 60 * 2);
    }

    public void testGetDistance() throws Exception {
        assertTrue(route.getDistance() < 120);
    }

    public void testAddPause() throws Exception {
        route.addPause(100);
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