package com.edit.reach.stationary;

import android.content.Context;
import android.media.Image;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by simonlarssontakman on 2014-10-11.
 */
public class MilestonesCard extends View {

    private ImageView categoryImage;
    private TextView milestoneName;


    public MilestonesCard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCategoryImage(int _id){
        categoryImage.setImageResource(_id);
    }

    public void setMilestoneName(String text){
        milestoneName.setText(text);
    }

    // Add the Milestone to the layout
    public void addView(MilestonesCard mc, int _id){

    }

}
