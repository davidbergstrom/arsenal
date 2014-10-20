package com.edit.reach.activities;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.edit.reach.app.R;
import com.edit.reach.constants.SignalType;
import com.edit.reach.fragments.ControlFragment;
import com.edit.reach.fragments.MapFragment;
import com.edit.reach.fragments.MilestonesFragment;
import com.edit.reach.fragments.RouteFragment;
import com.edit.reach.model.*;
import com.edit.reach.model.interfaces.IMilestone;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

public class MultiPaneActivity extends FragmentActivity{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private NavigationModel navigationModel;

    private Boolean routeWithCurrentLocation;

    private List<IMilestone> preliminaryMilestones;
    private ProgressBar spinner;

    private MilestonesFragment milestonesFragment;
    private RouteFragment routeFragment;
    private ControlFragment controlFragment;

    private boolean msFragmentHasBeenCreated = false;

    private Route route;

    // A handler for the UI thread. The Handler recieves messages from other thread.
    private Handler mainHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case SignalType.VEHICLE_STOPPED_OR_STARTED:
                    // TODO
                    break;

                case SignalType.ROUTE_INITIALIZATION_SUCCEDED:
                    if(!msFragmentHasBeenCreated) {
                        createMilestonesFragment();
                        msFragmentHasBeenCreated = true;
                    } else {
                        // TODO what to do here?
                    }
                    break;

                case SignalType.FUEL_UPDATE:
                    controlFragment.setBarFuel((Float) message.obj);

                    break;

                case SignalType.UPTIME_UPDATE:
                    controlFragment.setBarTimeClock((Double) message.obj);
                    break;

                case SignalType.ROUTE_TOTAL_TIME_UPDATE:
                    controlFragment.setTotalTime((Long)message.obj);
                    break;

                case SignalType.LEG_UPDATE:
                    controlFragment.setNextLeg((Leg)message.obj);
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_pane);
        //Create a FragmentManager and add a Fragment to it
        setUpMapIfNeeded();
        preliminaryMilestones = new ArrayList<IMilestone>();
        navigationModel = new NavigationModel(mMap, mainHandler);
	    Bundle bundle = getIntent().getExtras();
        if(findViewById(R.id.container_fragment_left) != null){
            if(savedInstanceState != null){
                return;
            }

	        if(bundle.getBoolean("Moving")){
			    initializeMovingBackend();
		        initializeMovingUI();
		    } else {
	            initializeStationaryUI();
		    }

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();


    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link } once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
	            Log.d("setUpMapIfNeeded", "mMap != null");
                setupMap();
            }else{
                // TODO: Alert user that application wont work properly
            }
        }
    }

    public List<String> addMatchedStringsToList(String input, List<String> strings){
        strings = navigationModel.getMatchedStringResults(input);
        return strings;
    }

	public void initializeMovingUI(){
		controlFragment = ControlFragment.newInstance("MovingMode");
		getSupportFragmentManager().beginTransaction().add(R.id.container_fragment_left, controlFragment).
				addToBackStack("ControlFragment").commit();
	}
	public void initializeMovingBackend(){
		navigationModel.getMap().setState(Map.State.MOVING);
	}

	public void addMilestones(){
		navigationModel.addMilestones(preliminaryMilestones);
	}

	public void initializeStationaryUI(){
		routeFragment = RouteFragment.newInstance("Route");
		getSupportFragmentManager().beginTransaction()
				.add(R.id.container_fragment_left, routeFragment).commit();
	}

    private void setupMap(){
        // Enable GoogleMap to track the user's location
        mMap.setMyLocationEnabled(true);
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                View myContentsView = getLayoutInflater().inflate(R.layout.custom_info_window, null);

                TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));
                tvTitle.setText(marker.getTitle());
                TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
                tvSnippet.setText(marker.getSnippet());

                return myContentsView;
            }

            @Override
            public View getInfoContents(Marker marker) {

                return null;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                IMilestone milestone = navigationModel.getMap().getMilestone(marker.getPosition());
                if(milestone != null){
                    if(preliminaryMilestones.contains(milestone)){
                        Log.d("MultiPaneActivity", "Milestone already in list, removing");
                        // Milestone already added
                        preliminaryMilestones.remove(milestone);
                        milestonesFragment.removeMilestoneCard(milestone);
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker());
                    }else{
                        Log.d("MultiPaneActivity", "Added milestone to list");
                        // Add milestone to list
                        preliminaryMilestones.add(milestone);
                        milestonesFragment.addMilestoneCard(milestone);
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    }
                }else {
                    Log.d("MultiPaneActivity", "Invalid milestone, removing");
                    // Marker unavailable, remove it
                    marker.remove();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.multi_pane, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    public void goBackToRouteFragment(){
        Log.d("MultiPaneActivity", "goBackFragment");
	    //msFragmentHasBeenCreated = false;
        getSupportFragmentManager().beginTransaction().replace
                (R.id.container_fragment_left, routeFragment).addToBackStack("routeFragment").commit();
    }

    public Location getMyLocation(){
        return mMap.getMyLocation();
    }

    public void createRoute(String from, String to){
        routeWithCurrentLocation = false;
        route = new Route(from, to);
        navigationModel.setRoute(route);
    }

    public void createRoute(LatLng one, String to){
        routeWithCurrentLocation = true;
        route = new Route(one, to);
        navigationModel.setRoute(route);
    }

    public boolean routeWithCurrentLocation(){
        return routeWithCurrentLocation;
    }


    public void createRouteWithMyLocation(String to){
        Double myLocationLatitude = getMyLocation().getLatitude();
        Double myLocationLongitude = getMyLocation().getLongitude();
        LatLng myLocation = new LatLng(myLocationLatitude, myLocationLongitude);
        createRoute(myLocation, to);
    }

    public void showSpinner(){
        spinner = (ProgressBar)findViewById(R.id.spinner);
        spinner.setVisibility(View.VISIBLE);
    }

    public void createMilestonesFragment(){
        if(spinner != null) {
            spinner = (ProgressBar) findViewById(R.id.spinner);
            spinner.setVisibility(View.GONE);
            milestonesFragment = MilestonesFragment.newInstance(route.getOriginAddress(), route.getDestinationAddress());
            getSupportFragmentManager().beginTransaction().
		            replace(R.id.container_fragment_left, milestonesFragment).addToBackStack("milestonesFragment").commit();

        }
    }

    public void getPauseSuggestions(IMilestone.Category category){
        navigationModel.getPauseSuggestions(category);
    }

	public void getMatchedStringResults(String str){
		List<String> list = navigationModel.getMatchedStringResults(str);
		routeFragment.suggestionList(list);
	}

}