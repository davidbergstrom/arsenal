package com.edit.reach.app;

import com.edit.reach.model.IMilestone;
import com.edit.reach.model.MilestonesReceiver;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class RankingSystem implements ResponseHandler {

    MilestonesReceiver milestonesReceiver;

    private RankingSystem(MilestonesReceiver milestonesReceiver) {
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
        milestonesReceiver.onMilestonesRecieved(milestones);
    }

    @Override
    public void onGetFail() {

    }
}
