package com.edit.reach.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.edit.reach.activities.MultiPaneActivity;
import com.edit.reach.app.R;
import com.edit.reach.model.IMilestone;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SuggestionFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SuggestionFragment extends Fragment {

	private String mId;
	private static final String ARG_ID = "Id";

    //Text Views
    private TextView textNextStop;
    private TextView textRating;
    private TextView textDescription;

    //Images Views
    private ImageView ivGasstation;
    private ImageView ivFood;
    private ImageView ivRestArea;
    private ImageView ivToilet;

	private IMilestone milestone;

    public SuggestionFragment() {
        // Required empty public constructor
    }

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
        Log.d("SuggestionFragment", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_suggestion, container, false);

        //Get Suggestion Info
        textNextStop = (TextView) view.findViewById(R.id.tv_navigation_info_title);
        textRating = (TextView) view.findViewById(R.id.tv_navigation_info_rating);
        textDescription = (TextView) view.findViewById(R.id.tv_navigation_info_description);

        //Get ImageViews
        ivFood = (ImageView) view.findViewById(R.id.navigation_info_icon_type_food);
        ivGasstation = (ImageView) view.findViewById(R.id.navigation_info_icon_type_gasstation);
        ivRestArea = (ImageView) view.findViewById(R.id.navigation_info_icon_type_restarea);
        ivToilet = (ImageView) view.findViewById(R.id.navigation_info_icon_type_toilet);

        //Get Suggestion Buttons
        Button buttonConfirm = (Button) view.findViewById(R.id.button_confirm);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MultiPaneActivity)getActivity()).suggestionAcceptMilestone(true);
            }
        });
        Button buttonNext = (Button) view.findViewById(R.id.button_next);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MultiPaneActivity)getActivity()).suggestionAcceptMilestone(false);
            }
        });
        Button cancel = (Button) view.findViewById(R.id.button_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MultiPaneActivity)getActivity()).goBackToControlFragment();
            }
        });

        if(milestone != null){
            setMilestone(milestone);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

	/**
	 * Sets the suggested Milestone information to the GUI
	 * @param milestone A IMilestone-object we get from the handler
	 */
    public void setMilestone(IMilestone milestone) {
	    this.milestone = milestone;

        String nextStopName = milestone.getName();
        int rating = milestone.getRank();
        List<IMilestone.Category> categories = milestone.getCategories();

	    if(textNextStop != null){
		    textNextStop.setText(nextStopName);
		    textRating.setText("" + rating + "/5");
            textDescription.setText(milestone.getDescription());

		    //Set Milestone Images
		    ivFood.setVisibility(ImageView.GONE);
		    ivGasstation.setVisibility(ImageView.GONE);
		    ivRestArea.setVisibility(ImageView.GONE);
		    ivToilet.setVisibility(ImageView.GONE);

		    for (IMilestone.Category cat : categories) {

			    switch (cat) {
				    case FOOD:
					    ivFood.setVisibility(ImageView.VISIBLE);
					    break;

				    case GASSTATION:
					    ivGasstation.setVisibility(ImageView.VISIBLE);
					    break;

				    case RESTAREA:
					    ivRestArea.setVisibility(ImageView.VISIBLE);
					    break;

				    case TOILET:
					    ivToilet.setVisibility(ImageView.VISIBLE);
					    break;

			    }
		    }
		    ((MultiPaneActivity)getActivity()).setSgsFragmentHasBeenCreated(true);
	    }
    }

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 * @param id Id of the fragment
	 * @return A new instance of SuggestionFragment
	 */
	public static SuggestionFragment newInstance(String id) {
		SuggestionFragment fragment = new SuggestionFragment();
		Bundle args = new Bundle();
		args.putString(ARG_ID, id);
		fragment.setArguments(args);
		return fragment;
	}
}