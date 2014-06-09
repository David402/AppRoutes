/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cardinalblue.approutes;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import com.cardinalblue.approutes.utils.TextUtils;
import com.cardinalblue.approutes.utils.URIUtils;

/**
 * /users/:uid/profile - auto parse :uid into parameter `uid`
 * /users/:uid/profile with params `@uid` - indicates preloaded `uid` info stored in params with `@uid` key
 *
 * {@code addRoute} -
 * {@code remoteRoute} -
 * {@code canRoute} -
 * {@code routeUrl} -
 * {@code routeUrlWithParams}
 * {@code toString} -
 */
public class AppRoutes {
    private final Map<String, Collection<Route>> mRoutes;

    public AppRoutes() {
        mRoutes = new Hashtable<String, Collection<Route>>();
    }

    public void dispose() {
        mRoutes.clear();
    }

    /**
     * Add the uri path and its listener to Route helper.
     * RouteListener will be informed once the uri path matched.
     * Note that the order will affect how the check been taken.
     *
     * @param path path
     * @param callback callback to be notified when route is matched
     */
    public void addRoute(String path, Callback callback) {
        URI uri;
        try {
            uri = new URI(path);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid URI format of parameter `path`");
        }
        if (!TextUtils.isEmpty(uri.getScheme())) {
            if (mRoutes.containsKey(uri.getScheme())) {
                mRoutes.get(uri.getScheme()).add(new Route(uri, callback));
            } else {
                Vector<Route> urls = new Vector<Route>();
                urls.add(new Route(uri, callback));
                mRoutes.put(uri.getScheme(), urls);
            }
        }
    }

    public void romveRoute(String url) {
        mRoutes.remove(url);
    }

    /**
     * Check if can route with scheme of {@code url}
     * It returns {@code True} only if scheme of {@code url} exists and there is any
     * registered route can handle this scheme.
     *
     * @param url Route url
     * @return {@code True} if can handle, otherwise return {@code False}
     */
    public boolean canRoute(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            return false;
        }
        return !TextUtils.isEmpty(uri.getScheme()) && mRoutes.containsKey(uri.getScheme());
    }

    public boolean routeUrl(String url) {
        return routeUrl(url, null);
    }

    public boolean routeUrl(String url, Map<String, Object> parameters) {
        if (!canRoute(url)) {
            return false;
        }

        URI inputUri;
        try {
           inputUri = new URI(url);
        } catch (URISyntaxException e) {
            return false;
        }
        String[] inputPaths = URIUtils.getPathSegments(inputUri);

        // Check if {@code inputUriStrings} is valid
        if (inputPaths == null || inputPaths.length == 0) {
            // Maybe call a global error handler here
            return false;
        }

        for (Route route : mRoutes.get(inputUri.getScheme())) {
            URI routeUri = route.uri;
            String[] routePaths = URIUtils.getPathSegments(routeUri);
            if (routePaths == null || routePaths.length == 0) {
                continue;
            }

            if (routePaths.length != inputPaths.length) {
                continue;
            }

            Map<String, String> variables = parseVariablesForUri(inputUri, routePaths);
            if (variables == null) {
                continue;
            }

            // Gotcha!
            // Matched route is found, merge parameters and call callback if exists
            if (parameters == null) {
                parameters = new HashMap<String, Object>();
            }
            parameters.putAll(variables);
            if (route.callback != null) {
                route.callback.call(parameters);
            }
            break;
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder("AppRoutes routes: ").append(mRoutes).toString();
    }

    private Map<String, String> parseVariablesForUri(final URI uri, final String[] routePaths) {
        Map<String, String> routeParams = null;
        Map<String, String> variables = new HashMap<String, String>();
        String[] inputPaths = URIUtils.getPathSegments(uri);

        boolean isComponentCountEqual = routePaths.length == inputPaths.length;
        if (isComponentCountEqual) {
            boolean isMatch = true;
            int routePathsSize = routePaths.length;
            for (int i = 0 ; i < routePathsSize ; i ++) {
                if (routePaths[i].startsWith(":")) {
                    variables.put(routePaths[i].substring(1), inputPaths[i]);
                } else if (!routePaths[i].equals(inputPaths[i])) {
                    isMatch = false;
                    break;
                }
            }
            if (isMatch) {
                routeParams = variables;
            }
        }

        return routeParams;
    }

    private static class Route {
        private final URI uri;
        private final Callback callback;
        public Route(URI uri, Callback callback) {
            this.uri = uri;
            this.callback = callback;
        }
    }
}

