package com.edit.reach.model;

import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Milestone implements IMilestone {

    String name;
    String description;
    Category category;
    int rank;
    LatLng location;

    public Milestone(JSONObject json) throws JSONException {
        JSONObject properties = json.getJSONObject("properties");

        name = properties.getString("name");
        description = properties.getString("description");
        int rank = properties.getInt("ranking");

        int categoryGroup = properties.getInt("category");

        category = Category.RESTAREA;

        JSONObject geometry = json.getJSONObject("geometry");
        JSONArray coordinates = geometry.getJSONArray("coordinates");
        double latitude = coordinates.getDouble(0);
        double longitude = coordinates.getDouble(1);
        location = new LatLng(latitude, longitude);

    }

    private Category setCategory(int i) {
        switch (i) {
            case 1:
            case 2:
                return Category.RESTAURANT;
            break;
            case 3:
                return Category.GASSTATION;
            break;

        }
    }

    /**
     * Returns the name of the milestone
     *
     * @return the milestones name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the milestone
     *
     * @return the milestones description
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Returns the category of the milestone
     *
     * @return the milestones category
     */
    @Override
    public Category getCategory() {
        return category;
    }

    /**
     * Set the rank of the milestone
     *
     * @param newRank
     */
    @Override
    public void setRank(int newRank) {

    }

    /**
     * Returns the rank of the milestone between 0 - 5.
     *
     * @return the milestones rank, 0 <= rank <= 5
     */
    @Override
    public int getRank() {
        return rank;
    }

    /**
     * Returns the location of the milestone
     *
     * @return the milestones location
     */
    @Override
    public LatLng getLocation() {
        return location;
    }
}
