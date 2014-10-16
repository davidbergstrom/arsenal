package com.edit.reach.views.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.edit.reach.app.R;
import com.edit.reach.model.interfaces.IMilestone;

/**
 * Created by simonlarssontakman on 2014-10-11.
 */
public class MilestonesCard extends RelativeLayout {

    private ImageView imageView;
    private TextView milestoneName;
    private IMilestone milestone;


    public MilestonesCard(Context context, IMilestone milestone) {
        super(context);
        inflate(getContext(), R.layout.card_milestone, this);
        this.milestoneName = (TextView)findViewById(R.id.card_milestone_text);
        this.imageView = (ImageView)findViewById(R.id.card_milestone_icon);
        this.milestone = milestone;
        setCategoryImage(milestone.getCategory());
        setMilestoneName(milestone.getName());
    }

    public MilestonesCard(Context context, String name, IMilestone.Category c){
        super(context);
        inflate(getContext(), R.layout.card_milestone, this);
        this.milestoneName = (TextView)findViewById(R.id.card_milestone_text);
        this.imageView = (ImageView)findViewById(R.id.card_milestone_icon);
        setMilestoneName(name);
        setCategoryImage(c);

    }

    private void setCategoryImage(IMilestone.Category c){
        if(c == IMilestone.Category.RESTAURANT ){
            Log.d("Category", "Restaurant");
            //imageView.setImageResource(RestaurantLogo);
        } else if (c == IMilestone.Category.GASSTATION){
            Log.d("Category", "Gasstation");
            //imageView.setImageResource(Gaststationlogo);
        } else if(c == IMilestone.Category.RESTAREA){
            Log.d("Category", "Restarea");
            //imageView.setImageResource(RestAreaLogo);
        } else {
            Log.d("Wrong", "Wrong category");
        }
    }

    private void setMilestoneName(String text){
        milestoneName.setText(text);
    }

    // Add the Milestone to the layout
    public void addView(MilestonesCard mc, int _id){

    }

    public IMilestone getMilestone(){
        return milestone;
    }

}
