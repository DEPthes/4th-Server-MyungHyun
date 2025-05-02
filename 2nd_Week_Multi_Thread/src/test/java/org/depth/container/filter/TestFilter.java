package org.depth.container.filter;

import org.depth.servlet.http.HttpServletRequest;
import org.depth.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestFilter implements Filter {
    private boolean initialized = false;
    private boolean destroyed = false;
    private boolean filtered = false;
    private List<String> actionLog = new ArrayList<>();

    @Override
    public void init() {
        initialized = true;
        actionLog.add("init");
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException {
        filtered = true;
        actionLog.add("doFilter-before");
        // 필터가 처리한 후 체인의 다음 필터로 진행
        chain.doFilter(request, response);
        actionLog.add("doFilter-after");
    }

    @Override
    public void destroy() {
        destroyed = true;
        actionLog.add("destroy");
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean isFiltered() {
        return filtered;
    }

    public List<String> getActionLog() {
        return actionLog;
    }
} 