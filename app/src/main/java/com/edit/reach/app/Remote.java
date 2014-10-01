package com.edit.reach.app;

import android.os.AsyncTask;

public class Remote {

    public void get(String url) {
        new GetMilestoneTask().execute();
    }

    private class GetMilestoneTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return null;
        }
    }
}