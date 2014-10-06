package com.edit.reach.app.stationary;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.edit.reach.app.R;

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

    private Button btGetRoute;
    private Button btAddDestination;

    private List<EditText> editTextList = new ArrayList<EditText>();






    private OnRouteInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param id Id of the fragment
     * @return A new instance of fragment EditRouteFragment.
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
        btGetRoute = (Button) getView().findViewById(R.id.bt_get_route);
        btGetRouteOnClick();
        btAddDestination = (Button)getView().findViewById(R.id.bt_add_destination);
        btAddDestinationOnClick();
    }

    public void btGetRouteOnClick(){
        btGetRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("getRoute banananananaz")
            }
        });
    }

    public void btAddDestinationOnClick(){
        btAddDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_route, container, false);
		return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onRouteInteraction(uri);


        }
    }

    private EditText editText(int _intID){
        EditText editText = new EditText(getActivity());
        editText.setId(_intID);
        editText.setHint("By");
        editText.setWidth(180);
        editTextList.add(editText);
        return editText;

    }

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
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
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
        public void onRouteInteraction(Uri uri);
    }




}
