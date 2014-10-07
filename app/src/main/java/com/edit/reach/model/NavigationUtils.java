package com.edit.reach.model;

import com.google.android.gms.maps.model.LatLng;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joakim Berntsson on 2014-10-01.
 * Static class for navigation utilities.
 */
public class NavigationUtils {

	private NavigationUtils(){

	}

    public static URL makeURL(LatLng source, LatLng dest, List<LatLng> waypoints, boolean routeOptimization) throws MalformedURLException {
        String url = "http://maps.googleapis.com/maps/api/directions/json";
        url += "?origin=" + Double.toString(source.latitude) + "," + Double.toString(source.longitude);// from
        url += "&destination=" + Double.toString(dest.latitude) + "," + Double.toString(dest.longitude);// to

        if(waypoints != null && waypoints.size() != 0) {
            url += "&waypoints=optimize:" + routeOptimization;

            for (LatLng stop : waypoints) {
                url += "|" + Double.toString(stop.latitude) + "," + Double.toString(stop.longitude);
            }

        }

        url += "&sensor=false&mode=driving&alternatives=true&language=EN";

        URL http = new URL(url);

        return http;
    }

    public static URL makeURL(String address) throws MalformedURLException {
        String location = address.replaceAll(" ", "+").toLowerCase();
        String url = "https://maps.googleapis.com/maps/api/geocode/json";
        url += "?address=" + location;
        url += "&key=AIzaSyCqs-SMMT3_BIzMsPr-wsWqsJTthTgFUb8";

        URL http = new URL(url);

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
}
