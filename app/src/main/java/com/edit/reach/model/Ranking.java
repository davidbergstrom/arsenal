package com.edit.reach.model;

import android.util.Log;
import com.edit.reach.constants.GetStatus;
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

import static com.edit.reach.model.IMilestone.Category;

/**
 * This classed is used to deliver milestones from the
 * WorldTrucker API to an instance of MilestonesReceiver.
 */
public class Ranking {

    private static GetStatus status;

    private Ranking() {
        status = GetStatus.PENDING;
    }

    /**
     * Finds a list of milestones within a specified area
     * and category, ordered by rating.
     * @param driverPoint The driver's location
     * @param maxPoint The maximum point away from the driver
     * @param category A milestone category
     * @param milestonesReceiver The object that will receive the ordered list of milestones.
     */
    public static void getMilestonesByRank(LatLng driverPoint, LatLng maxPoint, Category category, final MilestonesReceiver milestonesReceiver) {
        BoundingBox bbox = new BoundingBox(driverPoint, maxPoint);
        RankingHelper rankingHelper = new RankingHelper(category);
        performGet(bbox, rankingHelper, milestonesReceiver);
    }

    /**
     * Finds a list of milestones within a specified area
     * and category, ordered by closest distance.
     * @param driverPoint The driver's location
     * @param maxPoint The maximum point away from the driver
     * @param category A milestone category
     * @param milestonesReceiver The object that will receive the ordered list of milestones.
     */
    public static void getMilestonesByDistance(LatLng driverPoint, LatLng maxPoint, Category category, final MilestonesReceiver milestonesReceiver) {
        BoundingBox bbox = new BoundingBox(driverPoint, maxPoint);
        RankingHelper rankingHelper = new RankingDistanceHelper(category, driverPoint);
        performGet(bbox, rankingHelper, milestonesReceiver);
    }

    /**
     * Finds all milestones within a specified square
     * @param milestonesReceiver The object that will receive the ordered list of milestones.
     * @param centralPoint The central point of the square
     * @param sideLength The square's side length
     */
    public static void getMilestones(final MilestonesReceiver milestonesReceiver, LatLng centralPoint, double sideLength) {
        BoundingBox bbox = new BoundingBox(centralPoint, sideLength);
        performGet(bbox, null, milestonesReceiver);
    }

    /**
     * Starts a HTTP get request via the Remote class.
     * @param bbox A square representing an area with milestones
     * @param rankingHelper An object helping to fix the desired list.
     * @param milestonesReceiver The object that will receive the ordered list of milestones.
     */
    private static void performGet(BoundingBox bbox, final RankingHelper rankingHelper, final MilestonesReceiver milestonesReceiver) {
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

		            if (rankingHelper != null) {
			            rankingHelper.removeUnwanted(milestones);
			            Collections.sort(milestones, rankingHelper);
		            }

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

    /**
     * Showing the status of the Ranking class.
     * @return The current status of the class
     */
    public static GetStatus getStatus() {
        return status;
    }

    /**
     * Resets the class's status
     */
    public static void reset() {
        status = GetStatus.PENDING;
    }
}
