package com.edit.reach.app;



import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;


/**
 * Created by simonlarssontakman on 2014-10-01.
 */
public class FragmentTest extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pretrip_map);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        fm.beginTransaction();
        Fragment fragOne = new FragmentFag();
        Bundle arguments = new Bundle();
        arguments.putBoolean("shos", true);
        fragOne.setArguments(arguments);
        //ft.add(R.id.);
        ft.commit();
    }

}
