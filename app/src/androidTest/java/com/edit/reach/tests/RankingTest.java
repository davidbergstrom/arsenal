package com.edit.reach.tests;

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
        failed = false;
    }

    public void testGetMilestones() {

        Ranking r1 = new Ranking(new MilestonesReceiver() {
            @Override
            public void onMilestonesRecieved(ArrayList<IMilestone> ms) {
                milestonesLists = new HashMap<Integer, ArrayList<IMilestone>>();
                milestonesLists.put(1, ms);
            }

            @Override
            public void onMilestonesGetFailed() {
                failed = true;
            }
        });

        r1.getMilestones(new LatLng(62, 55), 60);

        while(!r1.isFinished()) {
            // Waiting for Remote to finsih
        }

        if (!failed) {
            assertNotNull(milestonesLists);

            if (milestonesLists.get(1).size() > 0) {
               assertSame(Milestone.class, milestonesLists.get(1).get(0).getClass());
            }
        }
    }
}