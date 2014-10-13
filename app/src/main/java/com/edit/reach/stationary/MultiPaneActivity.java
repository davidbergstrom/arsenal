package com.edit.reach.stationary;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.edit.reach.app.R;
import com.edit.reach.model.NavigationModel;
import com.edit.reach.model.Route;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class MultiPaneActivity extends FragmentActivity implements MapFragment.OnMapInteractionListener,
	RouteFragment.OnRouteInteractionListener, MilestonesFragment.OnMilestonesInteractionListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private NavigationModel nvm;

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

            RouteFragment routeFragment = RouteFragment.newInstance("Route");
            getSupportFragmentManager().beginTransaction().add(R.id.container_fragment_left, routeFragment).commit();
        }

        setUpMapIfNeeded();
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
                mMap.setMyLocationEnabled(true);
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
					@Override
					public View getInfoWindow(Marker marker) {
						return null;
					}

					@Override
					public View getInfoContents(Marker marker) {

						return null;
					}
				});
            }
        }
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
        if(o.getClass() == Route.class){

            Route r = (Route)o;
            nvm.setRoute(r);

			// Test data for Milestones List
			ArrayList <String> milestonesList = new ArrayList<String>();
			ArrayList <String> milestonesType = new ArrayList<String>();



			//fragment_container goes from RouteFragment -> MilestonesFragment
			MilestonesFragment milestonesFragment = MilestonesFragment.newInstance("Stockholm", "Lund", milestonesList, milestonesType);
			getSupportFragmentManager().beginTransaction().replace(R.id.container_fragment_left, milestonesFragment).commit();

        }
	}

    @Override
    public void onMilestonesInteraction(Object o) {

    }
}
