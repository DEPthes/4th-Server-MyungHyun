package org.depth.container.filter;

import org.depth.servlet.Servlet;
import org.depth.servlet.ServletRequest;
import org.depth.servlet.ServletResponse;
import org.depth.servlet.http.HttpServletRequest;
import org.depth.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class FilterChain {
    private final List<Filter> filters;
    private final Servlet targetServlet;
    private int currentFilterIndex = 0;

    public FilterChain(List<Filter> filters, Servlet targetServlet) {
        this.filters = filters;
        this.targetServlet = targetServlet;
    }


    public void doFilter(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 필터가 모두 실행된 경우 서블릿 실행
        if (currentFilterIndex < filters.size()) {
            Filter filter = filters.get(currentFilterIndex);
            currentFilterIndex++;

            // 필터 초기화
            filter.doFilter(request, response, this);
        } else if (targetServlet != null) {
            try {
                targetServlet.service(request, response);
            } catch (Exception e) {
                throw new IOException("서블릿 실행 중 오류", e);
            }
        }
    }
} 