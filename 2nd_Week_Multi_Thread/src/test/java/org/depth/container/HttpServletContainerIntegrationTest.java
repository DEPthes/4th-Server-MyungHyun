package org.depth.container;

import org.depth.container.filter.Filter;
import org.depth.container.filter.FilterChain;
import org.depth.http.model.HttpHeader;
import org.depth.servlet.http.HttpServlet;
import org.depth.servlet.http.HttpServletRequest;
import org.depth.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class HttpServletContainerIntegrationTest {

    private HttpServletContainer servletContainer;
    private ExecutorService executorService;
    private final List<String> executionLog = new CopyOnWriteArrayList<>();

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(2);
        servletContainer = new HttpServletContainer(executorService);
        executionLog.clear();
    }

    @AfterEach
    void tearDown() {
        servletContainer.stop();
        executorService.shutdown();
    }

    @Test
    void testServletContainerWithFilters() throws Exception {
        // Given
        LoggingFilter loggingFilter = new LoggingFilter(executionLog);
        AuthenticationFilter authFilter = new AuthenticationFilter(executionLog);
        ResponseModifierFilter responseFilter = new ResponseModifierFilter(executionLog);
        
        EchoServlet echoServlet = new EchoServlet(executionLog);
        
        // When
        servletContainer.registerFilter(loggingFilter);
        servletContainer.registerFilter(authFilter);
        servletContainer.registerFilter(responseFilter);
        servletContainer.registerServlet(echoServlet);
        
        // Then
        assertEquals(3, servletContainer.getFilters().size());
        
        // 초기화 로그 확인
        assertTrue(executionLog.contains("LoggingFilter initialized"));
        assertTrue(executionLog.contains("AuthenticationFilter initialized"));
        assertTrue(executionLog.contains("ResponseModifierFilter initialized"));
        assertTrue(executionLog.contains("EchoServlet initialized"));
        
        // 필터 등록 해제 테스트
        servletContainer.unregisterFilter(authFilter);
        assertEquals(2, servletContainer.getFilters().size());
        assertTrue(executionLog.contains("AuthenticationFilter destroyed"));
        
        // 서블릿 등록 해제 테스트
        servletContainer.unregisterServlet(echoServlet);
        assertTrue(executionLog.contains("EchoServlet destroyed"));
    }
    
    // 로깅 필터
    static class LoggingFilter implements Filter {
        private final List<String> executionLog;
        
        public LoggingFilter(List<String> executionLog) {
            this.executionLog = executionLog;
        }
        
        @Override
        public void init() {
            executionLog.add("LoggingFilter initialized");
        }
        
        @Override
        public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException {
            executionLog.add("LoggingFilter - Before: " + request.getMethod() + " " + request.getPath());
            chain.doFilter(request, response);
            executionLog.add("LoggingFilter - After: Status " + response.getStatusCode());
        }
        
        @Override
        public void destroy() {
            executionLog.add("LoggingFilter destroyed");
        }
    }
    
    // 인증 필터
    static class AuthenticationFilter implements Filter {
        private final List<String> executionLog;
        
        public AuthenticationFilter(List<String> executionLog) {
            this.executionLog = executionLog;
        }
        
        @Override
        public void init() {
            executionLog.add("AuthenticationFilter initialized");
        }
        
        @Override
        public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException {
            boolean isAuthenticated = request.getHeaders().stream()
                    .anyMatch(h -> h.getName().equals("Authorization"));
            
            if (isAuthenticated) {
                executionLog.add("AuthenticationFilter - User authenticated");
                chain.doFilter(request, response);
            } else {
                executionLog.add("AuthenticationFilter - Authentication failed");
                response.setStatusCode(401);
                response.setStatusText("Unauthorized");
                response.setBody("Authentication required");
            }
        }
        
        @Override
        public void destroy() {
            executionLog.add("AuthenticationFilter destroyed");
        }
    }
    
    // 응답 수정 필터
    static class ResponseModifierFilter implements Filter {
        private final List<String> executionLog;
        
        public ResponseModifierFilter(List<String> executionLog) {
            this.executionLog = executionLog;
        }
        
        @Override
        public void init() {
            executionLog.add("ResponseModifierFilter initialized");
        }
        
        @Override
        public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException {
            chain.doFilter(request, response);
            
            // 응답 헤더 추가
            response.addHeader("X-Filter", "ResponseModifier");
            executionLog.add("ResponseModifierFilter - Added X-Filter header");
        }
        
        @Override
        public void destroy() {
            executionLog.add("ResponseModifierFilter destroyed");
        }
    }
    
    // 에코 서블릿
    static class EchoServlet extends HttpServlet {
        private final List<String> executionLog;
        
        public EchoServlet(List<String> executionLog) {
            this.executionLog = executionLog;
        }
        
        @Override
        public void init() {
            executionLog.add("EchoServlet initialized");
        }
        
        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) {
            executionLog.add("EchoServlet - Processing request: " + request.getMethod() + " " + request.getPath());
            
            response.setStatusCode(200);
            response.setStatusText("OK");
            response.setBody("Echo: " + request.getBody());
            
            executionLog.add("EchoServlet - Response generated");
        }
        
        @Override
        public void destroy() {
            executionLog.add("EchoServlet destroyed");
        }
        
        @Override
        public String getServletName() {
            return "EchoServlet";
        }
        
        @Override
        public String getServletPath() {
            return "/echo";
        }
    }
} 