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
 * A class that gives location suggestions for a given search String.
 */
public class SuggestionUtil implements ResponseHandler {

    private SuggestionListener suggestionListener;
    private String searchString;

    public SuggestionUtil(SuggestionListener suggestionListener) {
        this.suggestionListener = suggestionListener;
    }

    /** Handler for receiving the search result as a JSON Object */
    @Override
    public void onGetSuccess(JSONObject json) {
        try {
            JSONArray resultsArray = json.getJSONArray("results");

            List<String> resultList = new ArrayList<String>();

            for (int i = 0; i < resultsArray.length(); i++) {
                JSONArray addressComponent = ((JSONObject)resultsArray.get(i)).getJSONArray("address_components");

                for (int j = 0; j < addressComponent.length(); j++) {

                    String potentialMatch = ((JSONObject)addressComponent.get(j)).getString("short_name").toLowerCase();

                    if (potentialMatch.contains(searchString)) {

                        resultList.add(potentialMatch);
                        Log.d("TestClass", "" + "" + resultList.size() + ": "+ potentialMatch);
                        break;
                    }
                }
            }

            suggestionListener.onGetSuccess(resultList);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGetFail() {

    }

    /**
     * Search in the Google Geocode API for the given String
     * @param str, search String
     */
    public void searchForAddresses(String str) {
        searchString = str.toLowerCase();
        URL url = GoogleMapsEndpoints.makeURL(searchString);
        Remote.get(url, this);
    }
}
