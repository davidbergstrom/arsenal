package com.edit.reach.system;

import android.util.Log;
import com.edit.reach.model.IMilestone;
import com.google.android.gms.maps.model.LatLng;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Erik Nordmark on 2014-10-14.
 * A class that constructs all URLs for the HTTP requests
 */
public class GoogleMapsEndpoints {

	/**
	 * Constructs the URL for Google Directions API
	 *
	 * @param origin            Latlng from the starting point
	 * @param destination       Latlng to the ending point
	 * @param milestones        List of via stops for the route
	 * @param routeOptimization Boolean if Directions API should optimize the route (sort the stops).
	 * @return the URL
	 */
	public static URL makeURLDirections(LatLng origin, LatLng destination, List<IMilestone> milestones, boolean routeOptimization) {

		List<LatLng> wayPoints = new ArrayList<LatLng>();

		for (IMilestone i : milestones) {
			wayPoints.add(i.getLocation());
		}

		String url = "http://maps.googleapis.com/maps/api/directions/json";
		url += "?origin=" + Double.toString(origin.latitude) + "," + Double.toString(origin.longitude);// from
		url += "&destination=" + Double.toString(destination.latitude) + "," + Double.toString(destination.longitude);// to

		if (wayPoints.size() != 0) {
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
		Log.d("GoogleMapsEndpoints", url);

		return http;
	}

	/**
	 * Constructs the URL for Google Geocode API
	 *
	 * @param address, that will be converted into a Latlng point
	 * @return the URL
	 */
	public static URL makeURLGeocode(String address) {
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

	/**
	 * Constructs the URL for Google Places API
	 *
	 * @param address, to find suggestions for.
	 * @return the URL
	 */
	public static URL makeURLPlaces(String address) {
		String location = address.replaceAll(" ", "+").toLowerCase();

		String encodedLocation = null;
		try {
			encodedLocation = URLEncoder.encode(location, "UTF-8"); //Converting the string to UTF-8
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json";
		url += "?input=" + encodedLocation;
		url += "&key=AIzaSyCqs-SMMT3_BIzMsPr-wsWqsJTthTgFUb8";

		URL http = null;
		try {
			http = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return http;
	}
}
