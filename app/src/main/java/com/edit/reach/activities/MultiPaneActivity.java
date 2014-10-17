package com.edit.reach.activities;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.edit.reach.app.R;
import com.edit.reach.fragments.MapFragment;
import com.edit.reach.fragments.MilestonesFragment;
import com.edit.reach.fragments.RouteFragment;
import com.edit.reach.model.Milestone;
import com.edit.reach.model.NavigationModel;
import com.edit.reach.model.Pause;
import com.edit.reach.model.Route;
import com.edit.reach.model.interfaces.IMilestone;
import com.edit.reach.model.interfaces.RouteListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MultiPaneActivity extends FragmentActivity implements MapFragment.OnMapInteractionListener,
	RouteFragment.OnRouteInteractionListener, MilestonesFragment.OnMilestonesInteractionListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private NavigationModel nvm;

    private List<IMilestone> preliminaryMilestones;

    private MilestonesFragment milestonesFragment;
	private RouteFragment routeFragment;

    // A handler for the UI thread. The Handler recieves messages from other thread.
	private Handler mainHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message message) {
			// TODO how to handle messages sent to UI thread.
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_pane);

        //Create a FragmentManager and add a Fragment to it
        if(findViewById(R.id.container_fragment_left) != null){
            if(savedInstanceState != null){
                return;
            }

            routeFragment = RouteFragment.newInstance("Route");
            getSupportFragmentManager().beginTransaction().add(R.id.container_fragment_left, routeFragment).commit();
        }

        setUpMapIfNeeded();
        preliminaryMilestones = new ArrayList<IMilestone>();
        nvm = new NavigationModel(mMap, mainHandler);
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
                setupMap();
            }else{
                // TODO: Alert user that application wont work properly
            }
        }
    }

    public List<String> addMatchedStringsToList(String input, List<String> strings){
        strings = nvm.getMatchedStringResults(input);
        return strings;
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
                IMilestone milestone = nvm.getMap().getMilestone(marker.getPosition());
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
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onMapInteraction(Uri uri) {

	}



	@Override
	public void onRouteInteraction(Object o) {
        if(o.getClass() == RouteFragment.class){

        }

		// Sends the text from search field and receives a list of
		// places and sends the list back to the fragment
		if(o.getClass() == String.class) {
			List<String> list = nvm.getMatchedStringResults((String)o);
			routeFragment.suggestionList(list);
		}

        if(o.getClass() == Route.class){

            final Route r = (Route)o;
            nvm.setRoute(r);

			//fragment_container goes from RouteFragment -> MilestonesFragment
            //TODO: Load with a spinner and wait for route to finish, then show fragment below...
            //new Spinner().start();
            r.addListener(new RouteListener() {
                @Override
                public void onInitialization(boolean success) {
                    // WHen route finished loading
                    if(success){
                        milestonesFragment = MilestonesFragment.newInstance(r.getOriginAddress(), r.getDestinationAddress());
                        getSupportFragmentManager().beginTransaction().replace(R.id.container_fragment_left, milestonesFragment).commit();
                        ProgressBar spinner = (ProgressBar)findViewById(R.id.spinner);
                        spinner.setVisibility(View.GONE);
                    }else{
                        // When the Route failed to initialize, show the user an error.
                    }
                }

                @Override
                public void onPauseAdded(Pause pause) {
                    // Fuck this
                }
            });
        }
	}

    @Override
    public void onMilestonesInteraction(Object o) {

    }
}
