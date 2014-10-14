package com.edit.reach.tests;

import android.util.Log;
import com.edit.reach.constants.GetStatus;
import com.edit.reach.model.Milestone;
import com.edit.reach.model.Ranking;
import com.edit.reach.model.interfaces.IMilestone;
import com.edit.reach.model.interfaces.MilestonesReceiver;
import com.google.android.gms.maps.model.LatLng;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;

public class RankingTest extends TestCase {

    private HashMap<Integer, ArrayList<IMilestone>> milestonesLists;
    private boolean failed;

    public void setUp() throws Exception {
        super.setUp();
        Ranking.reset();
        failed = false;
    }

    public void testLargeRadius() {

        Ranking.getMilestones(new MilestonesReceiver() {
            @Override
            public void onMilestonesRecieved(ArrayList<IMilestone> ms) {
                milestonesLists = new HashMap<Integer, ArrayList<IMilestone>>();
                milestonesLists.put(1, ms);
            }

            @Override
            public void onMilestonesGetFailed() {
                failed = true;
            }
        }, new LatLng(62, 55), 100);

        while(Ranking.getStatus() == GetStatus.RUNNING) {
            // Waiting for Remote to finsih
        }

        Log.d("RankingTest", "testLargeRadius status is FINISHED");

        if (Ranking.getStatus() == GetStatus.SUCCEEDED) {
            assertNotNull(milestonesLists);

            if (milestonesLists.get(1).size() > 0) {
               assertSame(Milestone.class, milestonesLists.get(1).get(0).getClass());
            }
        } else if (Ranking.getStatus() == GetStatus.FAILED) {
           assertTrue(failed);
        }

        Log.d("RankingTest", "testLargeRadius is done");
    }

    public void testZeroRadius() {
        Log.d("RankingTest", "starting testZeroRadius");
        Ranking.getMilestones(new MilestonesReceiver() {
            @Override
            public void onMilestonesRecieved(ArrayList<IMilestone> ms) {
                milestonesLists = new HashMap<Integer, ArrayList<IMilestone>>();
                milestonesLists.put(2, ms);
            }

            @Override
            public void onMilestonesGetFailed() {
                failed = true;
            }
        }, new LatLng(62, 55), 0);

        while(Ranking.getStatus() == GetStatus.RUNNING) {
            // Waiting for Remote to finsih
        }
        Log.d("RankingTest", "testZeroRadius status is FINISHED");

        if (Ranking.getStatus() == GetStatus.SUCCEEDED) {
            Log.d("RankingTest", "SUCCEEDED");
            assertNotNull(milestonesLists);

            int results = milestonesLists.get(2).size();

            if (results > 0) {
                assertSame(Milestone.class, milestonesLists.get(1).get(0).getClass());
            } else {
                Log.d("RankingTest", "0 results back");
            }
        } else if (Ranking.getStatus() == GetStatus.FAILED) {
            Log.d("RankingTest", "FAILED");
            assertTrue(failed);
        } else {
            Log.d("RankingTest", "" + Ranking.getStatus());
        }
    }
}