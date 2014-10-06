package com.edit.reach.app;

import java.net.MalformedURLException;
import java.net.URL;

public class WorldTruckerEndpoints {
    private static String baseURL = "https://api.worldtrucker.com/v1/";

    private WorldTruckerEndpoints() {
    }

    public static URL getMilestonesURL(BoundingBox bbox, int[] categories) throws MalformedURLException {
        String urlString = "tip?bbox=" + bbox + "&categories=" + categories;
        return createURL(urlString);
    }

    private static URL createURL(String url) throws MalformedURLException {
        return new URL(baseURL + url);
    }
}
