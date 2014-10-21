package com.edit.reach.fragments;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.*;
import com.edit.reach.activities.MainActivity;
import com.edit.reach.activities.MultiPaneActivity;
import com.edit.reach.app.R;
import com.edit.reach.model.Route;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RouteFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class RouteFragment extends Fragment {

	private static final String ARG_ID = "Route";
	private String mId;



	private AutoCompleteTextView actFrom;
	private AutoCompleteTextView actTo;
	private ToggleButton tbCurLoc;
	private List<EditText> etListOfVia;
	private List<String> matchedPlaces;
	private TextView tvMatchedListItem;
	private ProgressBar spinner;
	private ArrayAdapter<String> adapter;
	private ImageButton ibNext;
	private Button btNext;
	private boolean myCurrentLocationActivated;



	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param id Id of the fragment
	 * @return A new instance of fragment RouteFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static RouteFragment newInstance(String id) {
		RouteFragment fragment = new RouteFragment();
		Bundle args = new Bundle();
		args.putString(ARG_ID, id);
		fragment.setArguments(args);
		return fragment;
	}



	public RouteFragment() {
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
		View view = inflater.inflate(R.layout.fragment_route, container, false);

        spinner = (ProgressBar)view.findViewById(R.id.spinner);
        etListOfVia = new ArrayList<EditText>();

		ibNext = (ImageButton) view.findViewById(R.id.ib_route_navigation_next);
		ibNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getNearestRoute();
			}
		});
		btNext = (Button) view.findViewById(R.id.bt_route_navigation_next);
		btNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getNearestRoute();
			}
		});

		actFrom = (AutoCompleteTextView) view.findViewById(R.id.autocomplete_route_from);
		if(myCurrentLocationActivated){
			actFrom.setEnabled(false);
		} else{
			actFrom.setEnabled(true);
		}
		actFrom.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View view, int i, KeyEvent keyEvent) {
				return false;
			}
		});

		// Listener that listens to search field and reacts when text is changed
		actFrom.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 4) {
                    onTextEntered(s.toString());
                }
			}

			@Override
			public void afterTextChanged(Editable s) {}
		});

        actTo = (AutoCompleteTextView) view.findViewById(R.id.autocomplete_route_to);
        actTo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                return false;
            }
        });

        // Listener that listens to search field and reacts when text is changed
        actTo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 4) {
                    onTextEntered(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

		tvMatchedListItem = (TextView) view.findViewById(R.id.text_route_list_item);
		tbCurLoc = (ToggleButton) view.findViewById(R.id.toggle_my_location);
		tbCurLoc.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(((ToggleButton) view).isChecked()){
					myCurrentLocationActivated = true;
					actFrom.setText("My Location");
					actFrom.setEnabled(false);

				} else {
					myCurrentLocationActivated = false;
					actFrom.setText("");
					actFrom.setEnabled(true);
				}
			}
		});
/*		Button btGetNearestRoute = (Button) view.findViewById(R.id.btSubmitNearestRoute);
		btGetNearestRoute.setOnClickListener(getNearestRouteListener);*/

		return view;
	}

	private View.OnClickListener getNearestRouteListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			getNearestRoute();
		}
	};

	/**
	 * Finds the nearest route
	 * Sends to MultipaneActivity with either my location or user input from and input to.
	 */

	private void getNearestRoute(){
		String strTo = actTo.getText().toString();
		((MultiPaneActivity)getActivity()).showSpinner();
		if(myCurrentLocationActivated){
			((MultiPaneActivity)getActivity()).createRouteWithMyLocation(strTo);
		}else {
			String strFrom = actFrom.getText().toString();
			((MultiPaneActivity)getActivity()).createRoute(strFrom, strTo);
		}
	}

	public void onTextEntered(String text) {
		((MultiPaneActivity)getActivity()).getMatchedStringResults(text);
	}

	public void setRouteText(String from, String to){
		actFrom.setText(from);
		actTo.setText(to);
	}


	//Kan behövas för att dynamiskt lägga till fler textfält för del-destinationer
	private LinearLayout linearLayout (int _intID){
		LinearLayout ll = new LinearLayout(getActivity());
		ll.setId(_intID);

		return ll;
	}

	// Receives a list of places related to the entered text in search field
	// and gives suggestions.
	public void suggestionList(List<String> resultList) {
		matchedPlaces = resultList;
		adapter = new ArrayAdapter<String>(this.getActivity(), R.layout.route_list_item, matchedPlaces);
		actFrom.setAdapter(adapter);
        actTo.setAdapter(adapter);
	}







}
