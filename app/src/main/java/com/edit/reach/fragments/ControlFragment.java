package com.edit.reach.fragments;
import android.graphics.Color;
import android.os.Bundle;

import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageButton;

import com.edit.reach.activities.MultiPaneActivity;
import com.edit.reach.app.R;

import com.edit.reach.constants.UniversalConstants;
import com.edit.reach.model.interfaces.IMilestone;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class ControlFragment extends Fragment{

	private static final String ARG_ID = "Control";
	private ImageButton ibRestArea;
	private ImageButton ibRestaurant;
	private ImageButton ibToilet;
	private ImageButton ibGasStation;

	private float fuelLevel;
	private double nextStopClock; //in sec
	private double timeClock;   //in sec
	private double totalTime;   //in sec
	private String nextStopName = null;

	//Progressbars
	private ProgressBar fuelBar;
	private ProgressBar nextStopBar;
	private ProgressBar timeClockBar;

	public void setTimeClockBar(double timeClock) {
		this.timeClock = timeClock;
        timeClockBar.setBackgroundColor(Color.GREEN);
		timeClockBar.setMax((int)(UniversalConstants.LEGAL_UPTIME_IN_SECONDS * UniversalConstants.SECONDS_TO_MINUTES));
		timeClockBar.setProgress((int) (timeClock * UniversalConstants.SECONDS_TO_MINUTES));

        if (timeClock <= UniversalConstants.TIME_THRESHOLD) {
            timeClockBar.setBackgroundColor(Color.RED);
        }
	}


	public void setFuelBar(float fuelLevel) {
		this.fuelLevel = fuelLevel;
        fuelBar.setBackgroundColor(Color.GREEN);
		fuelBar.setMax(100);
		fuelBar.setProgress((int) fuelLevel);

        if (fuelLevel <= UniversalConstants.FUEL_THRESHOLD) {
            fuelBar.setBackgroundColor(Color.RED);
        }
	}

    public void setNextStopBar(double nextStopClock) {
        this.nextStopClock = nextStopClock;
        nextStopBar.setBackgroundColor(Color.GREEN);
        nextStopBar.setMax();
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }

	public static ControlFragment newInstance(String id){
		ControlFragment fragment = new ControlFragment();
		Bundle args = new Bundle();
		args.putString(ARG_ID, id);
		fragment.setArguments(args);
		return fragment;
	}

	public ControlFragment() {
		// Required empty public constructor
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment

		View view = inflater.inflate(R.layout.fragment_control, container, false);

		TextView stopNameView = (TextView) view.findViewById(R.id.tv_control_info_top_card_title);

		if(nextStopName != null) {
			stopNameView.setText(nextStopName);
		} else {
			stopNameView.setText("N/A");
		}

		//Get progressbars
		fuelBar = (ProgressBar) view.findViewById(R.id.progress_gas);
		timeClockBar = (ProgressBar) view.findViewById(R.id.progress_time_clock);
		nextStopBar = (ProgressBar) view.findViewById(R.id.progress_next_stop);

		ibRestArea = (ImageButton) view.findViewById(R.id.button_control_input_restarea);
		ibRestArea.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity)getActivity()).getPauseSuggestions(IMilestone.Category.RESTAREA);
			}
		});
		ibRestaurant = (ImageButton)view.findViewById(R.id.button_control_input_restaurant);
		ibRestaurant.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity)getActivity()).getPauseSuggestions(IMilestone.Category.RESTAURANT);
			}
		});
		ibToilet = (ImageButton) view.findViewById(R.id.button_control_input_toilet);
		ibToilet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//TODO: Fix Toilet...
			}
		});
		ibGasStation = (ImageButton) view.findViewById(R.id.button_control_input_gasstation);
		ibGasStation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity)getActivity()).getPauseSuggestions(IMilestone.Category.GASSTATION);
			}
		});

		return view;
	}

}