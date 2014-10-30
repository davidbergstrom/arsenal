package com.edit.reach.model;

import java.util.ArrayList;
import java.util.Comparator;

import static com.edit.reach.model.IMilestone.*;

/**
 * A class helping the Ranking class to make specialized lists
 */
public class RankingHelper implements Comparator<IMilestone> {

	private final Category category;

	/**
	 * Creates a RankingHelper with a specified category
	 * @param category The category wanted inside a list
	 */
	public RankingHelper(Category category) {
		this.category = category;
	}

	@Override
	public int compare(IMilestone m1, IMilestone m2) {
		if (m1.getRank() > m2.getRank())
			return 1;
		if (m1.getRank() < m2.getRank())
			return -1;
		return 0;
	}

	/**
	 * Removes all milestones that are not within this instance's category
	 * @param milestones A list of milestones.
	 */
	public void removeUnwanted(ArrayList<IMilestone> milestones) {
		for (int i = 0; i < milestones.size(); i++) {
			if (!milestones.get(i).hasCategory(category)) {
				milestones.remove(milestones.get(i));
			}
		}
	}
}
