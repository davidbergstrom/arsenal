package com.edit.reach.model;

import com.edit.reach.model.interfaces.IMilestone;

import java.util.ArrayList;
import java.util.Comparator;

import static com.edit.reach.model.interfaces.IMilestone.*;

public class RankingRating implements Comparator<IMilestone> {

    private final Category category;

    public RankingRating(Category category) {
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

    public void removeUnwanted(ArrayList<IMilestone> milestones) {
        for (int i = 0; i < milestones.size(); i++) {
            if (!milestones.get(i).hasCategory(category)) {
                milestones.remove(milestones.get(i));
            }
        }
    }
}
