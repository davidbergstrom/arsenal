package com.edit.reach.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.edit.reach.activities.MultiPaneActivity;
import com.edit.reach.app.R;
import com.edit.reach.constants.UniversalConstants;
import com.edit.reach.model.interfaces.IMilestone;

import java.util.List;

/**
 * Created by Erik Nordmark on 2014-10-21.
 */
public class SuggestionFragment extends Fragment {

	private String mId;
	private static final String ARG_ID = "Id";

    //Text Views
    private TextView textNextStop;
    private TextView textRating;
    //Suggestion Buttons
    private Button buttonConfirm;
    private Button buttonNext;
    //Images Views
    private ImageView ivGastation;
    private ImageView ivFood;
    private ImageView ivRestArea;
    private ImageView ivToilet;

    public void setMilestone(IMilestone milestone) {
        String nextStopName = milestone.getName();
        int rating = milestone.getRank();
        List<IMilestone.Category> categories = milestone.getCategories();

        textNextStop.setText(nextStopName);
        textRating.setText(rating);

        //Set Milestone Images
        ivFood.setVisibility(ImageView.GONE);
        ivGastation.setVisibility(ImageView.GONE);
        ivRestArea.setVisibility(ImageView.GONE);
        ivToilet.setVisibility(ImageView.GONE);

        for (IMilestone.Category cat : categories) {

            switch (cat) {
                case FOOD:
                    ivFood.setVisibility(ImageView.VISIBLE);
                    break;

                case GASSTATION:
                    ivGastation.setVisibility(ImageView.VISIBLE);
                    break;

                case RESTAREA:
                    ivRestArea.setVisibility(ImageView.VISIBLE);
                    break;

                case TOILET:
                    ivToilet.setVisibility(ImageView.VISIBLE);
                    break;

            }
        }
    }

	public static SuggestionFragment newInstance(String id) {
		SuggestionFragment fragment = new SuggestionFragment();
		Bundle args = new Bundle();
		args.putString(ARG_ID, id);
		fragment.setArguments(args);
		return fragment;
	}
	public SuggestionFragment() {
		// Required empty public constructor
	}

    //TODO delede?
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mId = getArguments().getString(ARG_ID);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_suggestion, container, false);

        //Get Suggestion Info
        textNextStop = (TextView) view.findViewById(R.id.tv_navigation_info_title);
        textRating = (TextView) view.findViewById(R.id.tv_navigation_info_rating);

        //Get Suggestion Buttons
        buttonNext = (Button) view.findViewById(R.id.suggestion_button_next);
        buttonConfirm = (Button) view.findViewById(R.id.suggestion_button_ok);

        //Get ImageViews
        ivFood = (ImageView) view.findViewById(R.id.navigation_info_icon_type_food);
        ivGastation = (ImageView) view.findViewById(R.id.navigation_info_icon_type_gasstation);
        ivRestArea = (ImageView) view.findViewById(R.id.navigation_info_icon_type_restarea);
        ivToilet = (ImageView) view.findViewById(R.id.navigation_info_icon_type_toilet);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);


	}
}
