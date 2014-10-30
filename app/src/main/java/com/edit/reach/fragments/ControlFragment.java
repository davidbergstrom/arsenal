package com.edit.reach.fragments;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.edit.reach.activities.MultiPaneActivity;
import com.edit.reach.app.R;
import com.edit.reach.constants.UniversalConstants;
import com.edit.reach.model.Leg;
import com.edit.reach.model.IMilestone;
import com.edit.reach.utils.TimeConvert;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ControlFragment extends Fragment{

	private static final String ARG_ID = "Control";
	private ImageButton ibRestArea;
	private ImageButton ibFood;
	private ImageButton ibRestroom;
	private ImageButton ibGasStation;

	private ImageState isRestArea;
	private ImageState isFood;
	private ImageState isRestroom;
	private ImageState isGasStation;

	public enum ImageState {
		ORIGINAL, PRESSED
	}

	//Progressbar
	private ProgressBar barFuel;
	private ProgressBar barTimeClock;

    //TextViews
    private TextView textTotalTime;
    private TextView textTimeToNextStop;
    private TextView textNextStop;
    private TextView textDistanceToTextStop;

    //MileStone Images
    private ImageView ivFood;
    private ImageView ivGasstation;
    private ImageView ivRestArea;
    private ImageView ivToilet;

	private LinearLayout navigationInfoContainer;

	/**
	 * Sets the indicator value on the progressbar for time clock
	 * @param timeClock the time to a obligatory pause
	 */
    public void setBarTimeClock(double timeClock) {
        barTimeClock.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
		barTimeClock.setMax((int) (UniversalConstants.LEGAL_UPTIME_IN_SECONDS * UniversalConstants.SECONDS_TO_MINUTES));
		barTimeClock.setProgress((int) (timeClock * UniversalConstants.SECONDS_TO_MINUTES));

        if (timeClock <= UniversalConstants.TIME_THRESHOLD) {
            barTimeClock.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        }
	}

	/**
	 * Sets the indicator value on the progressbar for fuel level
	 * @param fuelLevel the fuel level we have in the tank
	 */
	public void setBarFuel(float fuelLevel) {
        barFuel.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
		barFuel.setMax(100);
		barFuel.setProgress((int) fuelLevel);

        if (fuelLevel <= UniversalConstants.FUEL_THRESHOLD) {
            barFuel.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        }
	}

	/**
	 *
	 * @param leg
	 */
    public void setNextLeg(Leg leg) {

        if (leg.getMilestone() != null) {
            IMilestone milestone = leg.getMilestone();
            float nextStopClock = leg.getDuration();
            String nextStopName = milestone.getName();
            float distanceToNextStop = leg.getDistance();
            Log.d("ControlFragment", "Mile :"+milestone.toString() + ", cat :"+milestone.getCategories());
            List<IMilestone.Category> categories = milestone.getCategories();

            textNextStop.setText(nextStopName);
            textTimeToNextStop.setText(TimeConvert.convertToHoursAndMinutes(((int) (nextStopClock * UniversalConstants.SECONDS_TO_MINUTES))));
            textDistanceToTextStop.setText((int)(distanceToNextStop * 0.001) + " km");

            //Set Milestone Images
            ivFood.setVisibility(ImageView.GONE);
            ivGasstation.setVisibility(ImageView.GONE);
            ivRestArea.setVisibility(ImageView.GONE);
            ivToilet.setVisibility(ImageView.GONE);

            for (IMilestone.Category cat : categories) {

                Log.d("ControlFragment:", "" + cat);

                switch (cat) {
                    case FOOD:
                        ivFood.setVisibility(ImageView.VISIBLE);
                        Log.d("ControlFragment:", "Set FOOD Visible");
                        break;

                    case GASSTATION:
                        ivGasstation.setVisibility(ImageView.VISIBLE);
                        Log.d("ControlFragment:", "Set GASSTATION Visible");
                        break;

                    case RESTAREA:
                        ivRestArea.setVisibility(ImageView.VISIBLE);
                        Log.d("ControlFragment:", "Set RESTAREA Visible");
                        break;

                    case TOILET:
                        ivToilet.setVisibility(ImageView.VISIBLE);
                        Log.d("ControlFragment:", "Set TOILET Visible");
                        break;

                }
            }
        }
    }

	/**
	 * Sets the total time to the next milestone
	 * @param totalTime the totaltime to next milestone
	 */
    public void setTotalTime(double totalTime) {
        textTotalTime.setText(TimeConvert.convertToHoursAndMinutes((int) (totalTime * UniversalConstants.SECONDS_TO_MINUTES)));
    }

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 * @param id Id of the fragment
	 * @return A new instance of ControlFragment
	 */
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
        View totalTimeContainer = inflater.inflate(R.layout.total_time_window, null);

        textTotalTime = (TextView) totalTimeContainer.findViewById(R.id.tv_navigation_info_total_time_left);

		//Get Layout Containers to easily handle states
		navigationInfoContainer = (LinearLayout) view.findViewById(R.id.navigation_info_container);

        //Get TextViews
        textNextStop = (TextView) view.findViewById(R.id.tv_navigation_info_title);
        textTimeToNextStop = (TextView) view.findViewById(R.id.navigation_info_time);
        textDistanceToTextStop = (TextView) view.findViewById(R.id.navigation_info_distance);

        //Get ImageViews
        ivFood = (ImageView) view.findViewById(R.id.navigation_info_icon_type_food);
        ivGasstation = (ImageView) view.findViewById(R.id.navigation_info_icon_type_gasstation);
        ivRestArea = (ImageView) view.findViewById(R.id.navigation_info_icon_type_restarea);
        ivToilet = (ImageView) view.findViewById(R.id.navigation_info_icon_type_toilet);

        //Get progressbars
        barFuel = (ProgressBar) view.findViewById(R.id.progress_gas);
        barTimeClock = (ProgressBar) view.findViewById(R.id.progress_time_clock);

		//Get and set Input buttons
		ibRestArea = (ImageButton) view.findViewById(R.id.button_control_input_restarea);
		isRestArea = ImageState.ORIGINAL;
		ibRestArea.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity)getActivity()).getPauseSuggestions(IMilestone.Category.RESTAREA);
				Log.d("INPUTBUTTON", "Restarea");
			}
		});
		ibFood = (ImageButton) view.findViewById(R.id.button_control_input_restaurant);
		isFood = ImageState.ORIGINAL;
		ibFood.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity) getActivity()).getPauseSuggestions(IMilestone.Category.FOOD);
				Log.d("INPUTBUTTON", "Food");
			}
		});
		ibRestroom = (ImageButton) view.findViewById(R.id.button_control_input_toilet);
		isRestroom = ImageState.ORIGINAL;
		ibRestroom.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity)getActivity()).getPauseSuggestions(IMilestone.Category.TOILET);
				Log.d("INPUTBUTTON", "Toilet");
			}
		});
		ibGasStation = (ImageButton) view.findViewById(R.id.button_control_input_gasstation);
		isGasStation = ImageState.ORIGINAL;
		ibGasStation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity)getActivity()).getPauseSuggestions(IMilestone.Category.GASSTATION);
				Log.d("INPUTBUTTON", "Gas Station");
			}
		});

		return view;
	}



}