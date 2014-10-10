package com.edit.reach.app;

import com.edit.reach.model.IMilestone;
import com.edit.reach.model.Milestone;
import com.edit.reach.model.MilestonesReceiver;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class RankingSystem implements ResponseHandler {

    MilestonesReceiver milestonesReceiver;

    public RankingSystem(MilestonesReceiver milestonesReceiver) {
        this.milestonesReceiver = milestonesReceiver;
    }

    public void getMilestones(LatLng centralPoint, double sideLength) {

        BoundingBox bbox = new BoundingBox(centralPoint, sideLength);

        try {
            URL url = WorldTruckerEndpoints.getMilestonesURL(bbox);
            Remote.get(url, this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

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
            e.printStackTrace();
        }

        milestonesReceiver.onMilestonesRecieved(milestones);
    }

    @Override
    public void onGetFail() {

    }
}
