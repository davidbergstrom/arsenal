package com.edit.reach.utils;

import com.edit.reach.app.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joakim Berntsson on 2014-10-01.
 * Static class for navigation utilities.
 */
public class NavigationUtil {

    /** Radius for the pauses */
    public static final double RADIUS_IN_DEGREES = 0.2;

    /** Radius for the pauses */
    public static final int RADIUS_IN_KM = (int) getDistance(new LatLng(0,0), new LatLng(0,RADIUS_IN_DEGREES));

    public static final BitmapDescriptor
            foodMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_food),
            bathroomMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_bathroom),
            plannedMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_pause),
            restMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_reststop),
            gasMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_gas);

	private NavigationUtil(){}

    public static List<LatLng> decodePoly(String encoded) {

		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng( (((double) lat / 1E5)),
					(((double) lng / 1E5) ));
			poly.add(p);
		}

		return poly;
	}

    /**
     * Returns the distance between the two coordinates in km.
     * @param firstPosition, coordinate of the first location
     * @param secondPosition, coordinate of the second
     * @return the distance in km
     */
    public static double getDistance(LatLng firstPosition, LatLng secondPosition){
        int R = 6371; // Earths radius in km
        double a = Math.pow(Math.sin(Math.toRadians(secondPosition.latitude-firstPosition.latitude)/2), 2) +
                Math.cos(secondPosition.latitude) * Math.cos(firstPosition.latitude) *
                        Math.pow(Math.sin(Math.toRadians(secondPosition.longitude-firstPosition.longitude)/2), 2);

        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    /**
     * Get the bearing of the two locations provided.
     * @param firstLocation, the location to get the bearing from
     * @param secondLocation, the location to get the bearing too
     * @return the bearing
     */
    public static float finalBearing(LatLng firstLocation, LatLng secondLocation){
        double degToRad = Math.PI / 180.0;
        double phi1 = firstLocation.latitude * degToRad;
        double phi2 = secondLocation.latitude * degToRad;
        double lam1 = firstLocation.longitude * degToRad;
        double lam2 = secondLocation.longitude * degToRad;

        double bearing = Math.atan2(Math.sin(lam2-lam1)*Math.cos(phi2),
                Math.cos(phi1)*Math.sin(phi2) - Math.sin(phi1)*Math.cos(phi2)*Math.cos(lam2-lam1)) * 180/Math.PI;

        return (float)(bearing + 180.0) % 360;
    }
}
