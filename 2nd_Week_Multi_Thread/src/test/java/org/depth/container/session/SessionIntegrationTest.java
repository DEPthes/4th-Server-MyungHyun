package org.depth.container.session;

import org.depth.CustomServlet;
import org.depth.container.HttpServletContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class SessionIntegrationTest {
    
    private HttpServletContainer container;
    private static final int PORT = 8888;
    private static final String BASE_URL = "http://localhost:" + PORT;
    
    @BeforeEach
    public void setup() throws IOException {
        // 테스트를 위한 컨테이너 생성 및 시작
        container = new HttpServletContainer();
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
    public void testSessionCreation() throws IOException {
        // 첫 번째 요청 - 세션 생성 확인
        HttpResponse response = sendRequest("/", null);
        
        // 응답에 세션 쿠키가 포함되어 있는지 확인
        assertNotNull(response.getSessionId(), "세션 ID 쿠키가 없습니다");
        assertTrue(response.getBody().contains("Visit Count: 1"), "첫 방문 카운트가 1이 아닙니다");
    }
    
    @Test
    public void testSessionPersistence() throws IOException {
        // 첫 번째 요청으로 세션 생성
        HttpResponse firstResponse = sendRequest("/", null);
        String sessionId = firstResponse.getSessionId();
        
        assertNotNull(sessionId, "세션 ID가 생성되지 않았습니다");
        
        // 두 번째 요청 - 동일한 세션 ID 사용
        Map<String, String> cookies = new HashMap<>();
        cookies.put("JSESSIONID", sessionId);
        HttpResponse secondResponse = sendRequest("/", cookies);
        
        // 세션이 유지되고 방문 카운트가 증가했는지 확인
        assertEquals(sessionId, secondResponse.getSessionId(), "세션 ID가 유지되지 않았습니다");
        assertTrue(secondResponse.getBody().contains("Visit Count: 2"), "두 번째 방문 카운트가 2가 아닙니다");
        
        // 세 번째 요청 - 동일한 세션 ID 사용
        HttpResponse thirdResponse = sendRequest("/", cookies);
        
        // 세션이 유지되고 방문 카운트가 더 증가했는지 확인
        assertEquals(sessionId, thirdResponse.getSessionId(), "세션 ID가 유지되지 않았습니다");
        assertTrue(thirdResponse.getBody().contains("Visit Count: 3"), "세 번째 방문 카운트가 3이 아닙니다");
    }
    
    @Test
    public void testMultipleSessions() throws IOException {
        // 첫 번째 세션
        HttpResponse firstSessionResponse = sendRequest("/", null);
        String firstSessionId = firstSessionResponse.getSessionId();
        
        // 두 번째 세션 (쿠키 없이 새로운 요청)
        HttpResponse secondSessionResponse = sendRequest("/", null);
        String secondSessionId = secondSessionResponse.getSessionId();
        
        // 두 세션 ID가 다른지 확인
        assertNotNull(firstSessionId, "첫 번째 세션 ID가 생성되지 않았습니다");
        assertNotNull(secondSessionId, "두 번째 세션 ID가 생성되지 않았습니다");
        assertNotEquals(firstSessionId, secondSessionId, "두 세션 ID가 동일합니다");
        
        // 첫 번째 세션으로 다시 요청
        Map<String, String> cookies = new HashMap<>();
        cookies.put("JSESSIONID", firstSessionId);
        HttpResponse revisitFirstSession = sendRequest("/", cookies);
        
        // 첫 번째 세션이 유지되는지 확인
        assertEquals(firstSessionId, revisitFirstSession.getSessionId(), "첫 번째 세션 ID가 유지되지 않았습니다");
        assertTrue(revisitFirstSession.getBody().contains("Visit Count: 2"), "첫 번째 세션의 두 번째 방문 카운트가 2가 아닙니다");
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
        
        // 응답 코드 확인
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode, "HTTP 응답 코드가 200이 아닙니다");
        
        // 응답 본문 읽기
        StringBuilder responseBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
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