package com.edit.reach.model;

import com.edit.reach.model.interfaces.IMilestone;
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
        rank = properties.getInt("rating");

        int cat = properties.getInt("category");
        category = getCategoryGroup(cat);

        JSONObject geometry = json.getJSONObject("geometry");
        JSONArray coordinates = geometry.getJSONArray("coordinates");
        double latitude = coordinates.getDouble(0);
        double longitude = coordinates.getDouble(1);
        location = new LatLng(latitude, longitude);
    }

    /**
     * Takes a category number between 1-27 and
     * places it in the appropriate category group
     * @ return enum category
     */
    private Category getCategoryGroup(int category) {
        Category categoryGroup = Category.GASSTATION;
        switch(category) {
            case 1:
            case 2:
            case 4:
            case 12:
            case 25: categoryGroup = Category.FOOD;
                break;
            case 3: categoryGroup = Category.GASSTATION;
                break;
            case 8:
            case 9: categoryGroup = Category.SLEEP;
                break;
            case 22:
            case 23: categoryGroup = Category.OBSTRUCTION;
                break;
            case 24: categoryGroup = Category.ROAD_CAMERA;
                break;
            default: categoryGroup =  Category.RESTAREA;
        }

       return categoryGroup;
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
