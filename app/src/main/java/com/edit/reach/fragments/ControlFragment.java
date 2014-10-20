package com.edit.reach.fragments;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.edit.reach.activities.MultiPaneActivity;
import com.edit.reach.app.R;
import com.edit.reach.constants.UniversalConstants;
import com.edit.reach.model.Leg;
import com.edit.reach.model.interfaces.IMilestone;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class ControlFragment extends Fragment{

	private static final String ARG_ID = "Control";
	private ImageButton ibRestArea;
	private ImageButton ibFood;
	private ImageButton ibToilet;
	private ImageButton ibGasStation;

    private IMilestone milestone;
    private List<IMilestone.Category> categories;

	private float fuelLevel;
	private double nextStopClock; //in sec
	private double timeClock;   //in sec
	private double totalTime;   //in sec
	private String nextStopName = null;

	//Progressbars
	private ProgressBar barFuel;
	private ProgressBar barTimeClock;

    //TextViews
    private TextView textTimeToNextStop;
    private TextView textNextStop;
    private TextView textDistanceToTextStop;

    //MileStone Images
    private ImageView ivFood;
    private ImageView ivGastation;
    private ImageView ivRestArea;
    private ImageView ivToilet;

	public void setBarTimeClock(double timeClock) {
		this.timeClock = timeClock;
        barTimeClock.setBackgroundColor(Color.GREEN);
		barTimeClock.setMax((int) (UniversalConstants.LEGAL_UPTIME_IN_SECONDS * UniversalConstants.SECONDS_TO_MINUTES));
		barTimeClock.setProgress((int) (timeClock * UniversalConstants.SECONDS_TO_MINUTES));

        if (timeClock <= UniversalConstants.TIME_THRESHOLD) {
            barTimeClock.setBackgroundColor(Color.RED);
        }
	}


	public void setBarFuel(float fuelLevel) {
		this.fuelLevel = fuelLevel;
        barFuel.setBackgroundColor(Color.GREEN);
		barFuel.setMax(100);
		barFuel.setProgress((int) fuelLevel);

        if (fuelLevel <= UniversalConstants.FUEL_THRESHOLD) {
            barFuel.setBackgroundColor(Color.RED);
        }
	}

    public void setNextLeg(Leg leg) {
        Leg thisLeg = leg;
        this.milestone = leg.getMilestone();
        this.nextStopClock = leg.getDuration();
        this.nextStopName = milestone.getName();
        this.categories = milestone.getCategories();
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

        //Get TextViews
        textNextStop = (TextView) view.findViewById(R.id.tv_control_info_top_card_title);
        textTimeToNextStop = (TextView) view.findViewById(R.id.control_info_top_card_time);
        textDistanceToTextStop = (TextView) view.findViewById(R.id.control_info_top_card_distance);

        //TODO delete setVisibility
        //Get ImageViews
        ivFood = (ImageView) view.findViewById(R.id.control_info_top_card_type_food);
        //ivFood.setVisibility(ImageView.INVISIBLE);
        ivGastation = (ImageView) view.findViewById(R.id.control_info_top_card_type_gasstation);
        //ivGastation.setVisibility(ImageView.INVISIBLE);
        ivRestArea = (ImageView) view.findViewById(R.id.control_info_top_card_type_restarea);
        //ivRestArea.setVisibility(ImageView.INVISIBLE);
        ivToilet = (ImageView) view.findViewById(R.id.control_info_top_card_type_toilet);
        //ivToilet.setVisibility(ImageView.INVISIBLE);

        //Get progressbars
        barFuel = (ProgressBar) view.findViewById(R.id.progress_gas);
        barTimeClock = (ProgressBar) view.findViewById(R.id.progress_time_clock);

		if(nextStopName != null) {
			textNextStop.setText(nextStopName);
		} else {
			textNextStop.setText("N/A");
		}

        //Set Milestone Images
        for (IMilestone.Category cat : categories) {

            switch (cat) {
                case FOOD: ivFood.setVisibility(ImageView.VISIBLE);
                break;

                case GASSTATION: ivGastation.setVisibility(ImageView.VISIBLE);
                break;

                case RESTAREA: ivGastation.setVisibility(ImageView.VISIBLE);
                break;

                case TOILET: ivToilet.setVisibility(ImageView.VISIBLE);
                break;

            }

        }

		ibRestArea = (ImageButton) view.findViewById(R.id.button_control_input_restarea);
		ibRestArea.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity)getActivity()).getPauseSuggestions(IMilestone.Category.RESTAREA);
			}
		});
		ibFood = (ImageButton)view.findViewById(R.id.button_control_input_restaurant);
		ibFood.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity) getActivity()).getPauseSuggestions(IMilestone.Category.FOOD);
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