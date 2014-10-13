package com.edit.reach.tests;

import android.util.Log;
import com.edit.reach.model.Ranking;
import com.edit.reach.model.interfaces.IMilestone;
import com.edit.reach.model.interfaces.MilestonesReceiver;
import com.google.android.gms.maps.model.LatLng;
import junit.framework.TestCase;

import java.util.ArrayList;

public class RankingTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testGetMilestones() {
        Log.d("RankingTest", "Running test 1");
        Ranking r1 = new Ranking(new MilestonesReceiver() {
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