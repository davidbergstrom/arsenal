package com.edit.reach.model;

import com.edit.reach.model.interfaces.IMilestone;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Milestone implements IMilestone {

    private String name;
    private String description;
    private ArrayList<Category> categories;
    private int rank;
    private LatLng location;

    public Milestone(JSONObject json) throws JSONException {
        JSONObject properties = json.getJSONObject("properties");

        name = properties.getString("name");
        description = properties.getString("description");
        rank = properties.getInt("rating");

        int categoryNumber = properties.getInt("category");
        setCategories(categoryNumber);

        JSONObject geometry = json.getJSONObject("geometry");
        JSONArray coordinates = geometry.getJSONArray("coordinates");
        double latitude = coordinates.getDouble(0);
        double longitude = coordinates.getDouble(1);
        location = new LatLng(latitude, longitude);
    }

    public String getSnippet() {
        return name + "\n"
               + description + "\n"
               + rank;
    }

    /**
     * Takes a category number between 1-27 and
     * places it in the appropriate category group
     * @ return enum category
     */
    private void setCategories(int category) {
        switch(category) {
            case 1:
            case 2:
            case 4:
            case 12:
            case 25:
                categories.add(Category.FOOD);
                categories.add(Category.TOILET);
                break;
            case 3:
                categories.add(Category.GASSTATION);
                break;
            case 8:
            case 9:
                categories.add(Category.SLEEP);
                categories.add(Category.TOILET);
                break;
            case 22:
            case 23:
                categories.add(Category.OBSTRUCTION);
                break;
            case 24:
                categories.add(Category.ROAD_CAMERA);
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
     * Checks wether the milestone has the requested categories or not
     * @return true if the milestone has the requested categories
     */
    @Override
    public boolean hasCategories(ArrayList<Category> requestedCategories) {
        return this.categories.containsAll(requestedCategories);
    }

    @Override
    public boolean hasCategory(Category requestedCategory) {
        return this.categories.contains(requestedCategory);
    }

    /**
     * Returns the categories associated with the milestone
     *
     * @return a list with the milestone's categories
     */
    @Override
    public ArrayList<Category> getCategories() {
        return categories;
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
