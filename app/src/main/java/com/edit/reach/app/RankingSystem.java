package com.edit.reach.app;

import com.edit.reach.model.Milestone;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class RankingSystem implements ResponseHandler {

    private RankingSystem() {

    }

    public ArrayList<Milestone> getMilestones(LatLng centralPoint, double sideLength) {
        ArrayList<Milestone> milestones = new ArrayList<Milestone>();

        BoundingBox bbox = new BoundingBox(centralPoint, sideLength);

        try {
            URL url = WorldTruckerEndpoints.getMilestonesURL(bbox);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // https://api.worldtrucker.com/v1/
        // tip?bbox=26.23952479477298%2C-94.9609375%2C57.77762234415841%2C12.96875
        // &categories=

        return milestones;
    }

    @Override
    public void onGetSuccess(JSONObject json) {

    }

    @Override
    public void onGetFail() {

    }
}
