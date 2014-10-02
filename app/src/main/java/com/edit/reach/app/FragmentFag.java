package com.edit.reach.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by simonlarssontakman on 2014-10-01.
 */
public class FragmentFag extends Fragment {
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState){
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.pretrip, container, false);

        boolean shouldCreateChild = getArguments().getBoolean("shouldYouCreateAChildFragment");

        if(shouldCreateChild){
            FragmentManager fm  = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            fm.beginTransaction();
            Fragment fragTwo = new FragmentFag();
            Bundle arguments = new Bundle();
            arguments.putBoolean("ShouldCreateAChildFragment", false);
            fragTwo.setArguments(arguments);
            //ft.add(R.id.);
        }
        return layout;

    }
}
