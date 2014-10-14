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

public class Ranking {

    private static GetStatus status;

    private Ranking() {
        status = GetStatus.PENDING;
    }

    public static void getMilestones(final MilestonesReceiver milestonesReceiver, LatLng centralPoint, double sideLength) {
        status = GetStatus.RUNNING;

        BoundingBox bbox = new BoundingBox(centralPoint, sideLength);

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

                    milestonesReceiver.onMilestonesRecieved(milestones);

                    status = GetStatus.FINISHED;
                }

                @Override
                public void onGetFail() {
                    milestonesReceiver.onMilestonesGetFailed();
                    status = GetStatus.FINISHED;
                }
            });
        } catch (MalformedURLException e) {
            Log.d("RankingTest", e.getMessage());
        }
    }

    public static GetStatus getStatus() {
        return status;
    }

}
