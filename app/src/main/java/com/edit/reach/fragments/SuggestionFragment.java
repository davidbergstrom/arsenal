package com.edit.reach.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.edit.reach.activities.MultiPaneActivity;
import com.edit.reach.app.R;

/**
 * Created by simonlarssontakman on 2014-10-21.
 */
public class SuggestionFragment extends Fragment {

	private String mId;
	private static final String ARG_ID = "Id";

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
		View view = inflater.inflate(R.layout.fragment_milestones, container, false);

		return view;
	}




	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);


	}
}
