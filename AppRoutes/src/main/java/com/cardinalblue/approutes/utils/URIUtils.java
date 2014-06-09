package com.cardinalblue.approutes.utils;


import java.net.URI;

public class URIUtils {
    public static String[] getPathSegments(URI uri) {
        return uri.getPath().split("/");
    }

}
