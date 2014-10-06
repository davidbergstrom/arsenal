package com.edit.reach.app;

import android.graphics.PointF;

/**
 * A square representing a geographical area.
 */
public class BoundingBox {
    PointF bottomLeft;
    PointF topRight;

    public BoundingBox(PointF center, Float sideLength) {
        float d = sideLength / 2;

        bottomLeft = new PointF(center.x - d, center.y - d);
        topRight = new PointF(center.x + d, center.x + d);
    }

    PointF getBottomLeft() {
       return bottomLeft;
    }

    PointF getTopRight() {
        return topRight;
    }
}
