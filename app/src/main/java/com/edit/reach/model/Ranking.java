package com.edit.reach.model;

import android.util.Log;
import com.edit.reach.constants.GetStatus;
import com.edit.reach.model.interfaces.IMilestone;
import com.edit.reach.model.interfaces.MilestonesReceiver;
import com.edit.reach.system.Remote;
import com.edit.reach.system.ResponseHandler;
import com.edit.reach.system.WorldTruckerEndpoints;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.edit.reach.model.interfaces.IMilestone.Category;

public class Ranking {

    private static GetStatus status;

    private Ranking() {
        status = GetStatus.PENDING;
    }

    public static void getMilestones(LatLng bottomLeftPoint, LatLng topLeftPoint, Category category, final MilestonesReceiver milestonesReceiver) {
        BoundingBox bbox = new BoundingBox(bottomLeftPoint, topLeftPoint);
        performGet(bbox, category, milestonesReceiver);
    }

    public static void getMilestones(final MilestonesReceiver milestonesReceiver, LatLng centralPoint, double sideLength) {
        BoundingBox bbox = new BoundingBox(centralPoint, sideLength);
        performGet(bbox, null, milestonesReceiver);
    }

    private static void performGet(BoundingBox bbox, final Category category, final MilestonesReceiver milestonesReceiver) {
        status = GetStatus.RUNNING;

        try {
            URL url = WorldTruckerEndpoints.getMilestonesURL(bbox);
            Remote.get(url, new ResponseHandler() {

                @Override
                public void onGetSuccess(JSONObject json) {
                    ArrayList<IMilestone> milestones = new ArrayList<IMilestone>();

                    try {
                        JSONObject paging = json.getJSONObject("paging");
                        int milestonesCount = paging.getInt("objectcount");


                        if (milestonesCount > 0) {
                            JSONArray features = json.getJSONArray("features");

                            for (int i = 0; i < milestonesCount; ++i) {
                                IMilestone milestone = new Milestone(features.getJSONObject(i));
                                milestones.add(milestone);
                            }
                        }
                    } catch (JSONException e) {
                        Log.d("RankingTest", e.getMessage());
                    }

                    if (category != null) {
                        for (int i = 0; i < milestones.size(); i++) {
                            if (!milestones.get(i).hasCategory(category)) {
                               milestones.remove(milestones.get(i));
                            }
                        }
                    }

                    Collections.sort(milestones, new Comparator<IMilestone>() {
                        @Override
                        public int compare(IMilestone m1, IMilestone m2) {
                            if (m1.getRank() > m2.getRank())
                                return 1;
                            if (m1.getRank() < m2.getRank())
                                return -1;
                            return 0;
                        }
                    });

                    milestonesReceiver.onMilestonesRecieved(milestones);

                    status = GetStatus.SUCCEEDED;
                }

                @Override
                public void onGetFail() {
                    milestonesReceiver.onMilestonesGetFailed();
                    status = GetStatus.FAILED;
                }
            });
        } catch (MalformedURLException e) {
            Log.d("RankingTest", e.getMessage());
        }
    }

    public static GetStatus getStatus() {
        return status;
    }

    public static void reset() {
        status = GetStatus.PENDING;
    }

}
