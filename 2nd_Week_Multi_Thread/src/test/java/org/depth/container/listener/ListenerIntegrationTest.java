package org.depth.container.listener;

import org.depth.CustomServlet;
import org.depth.container.HttpServletContainer;
import org.depth.http.model.HttpSession;
import org.depth.servlet.http.HttpServletRequest;
import org.depth.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class ListenerIntegrationTest {
    
    private HttpServletContainer container;
    private static final int PORT = 8889;
    private static final String BASE_URL = "http://localhost:" + PORT;
    
    // 테스트용 리스너 구현
    private TestSessionListener sessionListener;
    private TestRequestListener requestListener;
    
    @BeforeEach
    public void setup() throws IOException {
        // 테스트를 위한 컨테이너 생성
        container = new HttpServletContainer();
        
        // 테스트용 리스너 생성
        sessionListener = new TestSessionListener();
        requestListener = new TestRequestListener();
        
        // 리스너 등록
        container.registerSessionListener(sessionListener);
        container.registerRequestListener(requestListener);
        
        // 서블릿 등록 및 서버 시작
        container.registerServlet(new CustomServlet());
        container.start(PORT);
        
        // 서버가 시작될 시간을 주기 위해 잠시 대기
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @AfterEach
    public void tearDown() {
        // 테스트 완료 후 컨테이너 중지
        if (container != null) {
            container.stop();
        }
    }
    
    @Test
    public void testSessionAndRequestListeners() throws IOException {
        // 첫 번째 요청 전 리스너 카운터 확인
        assertEquals(0, sessionListener.getSessionCreatedCount(), "세션 생성 이벤트가 아직 발생하지 않아야 합니다");
        assertEquals(0, requestListener.getRequestInitializedCount(), "요청 시작 이벤트가 아직 발생하지 않아야 합니다");
        assertEquals(0, requestListener.getRequestCompletedCount(), "요청 종료 이벤트가 아직 발생하지 않아야 합니다");
        
        // 첫 번째 요청 - 세션 생성 및 요청 이벤트 확인
        HttpResponse firstResponse = sendRequest("/", null);
        
        // 세션 생성 및 요청 이벤트 확인
        assertEquals(1, sessionListener.getSessionCreatedCount(), "세션 생성 이벤트가 발생해야 합니다");
        assertEquals(0, sessionListener.getSessionInvalidatedCount(), "세션 소멸 이벤트가 아직 발생하지 않아야 합니다");
        assertEquals(1, requestListener.getRequestInitializedCount(), "요청 시작 이벤트가 발생해야 합니다");
        assertEquals(1, requestListener.getRequestCompletedCount(), "요청 종료 이벤트가 발생해야 합니다");
        
        // 세션 ID 확인
        String sessionId = firstResponse.getSessionId();
        assertNotNull(sessionId, "세션 ID가 생성되지 않았습니다");
        assertEquals(sessionId, sessionListener.getLastSessionId(), "세션 ID가 리스너에 전달된 것과 일치해야 합니다");
        
        // 두 번째 요청 - 기존 세션 재사용 및 요청 이벤트 확인
        Map<String, String> cookies = new HashMap<>();
        cookies.put("JSESSIONID", sessionId);
        HttpResponse secondResponse = sendRequest("/", cookies);
        
        // 리스너 이벤트 카운트 확인
        assertEquals(1, sessionListener.getSessionCreatedCount(), "세션 생성 이벤트가 추가로 발생하지 않아야 합니다");
        assertEquals(1, sessionListener.getSessionAccessedCount(), "세션 접근 이벤트가 발생해야 합니다");
        assertEquals(0, sessionListener.getSessionInvalidatedCount(), "세션 소멸 이벤트가 아직 발생하지 않아야 합니다");
        assertEquals(2, requestListener.getRequestInitializedCount(), "요청 시작 이벤트가 두 번 발생해야 합니다");
        assertEquals(2, requestListener.getRequestCompletedCount(), "요청 종료 이벤트가 두 번 발생해야 합니다");
        
        // 세션 ID 유지 확인
        assertEquals(sessionId, secondResponse.getSessionId(), "세션 ID가 유지되어야 합니다");
    }
    
    /**
     * 테스트용 세션 리스너 구현
     */
    private static class TestSessionListener implements SessionListener {
        private final AtomicInteger sessionCreatedCount = new AtomicInteger(0);
        private final AtomicInteger sessionAccessedCount = new AtomicInteger(0);
        private final AtomicInteger sessionInvalidatedCount = new AtomicInteger(0);
        private final AtomicInteger attributeAddedCount = new AtomicInteger(0);
        private final AtomicInteger attributeRemovedCount = new AtomicInteger(0);
        private String lastSessionId;
        
        @Override
        public void sessionCreated(HttpSession session) {
            sessionCreatedCount.incrementAndGet();
            lastSessionId = session.getId();
            System.out.println("[테스트] 세션 생성 이벤트 발생: " + lastSessionId);
        }
        
        @Override
        public void sessionAccessed(HttpSession session) {
            sessionAccessedCount.incrementAndGet();
            System.out.println("[테스트] 세션 접근 이벤트 발생: " + session.getId());
        }
        
        @Override
        public void sessionInvalidated(HttpSession session) {
            sessionInvalidatedCount.incrementAndGet();
            System.out.println("[테스트] 세션 소멸 이벤트 발생: " + session.getId());
        }
        
        @Override
        public void attributeAdded(HttpSession session, String name, Object value) {
            attributeAddedCount.incrementAndGet();
            System.out.println("[테스트] 세션 속성 추가 이벤트 발생: " + session.getId() + ", 속성: " + name);
        }
        
        @Override
        public void attributeRemoved(HttpSession session, String name) {
            attributeRemovedCount.incrementAndGet();
            System.out.println("[테스트] 세션 속성 제거 이벤트 발생: " + session.getId() + ", 속성: " + name);
        }
        
        public int getSessionCreatedCount() {
            return sessionCreatedCount.get();
        }
        
        public int getSessionAccessedCount() {
            return sessionAccessedCount.get();
        }
        
        public int getSessionInvalidatedCount() {
            return sessionInvalidatedCount.get();
        }
        
        public int getAttributeAddedCount() {
            return attributeAddedCount.get();
        }
        
        public int getAttributeRemovedCount() {
            return attributeRemovedCount.get();
        }
        
        public String getLastSessionId() {
            return lastSessionId;
        }
    }
    
    /**
     * 테스트용 요청 리스너 구현
     */
    private static class TestRequestListener implements RequestListener {
        private final AtomicInteger requestInitializedCount = new AtomicInteger(0);
        private final AtomicInteger requestCompletedCount = new AtomicInteger(0);
        private final AtomicInteger requestErrorCount = new AtomicInteger(0);
        private final AtomicInteger attributeAddedCount = new AtomicInteger(0);
        private final AtomicInteger attributeRemovedCount = new AtomicInteger(0);
        private String lastRequestPath;
        
        @Override
        public boolean requestInitialized(HttpServletRequest request, HttpServletResponse response) {
            requestInitializedCount.incrementAndGet();
            lastRequestPath = request.getPath();
            System.out.println("[테스트] 요청 시작 이벤트 발생: " + lastRequestPath);
            // 요청 처리 계속 진행
            return true;
        }
        
        @Override
        public void requestCompleted(HttpServletRequest request, HttpServletResponse response) {
            requestCompletedCount.incrementAndGet();
            System.out.println("[테스트] 요청 완료 이벤트 발생: " + request.getPath() + ", 상태 코드: " + response.getStatusCode());
        }
        
        @Override
        public boolean requestError(HttpServletRequest request, HttpServletResponse response, Throwable exception) {
            requestErrorCount.incrementAndGet();
            System.out.println("[테스트] 요청 오류 이벤트 발생: " + request.getPath() + ", 오류: " + exception.getMessage());
            // 다른 오류 처리기에게 처리 위임
            return false;
        }
        
        @Override
        public void attributeAdded(HttpServletRequest request, String name, Object value) {
            attributeAddedCount.incrementAndGet();
            System.out.println("[테스트] 요청 속성 추가 이벤트 발생: " + request.getPath() + ", 속성: " + name);
        }
        
        @Override
        public void attributeRemoved(HttpServletRequest request, String name) {
            attributeRemovedCount.incrementAndGet();
            System.out.println("[테스트] 요청 속성 제거 이벤트 발생: " + request.getPath() + ", 속성: " + name);
        }
        
        public int getRequestInitializedCount() {
            return requestInitializedCount.get();
        }
        
        public int getRequestCompletedCount() {
            return requestCompletedCount.get();
        }
        
        public int getRequestErrorCount() {
            return requestErrorCount.get();
        }
        
        public int getAttributeAddedCount() {
            return attributeAddedCount.get();
        }
        
        public int getAttributeRemovedCount() {
            return attributeRemovedCount.get();
        }
        
        public String getLastRequestPath() {
            return lastRequestPath;
        }
    }
    
    /**
     * HTTP 요청을 보내고 응답을 받는 헬퍼 메소드
     */
    private HttpResponse sendRequest(String path, Map<String, String> cookies) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        // 쿠키 설정 (있는 경우)
        if (cookies != null && !cookies.isEmpty()) {
            StringBuilder cookieString = new StringBuilder();
            for (Map.Entry<String, String> cookie : cookies.entrySet()) {
                if (cookieString.length() > 0) {
                    cookieString.append("; ");
                }
                cookieString.append(cookie.getKey()).append("=").append(cookie.getValue());
            }
            connection.setRequestProperty("Cookie", cookieString.toString());
        }
        
        // 응답 코드
        int responseCode = connection.getResponseCode();
        
        // 응답 본문 읽기 (가능한 경우)
        StringBuilder responseBody = new StringBuilder();
        try (BufferedReader reader = responseCode < 400 ? 
                new BufferedReader(new InputStreamReader(connection.getInputStream())) :
                new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line);
            }
        }
        
        // 응답 헤더에서 세션 ID 쿠키 추출
        String sessionId = null;
        Map<String, List<String>> headers = connection.getHeaderFields();
        List<String> setCookieHeaders = headers.get("Set-Cookie");
        if (setCookieHeaders != null) {
            for (String header : setCookieHeaders) {
                if (header.startsWith("JSESSIONID=")) {
                    Pattern pattern = Pattern.compile("JSESSIONID=([^;]+)");
                    Matcher matcher = pattern.matcher(header);
                    if (matcher.find()) {
                        sessionId = matcher.group(1);
                    }
                }
            }
        }
        
        // 쿠키 헤더에서 세션 ID 추출 (응답에 Set-Cookie가 없는 경우)
        if (sessionId == null && cookies != null) {
            sessionId = cookies.get("JSESSIONID");
        }
        
        return new HttpResponse(responseCode, responseBody.toString(), sessionId);
    }
    
    /**
     * HTTP 응답을 나타내는 내부 클래스
     */
    private static class HttpResponse {
        private final int statusCode;
        private final String body;
        private final String sessionId;
        
        public HttpResponse(int statusCode, String body, String sessionId) {
            this.statusCode = statusCode;
            this.body = body;
            this.sessionId = sessionId;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public String getBody() {
            return body;
        }
        
        public String getSessionId() {
            return sessionId;
        }
    }
} 