package com.edit.reach.app;
import com.google.android.gms.maps.model.LatLng;

/**
 * A square representing a geographical area.
 */
public class BoundingBox {
    LatLng bottomLeft;
    LatLng topRight;

    public BoundingBox(LatLng center, double sideLength) {
        double d = sideLength / 2;

        bottomLeft = new LatLng(center.latitude - d, center.longitude - d);
        topRight = new LatLng(center.latitude + d, center.longitude + d);
    }

    LatLng getBottomLeft() {
       return bottomLeft;
    }

    LatLng getTopRight() {
        return topRight;
    }
}
