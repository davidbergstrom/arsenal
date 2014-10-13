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
    private final ArrayList<String> milestonesNames;
    private final ArrayList<String> milestonesTypes;
    private static LayoutInflater inflater = null;
    private final Context context;

    public MilestonesListAdapter(Activity activity, ArrayList<String> milestonesNames, ArrayList<String> milestonesTypes) {
        this.activity = activity;
        this.milestonesNames = milestonesNames;
        this.milestonesTypes = milestonesTypes;
        this.context = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_milestones_item, null);
        }

        Holder holder = new Holder();

        convertView = inflater.inflate(R.layout.list_milestones_item, null);
        holder.textView = (TextView) convertView.findViewById(R.id.list_milestones_item);
        holder.imageView = (ImageView) convertView.findViewById(R.id.list_milestones_image);

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

        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ;
            }
        });

        return convertView;

    }

    @Override
    public int getCount() {
        return milestonesNames.size();
    }

    @Override
    public Object getItem(int position) {
        return milestonesNames.get(position);
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

