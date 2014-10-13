package com.edit.reach.views.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.edit.reach.app.R;

/**
 * Created by simonlarssontakman on 2014-10-11.
 */
public class MilestonesCard extends RelativeLayout {

    private ImageView ImageView;
    private TextView milestoneName;


    public MilestonesCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(getContext(), R.layout.card_milestone, this);
        this.milestoneName = (TextView)findViewById(R.id.test);
        this.ImageView = (ImageView)findViewById(R.id.icon);

    }

    public void setCategoryImage(int _id){
        ImageView.setImageResource(_id);
    }

    public void setMilestoneName(String text){
        milestoneName.setText(text);
    }

    // Add the Milestone to the layout
    public void addView(MilestonesCard mc, int _id){

    }


}
