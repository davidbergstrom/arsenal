package com.edit.reach.fragments;




import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;


import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.widget.ProgressBar;
import android.widget.TextView;

import android.widget.ImageButton;

import com.edit.reach.app.R;
import com.edit.reach.constants.UniversalConstants;
import com.edit.reach.constants.SignalType;

import com.edit.reach.model.NavigationModel;
import com.edit.reach.model.interfaces.IMilestone;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class ControlFragment extends Fragment{

    private static final String ARG_ID = "Control";
    private ImageButton ibRestArea;
    private ImageButton ibRestaurant;
    private ImageButton ibToilet;
    private ImageButton ibGasStation;
    private NavigationModel navigationModel;



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
                    timeClockBar.setMax((int)(UniversalConstants.LEGAL_UPTIME_IN_SECONDS * UniversalConstants.SECONDS_TO_MINUTES));
                    timeClockBar.setProgress((int) (timeClock * UniversalConstants.SECONDS_TO_MINUTES));
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            navigationModel = NavigationModel.getInstance();
        }
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

        ibRestArea = (ImageButton) view.findViewById(R.id.button_control_input_restarea);
        ibRestArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        ibRestaurant = (ImageButton)view.findViewById(R.id.button_control_input_restaurant);
        ibRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        ibToilet = (ImageButton) view.findViewById(R.id.button_control_input_toilet);
        ibToilet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        ibGasStation = (ImageButton) view.findViewById(R.id.button_control_input_gasstation);
        ibGasStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        return view;
    }



}
