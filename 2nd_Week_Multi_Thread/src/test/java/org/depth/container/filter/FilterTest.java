package org.depth.container.filter;

import org.depth.servlet.Servlet;
import org.depth.servlet.ServletRequest;
import org.depth.servlet.ServletResponse;
import org.depth.servlet.http.HttpServletRequest;
import org.depth.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FilterTest {
    
    private TestFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;
    private Servlet servlet;
    
    @BeforeEach
    public void setUp() {
        filter = new TestFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        servlet = mock(Servlet.class);
        filterChain = new FilterChain(java.util.Collections.emptyList(), servlet);
    }
    
    @Test
    public void testInitMethod() {
        // 초기화 전
        assertFalse(filter.isInitialized());
        
        // 초기화 호출
        filter.init();
        
        // 초기화 후
        assertTrue(filter.isInitialized());
        assertEquals("init", filter.getActionLog().get(0));
    }
    
    @Test
    public void testDoFilterMethod() throws IOException {
        // 필터 처리 전
        assertFalse(filter.isFiltered());
        
        // 필터 처리 호출
        filter.doFilter(request, response, filterChain);
        
        // 필터 처리 후
        assertTrue(filter.isFiltered());
        assertEquals(3, filter.getActionLog().size());
        assertEquals("doFilter-before", filter.getActionLog().get(0));
        assertEquals("doFilter-after", filter.getActionLog().get(2));
        
        // 서블릿이 호출되었는지 확인 (HTTP 서블릿 요청, 응답 파라미터)
        verify(servlet, times(1)).service(any(ServletRequest.class), any(ServletResponse.class));
    }
    
    @Test
    public void testDestroyMethod() {
        // 종료 전
        assertFalse(filter.isDestroyed());
        
        // 종료 호출
        filter.destroy();
        
        // 종료 후
        assertTrue(filter.isDestroyed());
        assertEquals("destroy", filter.getActionLog().get(0));
    }
} 