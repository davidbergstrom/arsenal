package com.edit.reach.model;

import java.util.List;

/**
 * Created by Nordmark on 2014-10-13.
 */
public interface SuggestionListener {

	public void onGetSuccess(List<String> results);
}