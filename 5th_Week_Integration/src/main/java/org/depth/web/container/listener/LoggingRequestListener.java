package org.depth.web.container.listener;

import org.depth.web.servlet.http.HttpServletRequest;
import org.depth.web.servlet.http.HttpServletResponse;

/**
 * 요청 이벤트를 로깅하는 요청 리스너 구현체
 */
public class LoggingRequestListener implements RequestListener {
    
    @Override
    public boolean requestInitialized(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("[요청 시작] 경로: " + request.getPath() + ", 메소드: " + request.getMethod() + 
                ", 시간: " + System.currentTimeMillis());
        // 요청 처리 계속 진행
        return true;
    }
    
    @Override
    public void requestCompleted(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("[요청 완료] 경로: " + request.getPath() + ", 상태 코드: " + response.getStatusCode() + 
                ", 시간: " + System.currentTimeMillis());
    }
    
    @Override
    public boolean requestError(HttpServletRequest request, HttpServletResponse response, Throwable exception) {
        System.err.println("[요청 오류] 경로: " + request.getPath() + ", 오류: " + exception.getMessage());
        exception.printStackTrace();
        // 다른 오류 처리기에게 처리 위임 (false 반환 시)
        return false; 
    }
    
    @Override
    public void attributeAdded(HttpServletRequest request, String name, Object value) {
        System.out.println("[요청 속성 추가] 경로: " + request.getPath() + ", 속성: " + name + ", 값: " + value);
    }
    
    @Override
    public void attributeRemoved(HttpServletRequest request, String name) {
        System.out.println("[요청 속성 제거] 경로: " + request.getPath() + ", 속성: " + name);
    }
} 