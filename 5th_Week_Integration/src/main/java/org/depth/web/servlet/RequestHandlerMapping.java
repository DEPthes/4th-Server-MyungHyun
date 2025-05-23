package org.depth.web.servlet;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RequestHandlerMapping {
    private final Map<String, RequestHandlerMappingItem> handlers = new HashMap<>();

    public RequestHandlerMappingItem matchByPath(String path, String method) {
        RequestHandlerMappingItem found = handlers.get(path);

        if(found == null) {
            found = handlers.get("");
        }

        if(found == null) {
            throw new IllegalArgumentException("No handler found for path: " + path);
        }

        if(found.getMethod().equals(method)) {
            return found;
        } else {
            throw new IllegalArgumentException("Method not allowed: " + method);
        }
    }

    public void addHandler(String path, String method, Object controller, Method methodToCall) {
        RequestHandlerMappingItem item = new RequestHandlerMappingItem(path, method, controller, methodToCall);
        handlers.put(path, item);
    }

    public RequestHandlerMappingItem getHandler(String path) {
        return handlers.get(path);
    }

    public void removeHandler(String path) {
        handlers.remove(path);
    }

    @AllArgsConstructor
    @Data
    public static class RequestHandlerMappingItem {
        private final String path;
        private final String method;
        private final Object controller;
        private final Method methodToCall;
    }
}
