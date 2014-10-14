package com.edit.reach.views.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;

/**
 * Created by iDavid on 2014-10-09.
 */


public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    private ArrayList<String> resultsList;

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
/*					resultsList = autocomplete(constraint.toString());*/
				}
                return filterResults;
			}

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            }
        };

        return filter;
	}

}

