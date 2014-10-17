package com.edit.reach.fragments;



import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ProgressBar;
import com.edit.reach.activities.MovingActivity;
import com.edit.reach.activities.MultiPaneActivity;
import com.edit.reach.app.R;
import com.edit.reach.constants.MovingState;
import com.edit.reach.constants.SignalType;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class ControlFragment extends Fragment {

    private float fuelLevel;
    private double secToTimeStop;
    private double totalTime;
    private String nextStop;

    //Progressbar
    private ProgressBar fuelBar;
    private ProgressBar nextStopBar;
    private ProgressBar timeClockBar;

    /*
    // A handler for the UI thread. The Handler recieves messages from other thread.
    private Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {

            switch (SignalType) {

                case : fuelBar.setProgress((int) fuelLevel);
                break;

                case : nextStopBar.setProgress();

        }
    };*/


    public ControlFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        fuelBar = (ProgressBar) getView().findViewById(R.id.progress_gas);

        return inflater.inflate(R.layout.fragment_control, container, false);
    }


}
