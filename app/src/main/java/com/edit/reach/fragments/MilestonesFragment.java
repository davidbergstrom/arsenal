package com.edit.reach.fragments;

import android.app.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.*;
import com.edit.reach.activities.MovingActivity;
import com.edit.reach.activities.MultiPaneActivity;
import com.edit.reach.app.R;
import com.edit.reach.model.interfaces.IMilestone;
import com.edit.reach.views.widgets.MilestonesCard;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MilestonesFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
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

    private Button btEditRoute;
    private Button btStartRoute;

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
        btEditRoute = (Button) view.findViewById(R.id.button_edit_route);
        btStartRoute = (Button) view.findViewById(R.id.button_start_route);



        btEditRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MultiPaneActivity)getActivity()).goBackToRouteFragment();


            }
        });
        btStartRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("StartRoute", "Performed a StartRoute-click");
                ((MultiPaneActivity)getActivity()).initializeMovingBackend();
	            ((MultiPaneActivity)getActivity()).initializeMovingUI();
	            ((MultiPaneActivity)getActivity()).addMilestones();
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

    public void addMilestoneCard(IMilestone milestone){
        MilestonesCard mc = new MilestonesCard(getActivity().getApplicationContext(), milestone);
        cardList.addView(mc);

    }



}
