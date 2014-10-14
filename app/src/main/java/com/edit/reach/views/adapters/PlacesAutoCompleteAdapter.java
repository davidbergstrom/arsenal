package com.edit.reach.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.edit.reach.app.R;

import java.util.ArrayList;

/**
 * Created by iDavid on 2014-10-09.
 */


public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    private final ArrayList<String> resultsList

    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public int getCount() {
        return resultsList.size();
    }

    @Override
    public String getItem(int index) {
        return resultsList.get(index);
    }

	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults filterResults = new FilterResults();
				if (constraint != null) {
					// Retrieve the autocomplete results
					resultsList = autocomplete(constraint.toString());
				}
			}
		}
	}

}

