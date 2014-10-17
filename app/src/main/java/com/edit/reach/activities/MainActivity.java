package com.edit.reach.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import com.edit.reach.app.R;
import com.edit.reach.constants.MovingState;
import com.edit.reach.constants.SignalType;
import com.edit.reach.model.NavigationModel;


public class MainActivity extends Activity implements View.OnClickListener {

	private ImageButton startMovingActivity, startStationaryActivity;
    private NavigationModel navigationModel;

    // A handler for the UI thread. The Handler recieves messages from other thread.
    private Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {

            //Change Activity when state is changed
            if (message.what == SignalType.VEHICLE_STOPPED_OR_STARTED) {

                if ((Integer)message.obj == MovingState.NOT_IN_DRIVE) {
                     Intent intent = new Intent(getApplicationContext(), MultiPaneActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), MovingActivity.class);
                    startActivity(intent);
                }

            }


        }
    };

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

		startMovingActivity = (ImageButton) findViewById(R.id.start_moving_activity);
        startMovingActivity.setOnClickListener(this);

        startStationaryActivity = (ImageButton) findViewById(R.id.start_stationary_activity);
        startStationaryActivity.setOnClickListener(this);
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

	@Override
	public void onClick(View view) {
        Intent intent = null;
		if (view == startMovingActivity) {
			intent = new Intent(this, MovingActivity.class);
			startActivity(intent);
		} else if (view == startStationaryActivity) {
            intent = new Intent(this, MultiPaneActivity.class);
            startActivity(intent);
        }
	}
}
