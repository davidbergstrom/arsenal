package com.edit.reach.fragments;




import android.media.Image;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import com.edit.reach.app.R;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class ControlFragment extends Fragment{

    private static final String ARG_ID = "Control";
    private ImageButton ibRestArea;
    private ImageButton ibRestaurant;
    private ImageButton ibToilet;



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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_control, container, false);
        ibRestArea = (ImageButton) view.findViewById(R.id.ibrestarea);
        ibRestaurant = (ImageButton)view.findViewById(R.id.ibRestaurant);
        ibToilet = (ImageButton) view.findViewById(R.id.ibToilet);


        return view;
    }

    public void onClick(View view){
        if(view == ibRestArea){

        }else if(view == ibRestaurant){

        } else if(view == ibToilet){

        }
    }


}
