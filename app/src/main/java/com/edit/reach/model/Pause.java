package com.edit.reach.model;

import android.graphics.Color;
import android.util.Log;
import com.edit.reach.model.interfaces.IMilestone;
import com.edit.reach.utils.NavigationUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joakim on 2014-10-16.
 * A pause is an area (circle) of which a trucker needs to be stationary for a certain amount of time
 * before he can drive again. The pause is represented by a circle and contains available milestones.
 */
public class Pause {
    private LatLng location;
    private Circle circle;
    private Marker middleOfPause;
    private List<IMilestone> milestones;
    private List<Marker> milestoneMarkers;
    private String DEBUG_TAG = "Pause";

    public Pause(LatLng center, List<IMilestone> milestones){
        this.location = center;
        this.milestones = milestones;
        this.milestoneMarkers = new ArrayList<Marker>();
    }

    /**
     * Draw the pause as a circle
     * @param map, the map to draw it on
     */
    public void draw(GoogleMap map){
        Log.d("Pause", "Drawing Pause at " + location.toString());
        this.erase();
        this.circle = map.addCircle(new CircleOptions().center(location).fillColor(Color.RED).radius( NavigationUtil.RADIUS_IN_KM * 1000 ));
        this.middleOfPause = map.addMarker(new MarkerOptions()
                .position(location)
                .title("Pause")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        for(IMilestone milestone : milestones){
            milestoneMarkers.add(map.addMarker(new MarkerOptions()
                    .position(milestone.getLocation())
                    .title(milestone.getName())
                    .snippet("Rating: " + milestone.getRank() + "/5\n" + milestone.getDescription())));
        }
    }

    /**
     * Erase the pause from all of the maps it has been drawn on
     */
    public void erase(){
        if(circle != null){
            circle.remove();
            circle = null;
        }
        if(middleOfPause != null){
            middleOfPause.remove();
            middleOfPause = null;
        }
        for(Marker marker : milestoneMarkers){
            marker.remove();
        }
    }

    public LatLng getLocation(){
        return location;
    }

    public List<IMilestone> getMilestones(){
        return milestones;
    }
}
