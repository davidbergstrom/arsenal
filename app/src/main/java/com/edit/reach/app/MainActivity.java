package com.edit.reach.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.swedspot.automotiveapi.AutomotiveSignal;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;
import com.swedspot.automotiveapi.AutomotiveListener;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends Activity implements ResponseHandler {

    private AutomotiveListener hej = new AutomotiveListener() {
        @Override
        public void receive(AutomotiveSignal automotiveSignal) {

        }

        @Override
        public void timeout(int i) {

        }

        @Override
        public void notAllowed(int i) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            LatLng centralPoint = new LatLng(26.239523556, 3.45);
            Double sideLength = 20.2;
            BoundingBox bbox = new BoundingBox(centralPoint, sideLength);

            URL url = WorldTruckerEndpoints.getMilestonesURL(bbox);
            Remote.get(url, this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
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
			startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
