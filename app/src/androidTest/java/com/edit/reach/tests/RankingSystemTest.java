package com.edit.reach.tests;

import android.util.Log;
import com.edit.reach.app.RankingSystem;
import com.edit.reach.model.IMilestone;
import com.edit.reach.model.MilestonesReceiver;
import com.google.android.gms.maps.model.LatLng;
import junit.framework.TestCase;

import java.util.ArrayList;

public class RankingSystemTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testGetMilestones() {
        Log.d("RankingTest", "Running test 1");
        RankingSystem r1 = new RankingSystem(new MilestonesReceiver() {
            @Override
            public void onMilestonesRecieved(ArrayList<IMilestone> milestones) {
                Log.d("RankingTest", "Success");
            }

            @Override
            public void onMilestonesGetFailed() {
                Log.d("RankingTest", "Fail");
            }
        });

        r1.getMilestones(new LatLng(62, 55), 100);
    }
}