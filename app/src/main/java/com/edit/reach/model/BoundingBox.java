package com.edit.reach.model;
import com.google.android.gms.maps.model.LatLng;

/**
 * A square representing a geographical area.
 */
public class BoundingBox {
    private final LatLng bottomLeft;
    private final LatLng topRight;

    public BoundingBox(LatLng center, double sideLength) {
        double d = sideLength / 2;

        bottomLeft = new LatLng(center.latitude - d, center.longitude - d);
        topRight = new LatLng(center.latitude + d, center.longitude + d);
    }

    public BoundingBox(LatLng driverPoint, LatLng maxPoint) {
        if (driverPoint.latitude < maxPoint.latitude) {
            if (driverPoint.longitude < maxPoint.longitude) {
                bottomLeft = driverPoint;
                topRight = maxPoint;
            } else {
                bottomLeft = new LatLng(driverPoint.latitude, maxPoint.longitude);
                topRight = new LatLng(maxPoint.latitude, driverPoint.longitude);
            }
        } else {
            if (maxPoint.longitude < driverPoint.longitude) {
                bottomLeft = maxPoint;
                topRight = driverPoint;
            } else {
                bottomLeft = new LatLng(maxPoint.latitude, driverPoint.longitude);
                topRight = new LatLng(driverPoint.latitude, maxPoint.longitude);
            }
        }
    }

    public LatLng getBottomLeft() {
       return bottomLeft;
    }

    public LatLng getTopRight() {
        return topRight;
    }

    @Override
    public String toString() {
        String bboxParams = "";
        bboxParams += bottomLeft.latitude + ",";
        bboxParams += bottomLeft.longitude + ",";
        bboxParams += topRight.latitude + ",";
        bboxParams += topRight.longitude;

        return bboxParams;
    }
}
