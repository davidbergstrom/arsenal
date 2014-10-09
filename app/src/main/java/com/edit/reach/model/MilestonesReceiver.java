package com.edit.reach.model;

import java.util.ArrayList;

public interface MilestonesReceiver {
    public void onMilestonesRecieved(ArrayList<Milestone> milestones);
    public void onMilestonesGetFailed();
}
