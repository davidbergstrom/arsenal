package com.edit.reach.model;

import android.util.Log;
import com.edit.reach.app.Remote;
import com.edit.reach.app.ResponseHandler;
import com.edit.reach.model.SuggestionListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nordmark on 2014-10-13.
 */
public class SuggestionSystem implements ResponseHandler {

    private SuggestionListener suggestionListener;
    private String searchString;

    public SuggestionSystem(SuggestionListener suggestionListener) {
        this.suggestionListener = suggestionListener;
    }

    @Override
    public void onGetSuccess(JSONObject json) {
        try {
            JSONArray resultsArray = json.getJSONArray("results");

            List<String> resultList = new ArrayList<String>();

            for (int i = 0; i < resultsArray.length(); i++) {
                JSONArray addressComponent = ((JSONObject)resultsArray.get(i)).getJSONArray("address_components");

                for (int j = 0; j < addressComponent.length(); j++) {
                    String potentialMatch = ((JSONObject)addressComponent.get(j)).getString("short_name");

                    if (potentialMatch.contains(searchString)) {

                        resultList.add(potentialMatch);
                        Log.d("Test", "" + "" + resultList.size());

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

    public void searchForAddresses(String str) {
        searchString = str;
        URL url = NavigationUtils.makeURL(str);
        Remote.get(url, this);
    }
}
