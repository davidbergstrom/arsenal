package com.edit.reach.fragments;




import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;


import android.media.Image;
import android.os.Bundle;

import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.widget.ProgressBar;
import android.widget.TextView;
import com.edit.reach.activities.MovingActivity;
import com.edit.reach.activities.MultiPaneActivity;

import android.widget.ImageButton;

import com.edit.reach.app.R;
import com.edit.reach.constants.Constants;
import com.edit.reach.constants.MovingState;
import com.edit.reach.constants.SignalType;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class ControlFragment extends Fragment{

    private static final String ARG_ID = "Control";
    private ImageButton ibRestArea;
    private ImageButton ibRestaurant;
    private ImageButton ibToilet;

    private float fuelLevel;
    private double nextStopClock; //in sec
    private double timeClock;   //in sec
    private double totalTime;   //in sec
    private String nextStopName = null;

    //Progressbar
    private ProgressBar fuelBar;
    private ProgressBar nextStopBar;
    private ProgressBar timeClockBar;

    // A handler for the UI thread. The Handler recieves messages from other thread.
    private Handler mainHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message message) {

            switch (message.what) {

                case SignalType.FUEL_UPDATE:
                    fuelLevel = (Float) message.obj;
                    fuelBar.setMax(100);
                    fuelBar.setProgress((int) fuelLevel);
                    break;

                case SignalType.UPTIME_UPDATE:
                    timeClock = (Double) message.obj;
                    timeClockBar.setMax((int)(Constants.LEGAL_UPTIME_IN_SECONDS * Constants.SECONDS_TO_MINUTES));
                    timeClockBar.setProgress((int) (timeClock * Constants.SECONDS_TO_MINUTES));
                    break;
            }
        }
    };



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

        TextView stopNameView = (TextView) view.findViewById(R.id.tv_control_info_top_card_title);

        if(nextStopName != null) {
            stopNameView.setText(nextStopName);
        } else {
            stopNameView.setText("N/A");
        }

        //Get progressbars
        fuelBar = (ProgressBar) view.findViewById(R.id.progress_gas);
        timeClockBar = (ProgressBar) view.findViewById(R.id.progress_time_clock);
        nextStopBar = (ProgressBar) view.findViewById(R.id.progress_next_stop);



        /*
        ibRestArea = (ImageButton) view.findViewById(R.id.ibrestarea);
        ibRestaurant = (ImageButton)view.findViewById(R.id.ibRestaurant);
        ibToilet = (ImageButton) view.findViewById(R.id.ibToilet);
        */

        return view;
    }

    public void onClick(View view){
        if(view == ibRestArea){

        }else if(view == ibRestaurant){

        } else if(view == ibToilet){

        }

    }


}
