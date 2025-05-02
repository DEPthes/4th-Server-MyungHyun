package org.depth.container;

import org.depth.container.filter.Filter;
import org.depth.container.filter.FilterChain;
import org.depth.http.handler.HttpResponseWriter;
import org.depth.servlet.http.HttpServlet;
import org.depth.servlet.http.HttpServletRequest;
import org.depth.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class HttpServletContainerFilterTest {

    private HttpServletContainer servletContainer;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(2);
        servletContainer = new HttpServletContainer(executorService);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
    }

    @Test
    void testRegisterFilter() {
        // Given
        TestFilter filter = new TestFilter();
        
        // When
        servletContainer.registerFilter(filter);
        
        // Then
        assertTrue(filter.isInitialized());
        assertEquals(1, servletContainer.getFilters().size());
        assertTrue(servletContainer.getFilters().contains(filter));
    }
    
    @Test
    void testUnregisterFilter() {
        // Given
        TestFilter filter = new TestFilter();
        servletContainer.registerFilter(filter);
        
        // When
        servletContainer.unregisterFilter(filter);
        
        // Then
        assertTrue(filter.isDestroyed());
        assertTrue(servletContainer.getFilters().isEmpty());
    }
    
    @Test
    void testMultipleFilters() {
        // Given
        AtomicInteger executionOrder = new AtomicInteger(0);
        
        OrderedFilter filter1 = new OrderedFilter(executionOrder, 1);
        OrderedFilter filter2 = new OrderedFilter(executionOrder, 2);
        OrderedFilter filter3 = new OrderedFilter(executionOrder, 3);
        
        // When
        servletContainer.registerFilter(filter1);
        servletContainer.registerFilter(filter2);
        servletContainer.registerFilter(filter3);
        
        // Then
        assertEquals(3, servletContainer.getFilters().size());
        assertEquals(filter1, servletContainer.getFilters().get(0));
        assertEquals(filter2, servletContainer.getFilters().get(1));
        assertEquals(filter3, servletContainer.getFilters().get(2));
    }
    
    @Test
    void testFilterExecution() throws IOException {
        // Given
        AtomicInteger executionOrder = new AtomicInteger(0);
        AtomicBoolean servletExecuted = new AtomicBoolean(false);
        
        OrderedFilter filter1 = new OrderedFilter(executionOrder, 1);
        OrderedFilter filter2 = new OrderedFilter(executionOrder, 2);
        
        TestServlet servlet = new TestServlet(executionOrder, 3, servletExecuted);
        
        HttpServletRequest request = HttpServletRequest.builder()
                .method("GET")
                .path("/test")
                .version("HTTP/1.1")
                .headers(new ArrayList<>())
                .body("")
                .build();
                
        HttpServletResponse response = HttpResponseWriter.createDefaultResponse();
        
        // When
        List<Filter> filters = new ArrayList<>();
        filters.add(filter1);
        filters.add(filter2);
        FilterChain filterChain = new FilterChain(filters, servlet);
        filterChain.doFilter(request, response);
        
        // Then
        assertTrue(filter1.isExecuted());
        assertTrue(filter2.isExecuted());
        assertTrue(servletExecuted.get());
        assertEquals(1, filter1.getExecutionOrder());
        assertEquals(2, filter2.getExecutionOrder());
        assertEquals(3, servlet.getExecutionOrder());
    }
    
    @Test
    void testFilterChainInterruption() throws IOException {
        // Given
        AtomicInteger executionOrder = new AtomicInteger(0);
        AtomicBoolean servletExecuted = new AtomicBoolean(false);
        
        InterruptingFilter interruptingFilter = new InterruptingFilter();
        OrderedFilter nextFilter = new OrderedFilter(executionOrder, 2);
        
        TestServlet servlet = new TestServlet(executionOrder, 3, servletExecuted);
        
        HttpServletRequest request = HttpServletRequest.builder()
                .method("GET")
                .path("/test")
                .version("HTTP/1.1")
                .headers(new ArrayList<>())
                .body("")
                .build();
                
        HttpServletResponse response = HttpResponseWriter.createDefaultResponse();
        
        // When
        List<Filter> filters = new ArrayList<>();
        filters.add(interruptingFilter);
        filters.add(nextFilter);
        FilterChain filterChain = new FilterChain(filters, servlet);
        filterChain.doFilter(request, response);
        
        // Then
        assertTrue(interruptingFilter.isExecuted());
        assertFalse(nextFilter.isExecuted());
        assertFalse(servletExecuted.get());
        assertEquals(403, response.getStatusCode());
        assertEquals("Access Denied", response.getStatusText());
    }
    
    // 테스트용 필터 클래스
    static class TestFilter implements Filter {
        private boolean initialized = false;
        private boolean destroyed = false;
        private boolean executed = false;
        
        @Override
        public void init() {
            initialized = true;
        }
        
        @Override
        public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException {
            executed = true;
            chain.doFilter(request, response);
        }
        
        @Override
        public void destroy() {
            destroyed = true;
        }
        
        public boolean isInitialized() {
            return initialized;
        }
        
        public boolean isDestroyed() {
            return destroyed;
        }
        
        public boolean isExecuted() {
            return executed;
        }
    }
    
    // 실행 순서를 확인하는 필터
    static class OrderedFilter implements Filter {
        private final AtomicInteger counter;
        private final int expectedOrder;
        private boolean executed = false;
        private int executionOrder;
        
        public OrderedFilter(AtomicInteger counter, int expectedOrder) {
            this.counter = counter;
            this.expectedOrder = expectedOrder;
        }
        
        @Override
        public void init() {
        }
        
        @Override
        public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException {
            executed = true;
            executionOrder = counter.incrementAndGet();
            chain.doFilter(request, response);
        }
        
        @Override
        public void destroy() {
        }
        
        public boolean isExecuted() {
            return executed;
        }
        
        public int getExecutionOrder() {
            return executionOrder;
        }
    }
    
    // 필터 체인을 중단하는 필터
    static class InterruptingFilter implements Filter {
        private boolean executed = false;
        
        @Override
        public void init() {
        }
        
        @Override
        public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException {
            executed = true;
            // 필터 체인 중단하고 직접 응답 설정
            response.setStatusCode(403);
            response.setStatusText("Access Denied");
            response.setBody("<html><body><h1>403 Forbidden</h1><p>Access Denied</p></body></html>");
        }
        
        @Override
        public void destroy() {
        }
        
        public boolean isExecuted() {
            return executed;
        }
    }
    
    // 테스트용 서블릿
    static class TestServlet extends HttpServlet {
        private final AtomicInteger counter;
        private final int expectedOrder;
        private final AtomicBoolean executed;
        private int executionOrder;
        
        public TestServlet(AtomicInteger counter, int expectedOrder, AtomicBoolean executed) {
            this.counter = counter;
            this.expectedOrder = expectedOrder;
            this.executed = executed;
        }
        
        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) {
            executed.set(true);
            executionOrder = counter.incrementAndGet();
            response.setStatusCode(200);
            response.setStatusText("OK");
            response.setBody("Test servlet executed");
        }
        
        @Override
        public String getServletName() {
            return "TestServlet";
        }
        
        @Override
        public String getServletPath() {
            return "/test";
        }
        
        public int getExecutionOrder() {
            return executionOrder;
        }
    }
} 