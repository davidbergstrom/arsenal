package com.edit.reach.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.*;
import com.edit.reach.activities.MultiPaneActivity;
import com.edit.reach.app.R;
import com.edit.reach.model.Route;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RouteFragment.OnRouteInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RouteFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class RouteFragment extends Fragment {

    private static final String ARG_ID = "Route";
    private String mId;
    private Route route;
    private String strFrom;
    private String strTo;

    private AutoCompleteTextView actFrom;
    private AutoCompleteTextView actTo;
    private ToggleButton tbCurLoc;
    private List<EditText> etListOfVia;
    private List<String> lMatchedStrings;
    private TextView tvMatchedListItem;

    private OnRouteInteractionListener mListener;

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

    private View.OnClickListener addDestinationListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            editText();
        }
    };

    private View.OnClickListener getNearestRouteListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            strFrom = actFrom.getText().toString();
            strTo = actTo.getText().toString();
            route = new Route(strFrom, strTo);
            onSetRoute(route);


           /* List<String> strListOfVia = new ArrayList<String>();
            for(EditText et: etListOfVia){
                strListOfVia.add(et.getText().toString());
            }

            */


            //Send : strFrom, strTo, strListOfVia to map-Activity

        }
    };

    public String getStrFrom(){
        return strFrom;
    }

    public String getStrTo(){
        return strTo;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_route, container, false);

        actFrom = (AutoCompleteTextView) view.findViewById(R.id.autocomplete_route_from);
        actTo = (AutoCompleteTextView) view.findViewById(R.id.autocomplete_route_to);

        etListOfVia = new ArrayList<EditText>();
        lMatchedStrings = new ArrayList<String>();
        actFrom.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {


                return false;
            }
        });
        tvMatchedListItem = (TextView) view.findViewById(R.id.text_route_list_item);
        tbCurLoc = (ToggleButton) view.findViewById(R.id.toggle_my_location);
        tbCurLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((ToggleButton) view).isChecked()){
                    actFrom.setText("My Location");
                    actFrom.setEnabled(false);
                } else {
                    actFrom.setText("");
                    actFrom.setEnabled(true);
                }
            }
        });
        Button btGetNearestRoute = (Button) view.findViewById(R.id.btSubmitNearestRoute);
        btGetNearestRoute.setOnClickListener(getNearestRouteListener);

		return view;
    }


    public void onSetRoute(Route route) {
        if (mListener != null) {
            mListener.onRouteInteraction(route);


        }
    }

    //Kan behövas för att dynamiskt lägga till fler textfält för del-destinationer
    private EditText editText(){
        EditText editText = new EditText(getActivity());
        editText.setHint("By");
        editText.setWidth(180);
        etListOfVia.add(editText);
        return editText;

    }

    //Kan behövas för att dynamiskt lägga till fler textfält för del-destinationer
    private LinearLayout linearLayout (int _intID){
        LinearLayout ll = new LinearLayout(getActivity());
        ll.setId(_intID);


        return ll;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnRouteInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Must implement OnRouteInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnRouteInteractionListener {
        // TODO: Update argument type and name
        public void onRouteInteraction(Object o);
    }





}
