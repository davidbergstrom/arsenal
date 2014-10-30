package com.edit.reach.views.widgets;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.edit.reach.app.R;
import com.edit.reach.model.IMilestone;

/**
 * MilestonesCard, to create visual info from the Milestone, extends RelativeLayout
 */
public class MilestonesCard extends RelativeLayout {

    private final ImageView imageView;
    private final TextView milestoneName;
    private final IMilestone milestone;

	/**
	 * A constructor to create visual info from the Milestone
	 * @param context calls for super
	 * @param milestone the Milestone we will show
	 */
    public MilestonesCard(Context context, IMilestone milestone) {
        super(context);
        inflate(getContext(), R.layout.card_milestone, this);
        this.milestoneName = (TextView)findViewById(R.id.card_milestone_text);
        this.imageView = (ImageView)findViewById(R.id.card_milestone_icon);
        this.milestone = milestone;
        //setCategoryImage(milestone.getCategories());
        setMilestoneName(milestone.getName());
        setCategoryImage();
    }

    private void setCategoryImage(){
	    Log.d("MilestonesCard", "SetCategoryImage");
	    if (milestone.hasCategory(IMilestone.Category.GASSTATION)) {
            imageView.setImageResource(R.drawable.milestone_gas);
	    } else if (milestone.hasCategory(IMilestone.Category.FOOD)) {
		    imageView.setImageResource(R.drawable.milestone_food);
	    } else if (milestone.hasCategory(IMilestone.Category.TOILET)) {
		    imageView.setImageResource(R.drawable.milestone_toilet);
	    } else {
		    imageView.setImageResource(R.drawable.milestone_rest);
	    }


    }

    private void setMilestoneName(String text){
        milestoneName.setText(text);
    }

	/**
	 * To get the Milestone
	 * @return the Milestone
	 */
	public IMilestone getMilestone(){
        return milestone;
    }

}
