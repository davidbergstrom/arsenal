package com.edit.reach.system;

import org.json.JSONObject;

public interface ResponseHandler {
	public void onGetSuccess(JSONObject json);

	public void onGetFail();
}
