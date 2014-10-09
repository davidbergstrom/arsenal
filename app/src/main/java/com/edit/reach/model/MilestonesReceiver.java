package com.edit.reach.model;

import java.util.ArrayList;

public interface MilestonesReceiver {
    public void onMilestonesRecieved(ArrayList<IMilestone> milestones);
    public void onMilestonesGetFailed();
}
