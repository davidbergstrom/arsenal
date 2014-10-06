package com.edit.reach.app;

import android.app.Activity;
import android.os.Bundle;
import android.swedspot.automotiveapi.AutomotiveSignal;
import android.view.Menu;
import android.view.MenuItem;
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
            URL url = new URL("https://api.worldtrucker.com/v1/tip?bbox=26.23952479477298%2C-94.9609375%2C57.77762234415841%2C12.96875&categories=");
            Remote.get(url, this);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGetSuccess(JSONObject json) {

    }

    public void onGetFail() {

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
