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

import android.net.Uri;
import android.text.TextUtils;

import java.util.*;

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
    private final Map<String, Collection<Route>> mRoutes
            = new Hashtable<String, Collection<Route>>();

    public AppRoutes() {
    }

    public void dispose() {
        mRoutes.clear();
    }

    /**
     * Add the uri path and its listener to Route helper.
     * RouteListener will be informed once the uri path matched.
     * Note that the order will affect how the check been taken.
     *
     * @param path Route path
     * @param callback RouteListner responding to the matched route, can be null.
     */
    public void addRoute(String path, Callback callback) {
        Uri uri = Uri.parse(path);
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
        Uri uri = Uri.parse(url);
        return !TextUtils.isEmpty(uri.getScheme()) && mRoutes.containsKey(uri.getScheme());
    }

    public void routeUrl(String url) {
        routeUrl(url, null);
    }

    public void routeUrl(String url, Map<String, String> params) {
        if (canRoute(url)) {
            return;
        }

        Uri inputUri = Uri.parse(url);
        List<String> inputPaths = inputUri.getPathSegments();

        // Check if {@code inputUriStrings} is valid
        if (inputPaths.size() == 0) {
            // Maybe call a global error handler here
            return;
        }

        for (Route route : mRoutes.get(inputUri.getScheme())) {
            Uri routeUri = route.uri;
            List<String> routePaths = routeUri.getPathSegments();

            if (routePaths.size() != inputPaths.size()) {
                continue;
            }

            if (params == null) {
                params = new Hashtable<String, String>();
            }
            boolean isMatch = true;
            int routePathsSize = routePaths.size();
            for (int i = 0 ; i < routePathsSize ; i ++) {
                if (routePaths.get(i).startsWith(":")) {
                    params.put(routePaths.get(i).substring(1), inputPaths.get(i));
                } else {
                    if (!routePaths.get(i).equals(inputPaths.get(i))) {
                        isMatch = false;
                        break;
                    }
                }
            }
            if (!isMatch) {
                continue;
            }
            if (route.callback != null) {
                route.callback.call(params);
            }
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private static class Route {
        public final Uri uri;
        public final Callback callback;
        public Route(Uri uri, Callback callback) {
            this.uri = uri;
            this.callback = callback;
        }
    }
}

