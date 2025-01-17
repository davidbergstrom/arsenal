package com.edit.reach.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Joakim Berntsson on 2014-09-29.
 * Interface for Milestones.
 *
 */
public interface IMilestone {

	/**
	 * Category is an enum class to identify a milestones type.
	 */
	public enum Category{
		RESTAURANT, RESTAREA, GASSTATION, OBSTRUCTION, FOOD, SLEEP, TOILET, ROAD_CAMERA
	}

	/**
	 * Returns the name of the milestone
	 * @return the milestones name
	 */
	public String getName();

	/**
	 * Returns the description of the milestone
	 * @return the milestones description
	 */
	public String getDescription();

	/**
	 * Returns a 'snippet' of the milestone, which is the rank followed by the description.
	 * @return the snippet
	 */
	public String getSnippet();

	/**
	 * Checks wether the milestone has the requested categories or not
	 * @return true if the milestone has the requested categories
	 */
	public boolean hasCategories(ArrayList<Category> requestedCategories);

	/**
	 * Checks wether the milestone has the requested category or not
	 * @return true if the milestone has the requested category
	 */
	public boolean hasCategory(Category requestedCategory);

	/**
	 * Returns the categories associated with the milestone
	 * @return a list with the milestone's categories
	 */
	public ArrayList<Category> getCategories();

	/**
	 * Set the rank of the milestone
	 * @param newRank, the rank to set
	 */
	public void setRank(int newRank);

	/**
	 * Returns the rank of the milestone between 0 - 5.
	 * @return the milestones rank, 0 <= rank <= 5
	 */
	public int getRank();

	/**
	 * Returns the location of the milestone
	 * @return the milestones location
	 */
	public LatLng getLocation();

}
