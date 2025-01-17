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
import com.edit.reach.model.IMilestone;
import com.edit.reach.views.widgets.MilestonesCard;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment allows the user to add milestones to a chosen route in the stationary mode.
 */
public class MilestonesFragment extends Fragment {

	private static final String ARG_FROM = "From";
	private static final String ARG_TO = "To";
	private static final String ARG_LIST = "List";
	private static final String ARG_TYPE = "Type";

	private String mFrom;
	private String mTo;
	private ArrayList <String> mMilestonesList;
	private ArrayList <String> mMilestonesType;

	private Button btStartRoute;
	private ImageButton ibStartRoute;
	private Button btPrevious;
	private ImageButton ibPrevious;

	private static int n = 0;

	private ListView mMilestonesListView;
	private	TextView mFromTextView;
	private	TextView mToTextView;
	private LinearLayout cardList;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param from Start point of Route.
	 * @param to End point of Route.
	 * @return A new instance of fragment MilestonesFragment.
	 */
	public static MilestonesFragment newInstance(String from, String to) {
		MilestonesFragment fragment = new MilestonesFragment();
		Bundle args = new Bundle();
		args.putString(ARG_FROM, from);
		args.putString(ARG_TO, to);
		fragment.setArguments(args);
		return fragment;
	}

	public MilestonesFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mFrom = getArguments().getString(ARG_FROM);
			mTo = getArguments().getString(ARG_TO);
			mMilestonesList = getArguments().getStringArrayList(ARG_LIST);
			mMilestonesType = getArguments().getStringArrayList(ARG_TYPE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_milestones, container, false);
		//mMilestonesListView = () view.findViewById(R.id.lv_milestones);
		mFromTextView = (TextView) view.findViewById(R.id.tv_text_from);
		mToTextView = (TextView) view.findViewById(R.id.tv_text_to);
		cardList = (LinearLayout) view.findViewById(R.id.cardList);

		btStartRoute = (Button) view.findViewById(R.id.bt_milestone_navigation_start);
		ibStartRoute = (ImageButton) view.findViewById(R.id.ib_milestone_navigation_start);
		btStartRoute.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d("StartRoute", "Performed a StartRoute-click");
				((MultiPaneActivity)getActivity()).initializeMovingBackend();
				((MultiPaneActivity)getActivity()).initializeMovingUI();
				((MultiPaneActivity)getActivity()).addMilestones();
			}
		});
		ibStartRoute.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d("StartRoute", "Performed a StartRoute-click");
				((MultiPaneActivity)getActivity()).initializeMovingBackend();
				((MultiPaneActivity)getActivity()).initializeMovingUI();
				((MultiPaneActivity)getActivity()).addMilestones();
			}
		});

		btPrevious = (Button) view.findViewById(R.id.bt_milestone_navigation_back);
		ibPrevious = (ImageButton) view.findViewById(R.id.ib_milestone_navigation_back);
		ibPrevious.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity)getActivity()).changeMsFragmentHasBeenCreated(false);
				((MultiPaneActivity)getActivity()).goBackToRouteFragment();
			}
		});
		btPrevious.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				((MultiPaneActivity)getActivity()).changeMsFragmentHasBeenCreated(false);
				((MultiPaneActivity)getActivity()).goBackToRouteFragment();
			}
		});

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		//mMilestonesListView.setAdapter(new MilestonesListAdapter(this.getActivity(), mMilestonesList, mMilestonesType));

		if(((MultiPaneActivity)getActivity()).routeWithCurrentLocation()){
			mFromTextView.setText("My Location");
		} else{
			mFromTextView.setText(mFrom);
		}
		mToTextView.setText(mTo);
	}

	/**
	 * To remove the milestonecard from the list in MilestonesFragment and from the GUI
	 * @param milestone the milestone we will remove
	 */
	public void removeMilestoneCard(IMilestone milestone){
		List<MilestonesCard> mcList = new ArrayList<MilestonesCard>();
		for(int i =0; i < cardList.getChildCount(); i++){
			View tmp = cardList.getChildAt(i);
			if(tmp instanceof MilestonesCard){
				MilestonesCard card = (MilestonesCard)cardList.getChildAt(i);
				if(card.getMilestone().equals(milestone)){
					cardList.removeViewAt(i);
					break;
				}
			}
		}

	}

	/**
	 * To add the MilestoneCard to the GUI and add it to the list
	 * @param milestone
	 */
	public void addMilestoneCard(IMilestone milestone){
		MilestonesCard mc = new MilestonesCard(getActivity().getApplicationContext(), milestone);
		cardList.addView(mc);

	}
}
