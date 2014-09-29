package com.edit.reach.model;

import android.location.Location;

/**
 * Created by Joakim Berntsson on 2014-09-29.
 * Interface for Milestones.
 *
 */
public interface IMilestone {

	public enum Category{
		RESTAURANT, RESTAREA, GASSTATION

	}

	/**
	 * Returns the name of the milestone
	 * @return String, the milestones name
	 */
	public String getName();

	/**
	 * Returns the description of the milestone
	 * @return String, 
	 */
	public String getDescription();

	/**
	 *
	 */
	public Category getCategory();

	/**
	 * Set the rank of the milestone
	 * @param newRank, the rank to set
	 */
	public void setRank(int newRank);

	/**
	 * Returns the rank of the milestone between 0 - 5.
	 * @return rank, <= 0 rank <= 5
	 */
	public int getRank();

	/**
	 *
	 * @return
	 */
	public Location getLocation();


}
