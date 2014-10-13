package com.edit.reach.utils;

import com.edit.reach.model.interfaces.IMilestone;
import com.google.android.gms.maps.model.LatLng;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joakim Berntsson on 2014-10-01.
 * Static class for navigation utilities.
 */
public class NavigationUtil {
	/* TODO Hey guys please refactor this class.
		Have one class with the URL-stuff. And one class for the computation stuff.
	 */

    /** Radius for the pauses */
    public static final double RADIUS_IN_DEGREES = 1000;

    /** Radius for the pauses */
    public static final int RADIUS_IN_KM = (int) getDistance(new LatLng(0,0), new LatLng(0,RADIUS_IN_DEGREES));

	private NavigationUtil(){}

    public static URL makeURL(LatLng origin, LatLng destination, List<IMilestone> milestones, boolean routeOptimization) {

        List<LatLng> wayPoints = new ArrayList<LatLng>();

        for (IMilestone i : milestones) {
            wayPoints.add(i.getLocation());
        }

        String url = "http://maps.googleapis.com/maps/api/directions/json";
        url += "?origin=" + Double.toString(origin.latitude) + "," + Double.toString(origin.longitude);// from
        url += "&destination=" + Double.toString(destination.latitude) + "," + Double.toString(destination.longitude);// to

        if(wayPoints.size() != 0) {
            url += "&waypoints=optimize:" + routeOptimization;

            for (LatLng stop : wayPoints) {
                url += "|" + Double.toString(stop.latitude) + "," + Double.toString(stop.longitude);
            }

        }

        url += "&mode=driving&alternatives=true&language=EN";

        URL http = null;
        try {
            http = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return http;
    }

    public static URL makeURL(String address) {
        String location = address.replaceAll(" ", "+").toLowerCase();

        String encodedLocation = null;
        try {
            encodedLocation = URLEncoder.encode(location, "UTF-8"); //Converting the string to UTF-8
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String url = "https://maps.googleapis.com/maps/api/geocode/json";
        url += "?address=" + encodedLocation;
        url += "&key=AIzaSyCqs-SMMT3_BIzMsPr-wsWqsJTthTgFUb8";

        URL http = null;
        try {
            http = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return http;
    }

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

    public static double getDistance(LatLng firstPosition, LatLng secondPosition){
        int R = 6371; // Earths radius in km
        double a = Math.pow(Math.sin(Math.toRadians(secondPosition.latitude-firstPosition.latitude)/2), 2) +
                Math.cos(secondPosition.latitude) * Math.cos(firstPosition.latitude) *
                        Math.pow(Math.sin(Math.toRadians(secondPosition.longitude-firstPosition.longitude)/2), 2);

        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    public static double finalBearing(double lat1, double long1, double lat2, double long2){
        double degToRad = Math.PI / 180.0;
        double phi1 = lat1 * degToRad;
        double phi2 = lat2 * degToRad;
        double lam1 = long1 * degToRad;
        double lam2 = long2 * degToRad;

        double bearing = Math.atan2(Math.sin(lam2-lam1)*Math.cos(phi2),
                Math.cos(phi1)*Math.sin(phi2) - Math.sin(phi1)*Math.cos(phi2)*Math.cos(lam2-lam1)) * 180/Math.PI;

        return (bearing + 180.0) % 360;
    }
}
