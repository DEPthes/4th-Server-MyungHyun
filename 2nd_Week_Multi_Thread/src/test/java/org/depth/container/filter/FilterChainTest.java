package org.depth.container.filter;

import org.depth.servlet.Servlet;
import org.depth.servlet.ServletRequest;
import org.depth.servlet.ServletResponse;
import org.depth.servlet.http.HttpServletRequest;
import org.depth.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FilterChainTest {
    
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Servlet servlet;
    
    @BeforeEach
    public void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        servlet = mock(Servlet.class);
    }
    
    @Test
    public void testEmptyFilterChain() throws IOException {
        // 필터가 없는 체인 생성
        FilterChain filterChain = new FilterChain(new ArrayList<>(), servlet);
        
        // 체인 실행
        filterChain.doFilter(request, response);
        
        // 서블릿이 실행되었는지 확인
        verify(servlet, times(1)).service(any(ServletRequest.class), any(ServletResponse.class));
    }
    
    @Test
    public void testSingleFilterChain() throws IOException {
        // 테스트 필터 생성
        TestFilter filter = new TestFilter();
        
        // 필터 하나를 가진 체인 생성
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);
        FilterChain filterChain = new FilterChain(filters, servlet);
        
        // 체인 실행
        filterChain.doFilter(request, response);
        
        // 필터가 실행되었는지 확인
        assertTrue(filter.isFiltered());
        assertEquals(3, filter.getActionLog().size());
        assertEquals("doFilter-before", filter.getActionLog().get(0));
        assertEquals("doFilter-after", filter.getActionLog().get(2));
        
        // 서블릿이 실행되었는지 확인
        verify(servlet, times(1)).service(any(ServletRequest.class), any(ServletResponse.class));
    }
    
    @Test
    public void testMultipleFilterChain() throws IOException {
        // 여러 테스트 필터 생성
        TestFilter filter1 = new TestFilter();
        TestFilter filter2 = new TestFilter();
        TestFilter filter3 = new TestFilter();
        
        // 여러 필터를 가진 체인 생성
        List<Filter> filters = new ArrayList<>();
        filters.add(filter1);
        filters.add(filter2);
        filters.add(filter3);
        FilterChain filterChain = new FilterChain(filters, servlet);
        
        // 체인 실행
        filterChain.doFilter(request, response);
        
        // 모든 필터가 실행되었는지 확인
        assertTrue(filter1.isFiltered());
        assertTrue(filter2.isFiltered());
        assertTrue(filter3.isFiltered());
        
        // 필터 실행 순서 확인 - 첫 번째 필터
        assertEquals("doFilter-before", filter1.getActionLog().get(0));
        assertEquals("doFilter-after", filter1.getActionLog().get(2));
        
        // 필터 실행 순서 확인 - 두 번째 필터
        assertEquals("doFilter-before", filter2.getActionLog().get(0));
        assertEquals("doFilter-after", filter2.getActionLog().get(2));
        
        // 필터 실행 순서 확인 - 세 번째 필터
        assertEquals("doFilter-before", filter3.getActionLog().get(0));
        assertEquals("doFilter-after", filter3.getActionLog().get(2));
        
        // 서블릿이 실행되었는지 확인
        verify(servlet, times(1)).service(any(ServletRequest.class), any(ServletResponse.class));
    }
    
    @Test
    public void testNullServlet() throws IOException {
        // 테스트 필터 생성
        TestFilter filter = new TestFilter();
        
        // 서블릿이 null인 체인 생성
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);
        FilterChain filterChain = new FilterChain(filters, null);
        
        // 체인 실행
        filterChain.doFilter(request, response);
        
        // 필터가 실행되었는지 확인
        assertTrue(filter.isFiltered());
        
        // 서블릿은 null이므로 호출되지 않음
        verifyNoInteractions(servlet);
    }
    
    @Test
    public void testServletException() throws Exception {
        // Exception을 던지는 서블릿 설정
        doThrow(new RuntimeException("서블릿 오류")).when(servlet).service(any(ServletRequest.class), any(ServletResponse.class));
        
        // 필터와 체인 생성
        TestFilter filter = new TestFilter();
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);
        FilterChain filterChain = new FilterChain(filters, servlet);
        
        // 체인 실행 시 예외가 발생하는지 확인
        Exception exception = assertThrows(IOException.class, () -> {
            filterChain.doFilter(request, response);
        });
        
        // 예외 메시지 확인
        assertTrue(exception.getMessage().contains("서블릿 실행 중 오류"));
        
        // 필터는 실행되었지만 after 부분은 실행되지 않음
        assertTrue(filter.isFiltered());
        assertEquals("doFilter-before", filter.getActionLog().get(0));
        assertEquals(1, filter.getActionLog().size());
    }
    
    @Test
    public void testFilterException() throws Exception {
        // 예외를 던지는 필터 생성
        ExceptionFilter exceptionFilter = new ExceptionFilter("필터 오류");
        TestFilter normalFilter = new TestFilter();
        
        // 필터와 체인 생성 - 예외 필터를 첫 번째로 배치
        List<Filter> filters = new ArrayList<>();
        filters.add(exceptionFilter);
        filters.add(normalFilter);
        FilterChain filterChain = new FilterChain(filters, servlet);
        
        // 체인 실행 시 예외가 발생하는지 확인
        Exception exception = assertThrows(IOException.class, () -> {
            filterChain.doFilter(request, response);
        });
        
        // 예외 메시지 확인
        assertEquals("필터 오류", exception.getMessage());
        
        // 두 번째 필터와 서블릿은 실행되지 않아야 함
        assertFalse(normalFilter.isFiltered());
        verifyNoInteractions(servlet);
    }
    
    @Test
    public void testFilterExceptionInMiddle() throws Exception {
        // 필터 여러 개 생성
        TestFilter filter1 = new TestFilter();
        ExceptionFilter exceptionFilter = new ExceptionFilter("중간 필터 오류");
        TestFilter filter3 = new TestFilter();
        
        // 필터와 체인 생성 - 예외 필터를 중간에 배치
        List<Filter> filters = new ArrayList<>();
        filters.add(filter1);
        filters.add(exceptionFilter);
        filters.add(filter3);
        FilterChain filterChain = new FilterChain(filters, servlet);
        
        // 체인 실행 시 예외가 발생하는지 확인
        Exception exception = assertThrows(IOException.class, () -> {
            filterChain.doFilter(request, response);
        });
        
        // 예외 메시지 확인
        assertEquals("중간 필터 오류", exception.getMessage());
        
        // 첫 번째 필터는 실행되었지만, 세 번째 필터와 서블릿은 실행되지 않아야 함
        assertTrue(filter1.isFiltered());
        assertFalse(filter3.isFiltered());
        verifyNoInteractions(servlet);
        
        // 첫 번째 필터의 after 부분은 실행되지 않음
        assertEquals("doFilter-before", filter1.getActionLog().get(0));
        assertEquals(1, filter1.getActionLog().size());
    }
} 