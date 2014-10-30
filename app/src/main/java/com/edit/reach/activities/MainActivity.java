package com.edit.reach.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import com.edit.reach.app.R;

/** This class is the top level UI activity.
 * It Only starts the MultiPaneActivity which then decides which fragment to start based on
 * what button the user clicks in this activity.
 * The class is only for demo purposes it makes the use able to choose which state to start with.
 */
public class MainActivity extends Activity implements View.OnClickListener {

	private ImageButton startMovingActivity, startStationaryActivity;

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
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		Intent intent;
		if (view == startMovingActivity) {
			intent  = new Intent(this, MultiPaneActivity.class);
			intent.putExtra("Moving", true);
			startActivity(intent);
		} else if (view == startStationaryActivity) {
			intent = new Intent(this, MultiPaneActivity.class);
			intent.putExtra("Moving", false);
			startActivity(intent);
		}
	}
}
