package com.edit.reach.model;

import com.google.android.gms.maps.model.LatLng;

public class Milestone implements IMilestone {
    /**
     * Returns the name of the milestone
     *
     * @return the milestones name
     */
    @Override
    public String getName() {
        return null;
    }

    /**
     * Returns the description of the milestone
     *
     * @return the milestones description
     */
    @Override
    public String getDescription() {
        return null;
    }

    /**
     * Returns the category of the milestone
     *
     * @return the milestones category
     */
    @Override
    public Category getCategory() {
        return null;
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
        return 0;
    }

    /**
     * Returns the location of the milestone
     *
     * @return the milestones location
     */
    @Override
    public LatLng getLocation() {
        return null;
    }
}
