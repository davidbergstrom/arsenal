package com.edit.reach.app;

import org.json.JSONObject;

public interface ResponseHandler {
    public void onGetSuccess(JSONObject json);

    public void onGetFail();
}
