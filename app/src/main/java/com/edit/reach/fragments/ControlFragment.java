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

	private int intRestArea;
	private int intFood;
	private int intToilet;
	private int intGasStation;


    //State of Panel
	private State currentState;

    public enum State {
		ROUTELESS, INFO, SUGGESTION
	}

	//Progressbar
	private ProgressBar barFuel;
	private ProgressBar barTimeClock;

    //TextViews
    private TextView textTotalTime;
    private TextView textTimeToNextStop;
    private TextView textNextStop;
    private TextView textDistanceToTextStop;
    private TextView textRatingNextStop;

    //Suggestion Buttons
	private Button btNextSuggestion;
	private Button btOkSuggestion;

    //MileStone Images
    private ImageView ivFood;
    private ImageView ivGastation;
    private ImageView ivRestArea;
    private ImageView ivToilet;

	private LinearLayout navigationInfoContainer;
	private RelativeLayout suggestionButtonContainer;

    public void setBarTimeClock(double timeClock) {
        barTimeClock.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
		barTimeClock.setMax((int) (UniversalConstants.LEGAL_UPTIME_IN_SECONDS * UniversalConstants.SECONDS_TO_MINUTES));
		barTimeClock.setProgress((int) (timeClock * UniversalConstants.SECONDS_TO_MINUTES));

        if (timeClock <= UniversalConstants.TIME_THRESHOLD) {
            barTimeClock.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        }
	}


	public void setBarFuel(float fuelLevel) {
        barFuel.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
		barFuel.setMax(100);
		barFuel.setProgress((int) fuelLevel);

        if (fuelLevel <= UniversalConstants.FUEL_THRESHOLD) {
            barFuel.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        }
	}

    public void setNextLeg(Leg leg) {

        if (leg.getMilestone() != null) {
            IMilestone milestone = leg.getMilestone();
            float nextStopClock = leg.getDuration();
            String nextStopName = milestone.getName();
            float distanceToNextStop = leg.getDistance();
            Log.d("ControlFragment", "Mile :"+milestone.toString() + ", cat :"+milestone.getCategories());
            List<IMilestone.Category> categories = milestone.getCategories();

            textNextStop.setText(nextStopName);
            textTimeToNextStop.setText("Time to stop: " + (int)(nextStopClock * UniversalConstants.SECONDS_TO_MINUTES) + " min");
            textDistanceToTextStop.setText("Distance to stop: " + (int)(distanceToNextStop * 0.001) + " km");

            //Set Milestone Images
            ivFood.setVisibility(ImageView.GONE);
            ivGastation.setVisibility(ImageView.GONE);
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
                        ivGastation.setVisibility(ImageView.VISIBLE);
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

    public void setTotalTime(double totalTime) {
         //textTotalTime.setText((int)(totalTime * UniversalConstants.SECONDS_TO_MINUTES) + " min");
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

		textNextStop = (TextView) view.findViewById(R.id.tv_navigation_info_title);

		//Get Layout Containers to easily handle states
		navigationInfoContainer = (LinearLayout) view.findViewById(R.id.navigation_info_container);
		suggestionButtonContainer = (RelativeLayout) view.findViewById(R.id.suggestion_buttons);

        //Get TextViews
        textNextStop = (TextView) view.findViewById(R.id.tv_navigation_info_title);
        textRatingNextStop = (TextView) view.findViewById(R.id.navigation_info_rating);
        textTimeToNextStop = (TextView) view.findViewById(R.id.navigation_info_time);
        textDistanceToTextStop = (TextView) view.findViewById(R.id.navigation_info_distance);

		//Get Suggestion Buttons
		btNextSuggestion = (Button) view.findViewById(R.id.suggestion_button_next);
		btOkSuggestion = (Button) view.findViewById(R.id.suggestion_button_ok);

        //Get ImageViews
        ivFood = (ImageView) view.findViewById(R.id.navigation_info_icon_type_food);
        ivGastation = (ImageView) view.findViewById(R.id.navigation_info_icon_type_gasstation);
        ivRestArea = (ImageView) view.findViewById(R.id.navigation_info_icon_type_restarea);
        ivToilet = (ImageView) view.findViewById(R.id.navigation_info_icon_type_toilet);

        //Get progressbars
        barFuel = (ProgressBar) view.findViewById(R.id.progress_gas);
        barTimeClock = (ProgressBar) view.findViewById(R.id.progress_time_clock);

		//Get and set Input buttons
		ibRestArea = (ImageButton) view.findViewById(R.id.button_control_input_restarea);
		intRestArea = R.drawable.input_restarea;
		ibRestArea.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity)getActivity()).getPauseSuggestions(IMilestone.Category.RESTAREA);
				Log.d("INPUTBUTTON", "Restarea");
				if(intRestArea == R.drawable.input_restarea|| intRestArea == R.drawable.input_restarea_shaded){
					ibRestArea.setImageResource(R.drawable.input_restarea_pressed);
					intRestArea = R.drawable.input_restarea_pressed;
					shadeIcons(intRestArea);
				}
				else {
					ibRestArea.setImageResource(R.drawable.input_restarea);
					intRestArea = R.drawable.input_restarea;
				}
			}
		});
		ibFood = (ImageButton) view.findViewById(R.id.button_control_input_restaurant);
		intFood = R.drawable.input_restaurant;
		ibFood.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity) getActivity()).getPauseSuggestions(IMilestone.Category.FOOD);
				Log.d("INPUTBUTTON", "Food");
				if(intFood == R.drawable.input_restaurant || intFood == R.drawable.input_restaurant_shaded){
					ibFood.setImageResource(R.drawable.input_restaurant_pressed);
					intFood = R.drawable.input_restaurant_pressed;
					shadeIcons(intFood);
				}
				else {
					ibFood.setImageResource(R.drawable.input_restaurant);
					intFood = R.drawable.input_restaurant;
				}
			}
		});
		ibToilet = (ImageButton) view.findViewById(R.id.button_control_input_toilet);
		intToilet = R.drawable.input_toilet;
		ibToilet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//TODO: Fix Toilet...
				Log.d("INPUTBUTTON", "Toilet");
				if(intToilet == R.drawable.input_toilet || intToilet == R.drawable.input_toilet_shaded ){
					ibToilet.setImageResource(R.drawable.input_toilet_pressed);
					intToilet = R.drawable.input_toilet_pressed;
					shadeIcons(intToilet);
				}
				else {
					ibToilet.setImageResource(R.drawable.input_toilet);
					intToilet = R.drawable.input_toilet;
				}
			}
		});
		ibGasStation = (ImageButton) view.findViewById(R.id.button_control_input_gasstation);
		intGasStation = R.drawable.input_gasstation;
		ibGasStation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity)getActivity()).getPauseSuggestions(IMilestone.Category.GASSTATION);
				Log.d("INPUTBUTTON", "Gas Station");
				if(intGasStation == R.drawable.input_gasstation || intGasStation == R.drawable.input_gasstation_shaded ){
					ibGasStation.setImageResource(R.drawable.input_gasstation_pressed);
					intGasStation = R.drawable.input_gasstation_pressed;
					shadeIcons(intGasStation);
				}
				else {
					ibGasStation.setImageResource(R.drawable.input_gasstation);
					intGasStation = R.drawable.input_gasstation;
				}
			}
		});

		((MultiPaneActivity)getActivity()).readyToSetState();

		return view;
	}

	private void shadeIcons(int status){
		if(status == R.drawable.input_gasstation_pressed){
			ibRestArea.setImageResource(R.drawable.input_restarea_shaded);
			ibToilet.setImageResource(R.drawable.input_toilet_shaded);
			ibFood.setImageResource(R.drawable.input_restaurant_shaded);
		} else if(status == R.drawable.input_restarea_pressed){
			ibGasStation.setImageResource(R.drawable.input_gasstation_shaded);
			ibToilet.setImageResource(R.drawable.input_toilet_shaded);
			ibFood.setImageResource(R.drawable.input_restaurant_shaded);
		} else if(status == R.drawable.input_restaurant_pressed){
			ibGasStation.setImageResource(R.drawable.input_gasstation_shaded);
			ibRestArea.setImageResource(R.drawable.input_restarea_shaded);
			ibToilet.setImageResource(R.drawable.input_toilet_shaded);
		} else if(status == R.drawable.input_toilet_pressed){
			ibGasStation.setImageResource(R.drawable.input_gasstation_shaded);
			ibRestArea.setImageResource(R.drawable.input_restarea_shaded);
			ibFood.setImageResource(R.drawable.input_restaurant_shaded);
		} else{
			Log.d("ControlFragment", "shadeIcons");
		}
	}

	public void setState(State newState) {

		currentState = newState;

		if (currentState == State.ROUTELESS) {
			setStateRouteless();
		} else if (currentState == State.INFO) {
			setStateInfo();
		} else if (currentState == State.SUGGESTION) {
			setStateSuggestion();
		} else {
			setStateRouteless();
		}
	}

	private void setStateRouteless() {
		navigationInfoContainer.setVisibility(View.GONE);
		suggestionButtonContainer.setVisibility(View.GONE);
	}

	private void setStateInfo() {
		textRatingNextStop.setVisibility(View.GONE);
		suggestionButtonContainer.setVisibility(View.GONE);
	}

	private void setStateSuggestion() {
		;
	}

}