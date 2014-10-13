package com.edit.reach.model;

import android.util.Log;
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

public class Ranking implements ResponseHandler {

    MilestonesReceiver milestonesReceiver;

    public Ranking(MilestonesReceiver milestonesReceiver) {
        this.milestonesReceiver = milestonesReceiver;
    }

    public void getMilestones(LatLng centralPoint, double sideLength) {

        BoundingBox bbox = new BoundingBox(centralPoint, sideLength);

        try {
            URL url = WorldTruckerEndpoints.getMilestonesURL(bbox);
            Log.d("RankingTest", "getMileStones");
            Remote.get(url, this);
        } catch (MalformedURLException e) {
            Log.d("RankingTest", "Exception");
            e.printStackTrace();
        }
    }

    @Override
    public void onGetSuccess(JSONObject json) {
        ArrayList<IMilestone> milestones = new ArrayList<IMilestone>();
        Log.d("RankingTest", "Inside onGetSucces (REMOTE)");

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

/*        Log.d("RankingTest", "This is where I want to return the milestones");*/
        milestonesReceiver.onMilestonesRecieved(milestones);
    }

    @Override
    public void onGetFail() {
        Log.d("RankingTest", "This is where I fail!");
        milestonesReceiver.onMilestonesGetFailed();
    }
}
