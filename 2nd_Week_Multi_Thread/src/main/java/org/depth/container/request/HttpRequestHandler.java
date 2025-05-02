package org.depth.container.request;

import lombok.RequiredArgsConstructor;
import org.depth.container.PathRoutingServletMap;
import org.depth.container.filter.Filter;
import org.depth.container.filter.FilterChain;
import org.depth.container.session.SessionManager;
import org.depth.http.HttpRequest;
import org.depth.http.handler.HttpRequestParser;
import org.depth.http.handler.HttpResponseWriter;
import org.depth.http.model.HttpHeader;
import org.depth.http.model.HttpSession;
import org.depth.servlet.Servlet;
import org.depth.servlet.http.HttpServletRequest;
import org.depth.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class HttpRequestHandler implements RequestHandler {
    private final PathRoutingServletMap pathRoutingServletMap;
    private final List<Filter> filters;
    private final SessionManager sessionManager;
    
    // 세션 쿠키 이름
    private static final String SESSION_COOKIE_NAME = "JSESSIONID";

    @Override
    public void handle(Socket clientSocket) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream outputStream = clientSocket.getOutputStream()
        ) {
            HttpServletRequest request = HttpRequestParser.parse(reader);
            HttpServletResponse response = HttpResponseWriter.createDefaultResponse();
            
            // 세션 처리
            processSession(request, response);

            Servlet bestMatchingServlet = pathRoutingServletMap.findBestMatchingFor(request.getPath());

            if (bestMatchingServlet != null) {
                FilterChain filterChain = new FilterChain(filters, bestMatchingServlet);
                filterChain.doFilter(request, response);
            } else {
                response = HttpResponseWriter.createNotFoundResponse(request.getPath());
            }

            outputStream.write(response.getContent());
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 요청의 쿠키를 확인하여 세션을 처리합니다.
     * 세션이 없거나 만료된 경우 새 세션을 생성하고, 
     * 세션 ID를 쿠키로 응답에 포함시킵니다.
     */
    private void processSession(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 세션 ID 추출
        String sessionId = extractSessionIdFromCookies(request);
        
        // 세션 ID로 세션 조회 또는 생성
        HttpSession session;
        if (sessionId != null) {
            session = sessionManager.getSession(sessionId);
        } else {
            session = null;
        }
        
        // 세션이 없거나 만료된 경우 새 세션 생성
        if (session == null) {
            session = sessionManager.createSession();
            // 응답에 세션 쿠키 추가
            response.addHeader("Set-Cookie", SESSION_COOKIE_NAME + "=" + session.getId() + "; Path=/; HttpOnly");
        }
        
        // 요청에 세션 설정
        request.setSession(session);
    }
    
    /**
     * 요청 헤더에서 세션 ID를 추출합니다.
     * @param request HTTP 요청
     * @return 세션 ID 또는 null
     */
    private String extractSessionIdFromCookies(HttpServletRequest request) {
        Optional<HttpHeader> cookieHeader = request.getHeaders().stream()
                .filter(header -> "Cookie".equalsIgnoreCase(header.getName()))
                .findFirst();
        
        if (cookieHeader.isPresent()) {
            String cookieValue = cookieHeader.get().getValue();
            String[] cookies = cookieValue.split(";");
            
            for (String cookie : cookies) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && SESSION_COOKIE_NAME.equals(parts[0])) {
                    return parts[1];
                }
            }
        }
        
        return null;
    }
}
