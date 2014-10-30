package com.edit.reach.utils;

import com.edit.reach.app.R;
import com.edit.reach.model.IMilestone;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import static com.edit.reach.model.IMilestone.*;

/**
 * Created by Joakim Berntsson on 2014-10-01.
 * Static class for navigation utilities.
 */
public class NavigationUtil {

	/** Constant, the refresh rate of the navigation loop in milliseconds */
	public static final int UPDATE_INTERVAL_SLOW = 500,
			UPDATE_INTERVAL_NORMAL = 250,
			UPDATE_INTERVAL_FAST = 100,
			ROUTE_INTERVAL = 60000,
			ROUTE_INTERVAL_DEMO = 20000;

	/** Radius for the pauses */
	public static final double RADIUS_IN_DEGREES = 0.2;

	/** Radius for the pauses */
	public static final int RADIUS_IN_KM = (int) getDistance(new LatLng(0,0), new LatLng(0,RADIUS_IN_DEGREES));

	/** Icons for the markers */
	public static final BitmapDescriptor
			foodMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_food),
			selectedFoodMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_food_selected),
			bathroomMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_toilet),
			selectedBathroomMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_toilet_selected),
			pauseMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_pause),
			restMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_reststop),
			selectedRestMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_reststop_selected),
			gasMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_gas),
			selectedGasMarker = BitmapDescriptorFactory.fromResource(R.drawable.marker_gas_selected);

	private NavigationUtil(){}

	/**
	 * Returns the icon for the specified milestone as a selected icon.
	 * @param milestone the milstone to find the selected image for
	 * @return the icon as a BitmapDescriptor
	 */
	public static BitmapDescriptor getSelectedMilestoneIcon(IMilestone milestone){
		BitmapDescriptor icon;

		if (milestone.hasCategory(Category.GASSTATION)) {
			icon = NavigationUtil.selectedGasMarker;
		} else if (milestone.hasCategory(Category.FOOD)) {
			icon = NavigationUtil.selectedFoodMarker;
		} else if (milestone.hasCategory(Category.TOILET)) {
			icon = NavigationUtil.selectedBathroomMarker;
		} else {
			icon = NavigationUtil.selectedRestMarker;
		}

		return icon;
	}

	/**
	 * Returns the icon for the specified milestone.
	 * @param milestone the milestone to find the image for
	 * @return the icon as a BitmapDescriptor
	 */
	public static BitmapDescriptor getMilestoneIcon(IMilestone milestone) {
		BitmapDescriptor icon;

		if (milestone.hasCategory(Category.GASSTATION)) {
			icon = NavigationUtil.gasMarker;
		} else if (milestone.hasCategory(Category.FOOD)) {
			icon = NavigationUtil.foodMarker;
		} else if (milestone.hasCategory(Category.TOILET)) {
			icon = NavigationUtil.bathroomMarker;
		} else {
			icon = NavigationUtil.restMarker;
		}

		return icon;
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
	public static float getBearing(LatLng firstLocation, LatLng secondLocation){
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
