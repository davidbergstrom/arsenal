package com.edit.reach.stationary;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.edit.reach.app.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by iDavid on 2014-10-09.
 */
public class MilestonesListAdapter extends BaseAdapter {

	private final Activity activity;

	private static LayoutInflater inflater = null;
	private final Context context;


	public MilestonesListAdapter(Activity activity) {
		this.activity = activity;
		this.context = activity;
	}

    public void addMilestone(HashMap <String, String> hm){
        for(int i = 0; i < hm.size(); i++){
            hm.get(i);
        }
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Holder holder = new Holder();
		View rowView;

		rowView = inflater.inflate(R.layout.list_milestones_item, null);
        /*
        holder.textView = (TextView) rowView.findViewById(R.id.list_milestones_item);
		holder.imageView = (ImageView) rowView.findViewById(R.id.list_milestones_image);

		holder.textView.setText(milestonesNames.get(position));

		if (milestonesTypes.get(position) == "RESTAURANT") {
			holder.imageView.setImageResource(R.drawable.ic_launcher); // TODO Change icon
		} else if (milestonesTypes.get(position) == "RESTAREA") {
			holder.imageView.setImageResource(R.drawable.ic_launcher); // TODO Change icon
		} else if (milestonesTypes.get(position) == "GASSTATION") {
			holder.imageView.setImageResource(R.drawable.ic_launcher); // TODO Change icon
		} else {
			holder.imageView.setImageResource(R.drawable.ic_launcher);
		}


        */
		rowView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				;
			}
		});

		return rowView;

	}



	@Override
	public int getCount() {
		//return milestonesNames.size();
        return 0;
	}

	@Override
	public Object getItem(int position) {
		//return milestonesNames.get(position);
        return null;
	}


	@Override
	public long getItemId(int position) {
		return 0;
	}

	public class Holder
	{
		TextView textView;
		ImageView imageView;
	}
}
