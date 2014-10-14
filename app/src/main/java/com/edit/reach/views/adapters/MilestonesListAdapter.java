package com.edit.reach.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.edit.reach.app.R;

import java.util.ArrayList;

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
            convertView = layoutInflater.inflate(R.layout.route_list_item, null);
        }

		TextView textView = (TextView) convertView.findViewById(R.id.text_route_list_item);

        textView.setText(milestonesNames.get(position));

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
}

