package com.edit.reach.model;

import com.edit.reach.model.interfaces.IMilestone;
import com.edit.reach.utils.NavigationUtil;
import com.google.android.gms.maps.model.LatLng;

import static com.edit.reach.model.interfaces.IMilestone.Category;

public class RankingDistance extends RankingRating {
    private final LatLng driverPoint;

    public RankingDistance(Category category, LatLng driverPoint) {
        super(category);
        this.driverPoint = driverPoint;
    }

    @Override
    public int compare(IMilestone m1, IMilestone m2) {
        double distance1 = NavigationUtil.getDistance(m1.getLocation(), driverPoint);
        double distance2 = NavigationUtil.getDistance(m2.getLocation(), driverPoint);
        if (distance1 < distance2)
            return 1;
        if (distance2 > distance1)
            return -1;
        return 0;
    }
}
