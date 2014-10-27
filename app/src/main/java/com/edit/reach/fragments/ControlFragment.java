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
import com.edit.reach.utils.TimeConvert;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
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


    //State of Panel
	private State currentState;

    public enum State {
		ROUTELESS, INFO, SUGGESTION
	}

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
            textTimeToNextStop.setText(TimeConvert.convertTime(((int) (nextStopClock * UniversalConstants.SECONDS_TO_MINUTES))));
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

    public void setTotalTime(double totalTime) {
        textTotalTime.setText(TimeConvert.convertTime((int) (totalTime * UniversalConstants.SECONDS_TO_MINUTES)));
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

		//Get Layout Containers to easily handle states
		navigationInfoContainer = (LinearLayout) view.findViewById(R.id.navigation_info_container);

        //Get TextViews
        textNextStop = (TextView) view.findViewById(R.id.tv_navigation_info_title);
        textTotalTime = (TextView) view.findViewById(R.id.tv_navigation_info_total_time_left);
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
				if(isRestArea == ImageState.ORIGINAL){
					ibRestArea.setImageResource(R.drawable.restarea_pressed);
					isRestArea = ImageState.PRESSED;
					showTheOtherIcons();

				}
				else {
					ibRestArea.setImageResource(R.drawable.restarea_150);
					isRestArea = ImageState.ORIGINAL;
				}

			}
		});
		ibFood = (ImageButton) view.findViewById(R.id.button_control_input_restaurant);
		isFood = ImageState.ORIGINAL;
		ibFood.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity) getActivity()).getPauseSuggestions(IMilestone.Category.FOOD);
				Log.d("INPUTBUTTON", "Food");
				if(isFood == ImageState.ORIGINAL) {
					ibFood.setImageResource(R.drawable.food_pressed);
					isFood = ImageState.PRESSED;
					showTheOtherIcons();
				} else {
					ibFood.setImageResource(R.drawable.food_150);
					isFood = ImageState.ORIGINAL;
				}

			}
		});
		ibRestroom = (ImageButton) view.findViewById(R.id.button_control_input_toilet);
		isRestroom = ImageState.ORIGINAL;
		ibRestroom.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity)getActivity()).getPauseSuggestions(IMilestone.Category.TOILET);
				Log.d("INPUTBUTTON", "Toilet");
				if (isRestroom == ImageState.ORIGINAL) {
					ibRestroom.setImageResource(R.drawable.restroom_pressed);
					isRestroom = ImageState.PRESSED;
					showTheOtherIcons();

				} else {
					ibRestroom.setImageResource(R.drawable.restroom_150);
					isRestroom = ImageState.ORIGINAL;
				}

			}
		});
		ibGasStation = (ImageButton) view.findViewById(R.id.button_control_input_gasstation);
		isGasStation = ImageState.ORIGINAL;
		ibGasStation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity)getActivity()).getPauseSuggestions(IMilestone.Category.GASSTATION);
				Log.d("INPUTBUTTON", "Gas Station");
				if(isGasStation == ImageState.ORIGINAL){
					ibGasStation.setImageResource(R.drawable.gasstation_pressed);
					isGasStation = ImageState.PRESSED;
					showTheOtherIcons();
				}
				else {
					ibGasStation.setImageResource(R.drawable.gasstation_150);
					isGasStation = ImageState.ORIGINAL;
				}

			}
		});

		((MultiPaneActivity)getActivity()).readyToSetState();

		return view;
	}
	//TODO: Use if we going to have Multiple choices.
	private void showTheOtherIcons(){
		if(isGasStation == ImageState.PRESSED){
			ibRestArea.setImageResource(R.drawable.restarea_150);
			isRestArea = ImageState.ORIGINAL;

			ibRestroom.setImageResource(R.drawable.restroom_150);
			isRestroom = ImageState.ORIGINAL;

			ibFood.setImageResource(R.drawable.food_150);
			isFood = ImageState.ORIGINAL;

		} else if(isRestArea == ImageState.PRESSED){

			ibGasStation.setImageResource(R.drawable.gasstation_150);
			isGasStation = ImageState.ORIGINAL;

			ibRestroom.setImageResource(R.drawable.restroom_150);
			isRestroom = ImageState.ORIGINAL;

			ibFood.setImageResource(R.drawable.food_150);
			isFood = ImageState.ORIGINAL;

		} else if(isFood == ImageState.PRESSED){

			ibGasStation.setImageResource(R.drawable.gasstation_150);
			isGasStation = ImageState.ORIGINAL;

			ibRestArea.setImageResource(R.drawable.restarea_150);
			isRestArea = ImageState.ORIGINAL;

			ibRestroom.setImageResource(R.drawable.restroom_150);
			isRestroom = ImageState.ORIGINAL;

		} else if(isRestroom == ImageState.PRESSED){

			ibGasStation.setImageResource(R.drawable.gasstation_150);
			isGasStation = ImageState.ORIGINAL;

			ibRestArea.setImageResource(R.drawable.restarea_150);
			isRestArea = ImageState.ORIGINAL;

			ibFood.setImageResource(R.drawable.food_150);
			isFood = ImageState.ORIGINAL;

		} else{
			Log.d("ControlFragment", "shadeIcons");
		}
	}

	public void showAllIcons(){
		ibFood.setImageResource(R.drawable.food_150);
		ibRestArea.setImageResource(R.drawable.restarea_150);
		ibRestroom.setImageResource(R.drawable.restroom_150);
		ibGasStation.setImageResource(R.drawable.gasstation_150);

		isFood = ImageState.ORIGINAL;
		isRestArea = ImageState.ORIGINAL;
		isRestroom = ImageState.ORIGINAL;
		isGasStation = ImageState.ORIGINAL;
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
	}

	private void setStateInfo() {
		;
	}

	private void setStateSuggestion() {
		;
	}

}