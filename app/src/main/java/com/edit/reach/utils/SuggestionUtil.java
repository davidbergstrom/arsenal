package com.edit.reach.utils;

import android.util.Log;
import com.edit.reach.model.interfaces.SuggestionListener;
import com.edit.reach.system.GoogleMapsEndpoints;
import com.edit.reach.system.Remote;
import com.edit.reach.system.ResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nordmark on 2014-10-13.
 * A class that gives location suggestions for a given search String using Google Places.
 */
public class SuggestionUtil implements ResponseHandler {

    private final SuggestionListener suggestionListener;
    private String searchString;

    public SuggestionUtil(SuggestionListener suggestionListener) {
        this.suggestionListener = suggestionListener;
    }

    /** Handler for receiving the search result as a JSON Object */
    @Override
    public void onGetSuccess(JSONObject json) {
        try {
            JSONArray predictions = json.getJSONArray("predictions");

            Log.d("Suggestion", " " + predictions);

            List<String> resultList = new ArrayList<String>();

                for (int i = 0; i < predictions.length(); i++) {
                    resultList.add(((JSONObject)predictions.get(i)).getString("description"));
                    Log.d("Suggestion", " " + ((JSONObject)predictions.get(i)).getString("description"));
                }

            suggestionListener.onGetSuccess(resultList);

            } catch (JSONException e) {
                e.printStackTrace();
        }

    }

    @Override
    public void onGetFail() {
	    Log.d("Suggestion", "Suggestion failed");
    }

    /**
     * Search in the Google Places API for the given String
     * @param str, search String
     */
    public void searchForAddresses(String str) {
        Log.d("Suggestion", " " + str);
        searchString = str.toLowerCase();
        URL url = GoogleMapsEndpoints.makeURLPlaces(searchString);
        Remote.get(url, this);
    }
}

