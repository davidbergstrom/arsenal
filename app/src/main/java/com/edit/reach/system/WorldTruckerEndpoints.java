package com.edit.reach.system;

import com.edit.reach.model.BoundingBox;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Responsible for handling all
 * server endpoints to the WorldTrucker API.
 */
public class WorldTruckerEndpoints {
	private static final String baseURL = "https://api.worldtrucker.com/v1/";

	private WorldTruckerEndpoints() {
	}

    /**
     * Creates a URL that is used to get all milestones
     * within a specified area.
     * @param bbox A square area on a map.
     * @return A URL used to get milestones within a specified square.
     * @throws MalformedURLException
     */
	public static URL getMilestonesURL(BoundingBox bbox) throws MalformedURLException {
		String urlString = "tip?bbox=" + bbox + "&categories=";
		return createURL(urlString);
	}

    /**
     * Puts together the complete URL.
     * @param url One part of the URL.
     * @return The complete URL.
     * @throws MalformedURLException
     */
	private static URL createURL(String url) throws MalformedURLException {
		return new URL(baseURL + url);
	}
}
