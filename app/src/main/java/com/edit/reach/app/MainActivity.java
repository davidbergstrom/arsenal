package com.edit.reach.app;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.edit.reach.model.NavigationModel;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.swedspot.automotiveapi.*;
import android.swedspot.automotiveapi.unit.*;
import android.swedspot.automotiveapi.*;
import android.swedspot.scs.data.*;
import android.widget.TextView;


import com.swedspot.vil.policy.*;
import com.swedspot.vil.distraction.*;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    Log.d("ONCREATE", "ONCREATE");
	    setContentView(R.layout.activity_main);
	    NavigationModel m = new NavigationModel();

	    // MORGAN EXAMPLE
/*	    // Fire off an async task. Networking and similar should not (cannot) happen on the UI thread
	    new AsyncTask() {
		    @Override
		    protected Object doInBackground(Object... objects) {
			    // Access to Automotive API
			    AutomotiveFactory.createAutomotiveManagerInstance(
					    new AutomotiveCertificate(new byte[0]),
					    new AutomotiveListener() { // Listener that observes the Signals
						    @Override
						    public void receive(final AutomotiveSignal automotiveSignal) {
							    SCSFloat f = (SCSFloat)automotiveSignal.getData();
							    Log.d("FUEL", f.getFloatValue() + "");
						    }

						    @Override
						    public void timeout(int i) {}

						    @Override
						    public void notAllowed(int i) {}
					    },
					    new DriverDistractionListener() {       // Observe driver distraction level
						    @Override
						    public void levelChanged(final DriverDistractionLevel driverDistractionLevel) {
						    }
					    }
			    ).register(AutomotiveSignalId.FMS_FUEL_LEVEL_1); // Register for the speed signal
			    return null;
		    }
	    }.execute(); // And go!

    }*/

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
}