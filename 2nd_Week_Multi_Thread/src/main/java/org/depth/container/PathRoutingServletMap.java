package org.depth.container;

import org.depth.servlet.Servlet;

import java.util.HashMap;


public class PathRoutingServletMap extends HashMap<String, Servlet> {
    public Servlet findBestMatchingFor(String path) {
        if (path == null) {
            return null;
        }

        // 정확히 일치
        if (containsKey(path)) {
            return get(path);
        }

        // 가장 긴 경로 접두사 일치
        String longestMatch = null;
        for (String key : keySet()) {
            if (path.startsWith(key) && (longestMatch == null || key.length() > longestMatch.length())) {
                longestMatch = key;
            }
        }
        if (longestMatch != null) {
            return get(longestMatch);
        }

        // 확장자 일치
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex != -1) {
            String extension = path.substring(lastDotIndex);
            for (String key : keySet()) {
                if (key.startsWith("*") && key.endsWith(extension)) {
                    return get(key);
                }
            }
        }

        // 기본 서블릿
        return get("/*");
    }
}
